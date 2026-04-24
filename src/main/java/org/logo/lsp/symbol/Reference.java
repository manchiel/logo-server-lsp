package org.logo.lsp.symbol;
import org.eclipse.lsp4j.Range;

public record Reference(String lookupKey, String documentUri, Range range, String scope) {}
