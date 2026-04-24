package org.logo.lsp.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logo.lsp.symbol.Reference;
import org.logo.lsp.symbol.Symbol;
import org.logo.lsp.symbol.SymbolTable;
import org.logo.lsp.symbol.SymbolType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentAnalyzerTest {

    private DocumentAnalyzer analyzer;
    private static final String URI = "file:///test.logo";

    @BeforeEach
    void setUp() {
        analyzer = new DocumentAnalyzer();
    }

    // ── Procedure definitions ───────────────────────────────────────────

    @Test
    void testSimpleProcedureDefinition() {
        analyzer.analyze("TO square :size\nFD :size\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol proc = table.resolve("square", "global");
        assertNotNull(proc);
        assertEquals(SymbolType.PROCEDURE, proc.getType());
        assertEquals("square", proc.getName());
        assertEquals(1, proc.getParameterCount());
        assertEquals("size", proc.getParameterNames().get(0));
    }

    @Test
    void testProcedureWithMultipleParams() {
        analyzer.analyze("TO rect :width :height\nFD :width\nRT 90\nFD :height\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol proc = table.resolve("rect", "global");
        assertNotNull(proc);
        assertEquals(2, proc.getParameterCount());
        assertEquals("width", proc.getParameterNames().get(0));
        assertEquals("height", proc.getParameterNames().get(1));
    }

    @Test
    void testProcedureNoParams() {
        analyzer.analyze("TO greet\nPRINT \"hello\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol proc = table.resolve("greet", "global");
        assertNotNull(proc);
        assertEquals(0, proc.getParameterCount());
    }

    // ── Case insensitivity ──────────────────────────────────────────────

    @Test
    void testCaseInsensitiveLookup() {
        analyzer.analyze("TO Square :size\nFD :size\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        assertNotNull(table.resolve("square", "global"));
        assertNotNull(table.resolve("SQUARE", "global"));
        assertNotNull(table.resolve("Square", "global"));
    }

    // ── Forward references ──────────────────────────────────────────────

    @Test
    void testForwardReference() {
        String code = """
                square 100
                TO square :size
                REPEAT 4 [ FD :size RT 90 ]
                END
                """;
        analyzer.analyze(code, URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol proc = table.resolve("square", "global");
        assertNotNull(proc);

        List<Reference> refs = table.getReferences("square");
        assertFalse(refs.isEmpty());
    }

    // ── Parameters and scope ────────────────────────────────────────────

    @Test
    void testParametersInProcedureScope() {
        analyzer.analyze("TO circle :radius\nFD :radius\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol param = table.resolve("radius", "circle");
        assertNotNull(param);
        assertEquals(SymbolType.PARAMETER, param.getType());

        Symbol global = table.resolve("radius", "global");
        assertNull(global);
    }

    // ── Variable shadowing ──────────────────────────────────────────────

    @Test
    void testVariableShadowing() {
        String code = """
                MAKE "x 10
                TO myproc :x
                FD :x
                END
                """;
        analyzer.analyze(code, URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol globalX = table.resolve("x", "global");
        assertNotNull(globalX);
        assertEquals(SymbolType.VARIABLE, globalX.getType());

        Symbol localX = table.resolve("x", "myproc");
        assertNotNull(localX);
        assertEquals(SymbolType.PARAMETER, localX.getType());
    }

    // ── Multiple definitions ────────────────────────────────────────────

    @Test
    void testMultipleDefinitionsLastWins() {
        String code = """
                TO square :size
                REPEAT 4 [ FD :size RT 90 ]
                END
                TO square :side
                REPEAT 4 [ FD :side RT 90 ]
                END
                """;
        analyzer.analyze(code, URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol proc = table.resolve("square", "global");
        assertNotNull(proc);
        assertEquals("side", proc.getParameterNames().get(0));
    }

    // ── Variable references ─────────────────────────────────────────────

    @Test
    void testVariableReferences() {
        analyzer.analyze("TO move :dist\nFD :dist\nBK :dist\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        List<Reference> refs = table.getReferences("dist");
        assertEquals(2, refs.size());
    }

    // ── MAKE creates variable ───────────────────────────────────────────

    @Test
    void testMakeCreatesVariable() {
        analyzer.analyze("MAKE \"count 10", URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol var = table.resolve("count", "global");
        assertNotNull(var);
        assertEquals(SymbolType.VARIABLE, var.getType());
    }

    // ── LOCAL creates scoped variable ───────────────────────────────────

    @Test
    void testLocalCreatesVariable() {
        String code = """
                TO myproc
                LOCAL "temp
                MAKE "temp 5
                END
                """;
        analyzer.analyze(code, URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol local = table.resolve("temp", "myproc");
        assertNotNull(local);
        assertEquals(SymbolType.VARIABLE, local.getType());
    }

    // ── Positional lookup ───────────────────────────────────────────────

    @Test
    void testFindSymbolAtPosition() {
        analyzer.analyze("TO square :size\nFD :size\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        Symbol found = table.findSymbolAtPosition(URI, 0, 3);
        assertNotNull(found);
        assertEquals("square", found.getName());
    }

    // ── Empty program ───────────────────────────────────────────────────

    @Test
    void testEmptyProgram() {
        analyzer.analyze("", URI);
        SymbolTable table = analyzer.getSymbolTable();

        assertTrue(table.getAllSymbols().isEmpty());
    }

    // ── Re-analysis clears state ────────────────────────────────────────

    @Test
    void testReanalysisClears() {
        analyzer.analyze("TO square :size\nFD :size\nEND", URI);
        analyzer.analyze("TO circle :radius\nFD :radius\nEND", URI);
        SymbolTable table = analyzer.getSymbolTable();

        assertNull(table.resolve("square", "global"));
        assertNotNull(table.resolve("circle", "global"));
    }

    // ── Broken code ─────────────────────────────────────────────────────

    @Test
    void testBrokenCodeDoesNotCrash() {
        analyzer.analyze("TO square :size\nFD :size", URI);
        SymbolTable table = analyzer.getSymbolTable();
        assertNotNull(table.getAllSymbols());
    }

    @Test
    void testPartialCodeStillHighlights() {
        analyzer.analyze("FORWARD", URI);
        SymbolTable table = analyzer.getSymbolTable();
        assertNotNull(table);
    }

}

