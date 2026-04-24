package org.logo.lsp.provider;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logo.lsp.analysis.DocumentAnalyzer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiagnosticsProviderTest {

    private DiagnosticsProvider provider;
    private DocumentAnalyzer analyzer;
    private static final String URI = "file:///test.logo";

    @BeforeEach
    void setUp() {
        provider = new DiagnosticsProvider();
        analyzer = new DocumentAnalyzer();
    }

    private List<Diagnostic> getDiagnostics(String code) {
        analyzer.analyze(code, URI);
        return provider.provide(analyzer.getSymbolTable(), URI);
    }

    // ── Undefined procedure ─────────────────────────────────────────────

    @Test
    void testUndefinedProcedureCall() {
        List<Diagnostic> diags = getDiagnostics("notexist 100");

        assertEquals(1, diags.size());
        assertEquals(DiagnosticSeverity.Error, diags.get(0).getSeverity());
        assertTrue(diags.get(0).getMessage().contains("notexist"));
    }

    // ── Defined procedure — no error ────────────────────────────────────

    @Test
    void testDefinedProcedureNoError() {
        String code = """
                TO square :size
                REPEAT 4 [ FD :size RT 90 ]
                END
                square 100
                """;
        List<Diagnostic> diags = getDiagnostics(code);

        assertTrue(diags.isEmpty());
    }

    // ── Forward reference — no error ────────────────────────────────────

    @Test
    void testForwardReferenceNoError() {
        String code = """
                square 100
                TO square :size
                REPEAT 4 [ FD :size RT 90 ]
                END
                """;
        List<Diagnostic> diags = getDiagnostics(code);

        assertTrue(diags.isEmpty());
    }

    // ── Built-in commands — no error ────────────────────────────────────

    @Test
    void testBuiltinCommandsNotFlagged() {
        List<Diagnostic> diags = getDiagnostics("FD 100\nRT 90\nPENUP");

        assertTrue(diags.isEmpty());
    }

    // ── Procedure redefinition — warning ─────────────────────────────────

    @Test
    void testProcedureRedefinitionWarning() {
        String code = """
                TO square :size
                FD :size
                END
                TO square :side
                FD :side
                END
                """;
        List<Diagnostic> diags = getDiagnostics(code);

        assertEquals(1, diags.size());
        assertEquals(DiagnosticSeverity.Warning, diags.get(0).getSeverity());
        assertTrue(diags.get(0).getMessage().contains("already defined"));
    }

    // ── Multiple undefined — error for each ─────────────────────────────

    @Test
    void testMultipleUndefinedSymbols() {
        String code = """
                foo 10
                bar 20
                """;
        List<Diagnostic> diags = getDiagnostics(code);

        assertEquals(2, diags.size());
    }

    // ── Same undefined called twice — two errors ────────────────────────

    @Test
    void testSameUndefinedCalledTwice() {
        String code = """
                notexist 10
                notexist 20
                """;
        List<Diagnostic> diags = getDiagnostics(code);

        assertEquals(2, diags.size());
    }

    // ── Clean code — no diagnostics ─────────────────────────────────────

    @Test
    void testCleanCodeNoDiagnostics() {
        String code = """
                TO circle :radius
                REPEAT 36 [ FD :radius RT 10 ]
                END
                circle 5
                """;
        List<Diagnostic> diags = getDiagnostics(code);

        assertTrue(diags.isEmpty());
    }

    // ── Empty program — no diagnostics ──────────────────────────────────

    @Test
    void testEmptyProgramNoDiagnostics() {
        List<Diagnostic> diags = getDiagnostics("");

        assertTrue(diags.isEmpty());
    }

    // ── Source field is set ──────────────────────────────────────────────

    @Test
    void testDiagnosticSourceIsSet() {
        List<Diagnostic> diags = getDiagnostics("unknown 50");

        assertFalse(diags.isEmpty());
        assertEquals("logo-lsp", diags.get(0).getSource());
    }
}