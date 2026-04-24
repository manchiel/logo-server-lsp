package org.logo.lsp.provider;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.logo.lsp.symbol.Reference;
import org.logo.lsp.symbol.Symbol;
import org.logo.lsp.symbol.SymbolTable;

public class DefinitionProvider {

    public Location provide(SymbolTable symbolTable, String documentUri,
                            Position position, String currentScope) {
        int line = position.getLine();
        int character = position.getCharacter();

        Reference ref = symbolTable.findReferenceAtPosition(documentUri, line, character);
        if (ref != null) {
            Symbol symbol = symbolTable.resolve(ref.lookupKey(), ref.scope());
            if (symbol != null) {
                return new Location(symbol.getDocumentUri(), symbol.getRange());
            }
        }

        Symbol symbol = symbolTable.findSymbolAtPosition(documentUri, line, character);
        if (symbol != null) {
            return new Location(symbol.getDocumentUri(), symbol.getRange());
        }

        return null;
    }
}