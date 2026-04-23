package org.logo.lsp.symbol;

import org.eclipse.lsp4j.Range;
import java.util.*;

public class SymbolTable {


    private final Map<String, List<Symbol>> scopes = new HashMap<>();
    private final List<Reference> references = new ArrayList<>();

    public void define(Symbol symbol, String scopeName) {
        if (!scopes.containsKey(scopeName)) {
            scopes.put(scopeName, new ArrayList<>());
        }
    }

    public Symbol resolve(String name, String scopeName) {
        String lookupKey = name.toLowerCase();

        Symbol local = findInScope(lookupKey, scopeName);
        if (local != null) {
            return local;
        }
        if (!scopeName.equals("global")) {
            return findInScope(lookupKey, "global");
        }

        return null;
    }

    private Symbol findInScope(String lookupKey, String scopeName) {
        List<Symbol> symbols = scopes.get(scopeName);
        if (symbols == null) {
            return null;
        }
        for (int i = symbols.size() - 1; i >= 0; i--) {
            if (symbols.get(i).getLookupKey().equals(lookupKey)) {
                return symbols.get(i);
            }
        }
        return null;
    }

    public void addReference(Reference ref) {
        references.add(ref);
    }

    public List<Reference> getReferences(String lookupKey) {
        String key = lookupKey.toLowerCase();
        List<Reference> result = new ArrayList<>();
        for (Reference ref : references) {
            if (ref.lookupKey().equals(key)) {
                result.add(ref);
            }
        }
        return result;
    }

    public List<Symbol> getSymbolsInScope(String scopeName) {
        return scopes.getOrDefault(scopeName, List.of());
    }

    public List<Symbol> getAllSymbols() {
        List<Symbol> all = new ArrayList<>();
        for (List<Symbol> symbols : scopes.values()) {
            all.addAll(symbols);
        }
        return all;
    }

    public Symbol findSymbolAtPosition(String documentUri, int line, int character) {
        for (Symbol symbol : getAllSymbols()) {
            if (symbol.getDocumentUri().equals(documentUri) && containsPosition(symbol.getRange(), line, character)) {
                return symbol;
            }
        }
        return null;
    }

    public Reference findReferenceAtPosition(String documentUri, int line, int character) {
        for (Reference ref : references) {
            if (ref.documentUri().equals(documentUri) && containsPosition(ref.range(), line, character)) {
                return ref;
            }
        }
        return null;
    }

    private boolean containsPosition(Range range, int line, int character) {
        if (line < range.getStart().getLine() || line > range.getEnd().getLine()) {
            return false;
        }
        if (line == range.getStart().getLine() && character < range.getStart().getCharacter()) {
            return false;
        }
        if (line == range.getEnd().getLine() && character > range.getEnd().getCharacter()) {
            return false;
        }
        return true;
    }

    public void clear() {
        scopes.clear();
        references.clear();
    }
}