package org.logo.lsp.provider;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logo.lsp.analysis.DocumentAnalyzer;
import org.logo.lsp.symbol.SymbolTable;

import static org.junit.jupiter.api.Assertions.*;

class DefinitionProviderTest {

    private DefinitionProvider provider;
    private DocumentAnalyzer analyzer;
    private static final String URI = "file:///test.logo";

    @BeforeEach
    void setUp() {
        provider = new DefinitionProvider();
        analyzer = new DocumentAnalyzer();
    }

    private Location getDefinition(String code, int line, int character) {
        analyzer.analyze(code, URI);
        SymbolTable table = analyzer.getSymbolTable();
        String scope = analyzer.getScopeAtPosition(line);
        return provider.provide(table, URI, new Position(line, character), scope);
    }

    // ── Procedure call → procedure definition ───────────────────────────

    @Test
    void testGoToProcedureDefinition() {
        String code = """
                TO square :size
                REPEAT 4 [ FD :size RT 90 ]
                END
                square 100
                """;
        // "square" call is on line 3, column 0
        Location loc = getDefinition(code, 3, 0);

        assertNotNull(loc);
        // Should jump to line 0 where TO square is defined
        assertEquals(0, loc.getRange().getStart().getLine());
    }

    // ── Variable reference → parameter declaration ──────────────────────

    @Test
    void testGoToParameterDefinition() {
        String code = """
                TO square :size
                FD :size
                END
                """;
        // :size reference on line 1 — click on "size" part (column 4)
        Location loc = getDefinition(code, 1, 4);

        assertNotNull(loc);
        // Should jump to line 0 where :size is declared as parameter
        assertEquals(0, loc.getRange().getStart().getLine());
    }

    // ── Click on colon of variable reference ────────────────────────────

    @Test
    void testGoToDefinitionFromColon() {
        String code = """
                TO square :size
                FD :size
                END
                """;
        // Click on the ":" of :size on line 1 (column 3)
        Location loc = getDefinition(code, 1, 3);

        assertNotNull(loc);
        assertEquals(0, loc.getRange().getStart().getLine());
    }

    // ── Forward reference ───────────────────────────────────────────────

    @Test
    void testForwardReferenceGoToDefinition() {
        String code = """
                circle 50
                TO circle :radius
                FD :radius
                END
                """;
        // "circle" call on line 0
        Location loc = getDefinition(code, 0, 0);

        assertNotNull(loc);
        // Should jump to line 1 where TO circle is defined
        assertEquals(1, loc.getRange().getStart().getLine());
    }

    // ── Case insensitive ────────────────────────────────────────────────

    @Test
    void testCaseInsensitiveGoToDefinition() {
        String code = """
                TO Square :size
                FD :size
                END
                SQUARE 100
                """;
        // "SQUARE" call on line 3
        Location loc = getDefinition(code, 3, 0);

        assertNotNull(loc);
        assertEquals(0, loc.getRange().getStart().getLine());
    }

    // ── Variable shadowing ──────────────────────────────────────────────

    @Test
    void testShadowedVariableGoesToLocalDefinition() {
        String code = """
                MAKE "x 10
                TO myproc :x
                FD :x
                END
                """;
        // :x on line 2 inside myproc — should go to parameter on line 1, not global
        Location loc = getDefinition(code, 2, 4);

        assertNotNull(loc);
        assertEquals(1, loc.getRange().getStart().getLine());
    }

    // ── Click on declaration itself ─────────────────────────────────────

    @Test
    void testClickOnDeclarationReturnsSelf() {
        String code = """
                TO square :size
                FD :size
                END
                """;
        // Click on "square" in the TO line (line 0, column 3)
        Location loc = getDefinition(code, 0, 3);

        assertNotNull(loc);
        assertEquals(0, loc.getRange().getStart().getLine());
    }

    // ── Undefined symbol ────────────────────────────────────────────────

    @Test
    void testUndefinedSymbolReturnsNull() {
        String code = "FD 100";
        // Click on "FD" — it's a keyword, not in symbol table
        Location loc = getDefinition(code, 0, 0);

        assertNull(loc);
    }

    // ── MAKE variable ───────────────────────────────────────────────────

    @Test
    void testGoToMakeVariableDefinition() {
        String code = """
                MAKE "count 0
                FD :count
                """;
        // :count on line 1
        Location loc = getDefinition(code, 1, 4);

        assertNotNull(loc);
        assertEquals(0, loc.getRange().getStart().getLine());
    }
}