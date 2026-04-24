package org.logo.lsp.provider;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.logo.lsp.symbol.Reference;
import org.logo.lsp.symbol.Symbol;
import org.logo.lsp.symbol.SymbolTable;
import org.logo.lsp.symbol.SymbolType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiagnosticsProvider {

    public List<Diagnostic> provide(SymbolTable symbolTable, String documentUri) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        checkUndefinedReferences(symbolTable, documentUri, diagnostics);
        checkProcedureRedefinition(symbolTable, diagnostics);

        return diagnostics;
    }

    private void checkUndefinedReferences(SymbolTable symbolTable,
                                          String documentUri,
                                          List<Diagnostic> diagnostics) {
        for (List<Reference> refs : symbolTable.getAllReferencesByKey().values()) {
            if (refs.isEmpty()) continue;

            // Resolve once per symbol name, not once per reference
            Reference first = refs.get(0);
            Symbol resolved = symbolTable.resolve(first.lookupKey(), "global");

            if (resolved == null && !isBuiltinCommand(first.lookupKey())) {
                for (Reference ref : refs) {
                    if (!ref.documentUri().equals(documentUri)) continue;

                    Diagnostic diag = new Diagnostic();
                    diag.setRange(ref.range());
                    diag.setSeverity(DiagnosticSeverity.Error);
                    diag.setSource("logo-lsp");
                    diag.setMessage("Undefined symbol: '" + ref.lookupKey() + "'");
                    diagnostics.add(diag);
                }
            }
        }
    }

    private void checkProcedureRedefinition(SymbolTable symbolTable,
                                            List<Diagnostic> diagnostics) {
        List<Symbol> globals = symbolTable.getSymbolsInScope("global");
        Set<String> seen = new HashSet<>();

        for (Symbol symbol : globals) {
            if (symbol.getType() != SymbolType.PROCEDURE) {
                continue;
            }

            String key = symbol.getLookupKey();
            if (seen.contains(key)) {
                Diagnostic diag = new Diagnostic();
                diag.setRange(symbol.getRange());
                diag.setSeverity(DiagnosticSeverity.Warning);
                diag.setSource("logo-lsp");
                diag.setMessage("Procedure '" + symbol.getName()
                        + "' is already defined. This definition replaces the previous one.");
                diagnostics.add(diag);
            }
            seen.add(key);
        }
    }

    private boolean isBuiltinCommand(String name) {
        return Set.of(
                "forward", "fd", "back", "bk", "left", "lt",
                "right", "rt", "penup", "pu", "pendown", "pd",
                "hideturtle", "ht", "showturtle", "st",
                "home", "clearscreen", "cs", "setxy",
                "repeat", "if", "ifelse", "stop", "output", "op",
                "print", "make", "local", "setpencolor", "setpc"
        ).contains(name);
    }
}