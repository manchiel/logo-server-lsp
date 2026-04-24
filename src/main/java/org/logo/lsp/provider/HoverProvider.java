package org.logo.lsp.provider;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.logo.lsp.symbol.Reference;
import org.logo.lsp.symbol.Symbol;
import org.logo.lsp.symbol.SymbolTable;

import java.util.List;

public class HoverProvider {

    public Hover provide(SymbolTable symbolTable, String documentUri,
                         Position position, String currentScope) {
        int line = position.getLine();
        int character = position.getCharacter();

        Reference ref = symbolTable.findReferenceAtPosition(documentUri, line, character);
        if (ref != null) {
            Symbol symbol = symbolTable.resolve(ref.lookupKey(), currentScope);
            if (symbol != null) {
                return buildHover(symbol, currentScope);
            }
        }

        Symbol symbol = symbolTable.findSymbolAtPosition(documentUri, line, character);
        if (symbol != null) {
            return buildHover(symbol, currentScope);
        }

        return null;
    }

    private Hover buildHover(Symbol symbol, String currentScope) {
        String content = switch (symbol.getType()) {
            case PROCEDURE -> buildProcedureHover(symbol);
            case PARAMETER -> buildParameterHover(symbol, currentScope);
            case VARIABLE -> buildVariableHover(symbol, currentScope);
        };

        MarkupContent markup = new MarkupContent();
        markup.setKind(MarkupKind.MARKDOWN);
        markup.setValue(content);

        return new Hover(markup);
    }

    private String buildProcedureHover(Symbol symbol) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Procedure** `").append(symbol.getName()).append("`\n\n");

        List<String> params = symbol.getParameterNames();
        if (params.isEmpty()) {
            sb.append("No parameters");
        } else {
            sb.append("Parameters: ");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("`:").append(params.get(i)).append("`");
            }
        }

        sb.append("\n\n");
        sb.append("```logo\nTO ").append(symbol.getName());
        for (String param : params) {
            sb.append(" :").append(param);
        }
        sb.append("\n```");

        return sb.toString();
    }

    private String buildParameterHover(Symbol symbol, String scope) {
        return "**Parameter** `:" + symbol.getName() + "`\n\n" +
                "Scope: procedure `" + scope + "`";
    }

    private String buildVariableHover(Symbol symbol, String scope) {
        String scopeDesc = scope.equals("global") ? "global" : "procedure `" + scope + "`";
        return "**Variable** `" + symbol.getName() + "`\n\n" +
                "Scope: " + scopeDesc;
    }
}