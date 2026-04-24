package org.logo.lsp.symbol;

import org.eclipse.lsp4j.Range;
import java.util.*;

public class SymbolTable {

    private final Map<String, List<Symbol>> scopes = new HashMap<>();
    private final Map<String, List<Reference>> referencesByKey = new HashMap<>();
    private final Map<Integer, List<Symbol>> symbolsByLine = new HashMap<>();
    private final Map<Integer, List<Reference>> referencesByLine = new HashMap<>();

    public void define(Symbol symbol, String scopeName) {
        scopes.computeIfAbsent(scopeName, k -> new ArrayList<>()).add(symbol);
        int line = symbol.getRange().getStart().getLine();
        symbolsByLine.computeIfAbsent(line, k -> new ArrayList<>()).add(symbol);
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
        referencesByKey.computeIfAbsent(ref.lookupKey(), k -> new ArrayList<>()).add(ref);

        int line = ref.range().getStart().getLine();
        referencesByLine.computeIfAbsent(line, k -> new ArrayList<>()).add(ref);
    }

    public List<Reference> getReferences(String lookupKey) {
        String key = lookupKey.toLowerCase();
        return referencesByKey.getOrDefault(key, List.of());
    }

    public Symbol findSymbolAtPosition(String documentUri, int line, int character) {
        List<Symbol> lineSymbols = symbolsByLine.get(line);
        if (lineSymbols == null) {
            return null;
        }
        for (Symbol symbol : lineSymbols) {
            if (symbol.getDocumentUri().equals(documentUri)
                    && containsPosition(symbol.getRange(), line, character)) {
                return symbol;
            }
        }
        return null;
    }

    public Reference findReferenceAtPosition(String documentUri, int line, int character) {
        List<Reference> lineRefs = referencesByLine.get(line);
        if (lineRefs == null) {
            return null;
        }
        for (Reference ref : lineRefs) {
            if (ref.documentUri().equals(documentUri)
                    && containsPosition(ref.range(), line, character)) {
                return ref;
            }
        }
        return null;
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
        referencesByKey.clear();
        symbolsByLine.clear();
        referencesByLine.clear();
    }
}