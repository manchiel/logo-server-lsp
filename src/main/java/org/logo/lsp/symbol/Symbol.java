package org.logo.lsp.symbol;

import org.eclipse.lsp4j.Range;
import java.util.List;

public class Symbol {

    private final String name;
    private final SymbolType type;
    private final String documentUri;
    private final Range range;
    private final List<String> parameterNames;

    public Symbol(String name, SymbolType type, String documentUri, Range range) {
        this(name, type, documentUri, range, List.of());
    }

    public Symbol(String name, SymbolType type, String documentUri, Range range,
                  List<String> parameterNames) {
        this.name = name;
        this.type = type;
        this.documentUri = documentUri;
        this.range = range;
        this.parameterNames = parameterNames;
    }

    public String getName() {
        return name;
    }

    public String getLookupKey() {
        return name.toLowerCase();
    }

    public SymbolType getType() {
        return type;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public Range getRange() {
        return range;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public int getParameterCount() {
        return parameterNames.size();
    }
}