package org.logo.lsp.analysis;

import org.antlr.v4.runtime.*;
import org.logo.lsp.parser.LogoLexer;
import org.logo.lsp.parser.LogoParser;
import org.logo.lsp.symbol.SymbolTable;

public class DocumentAnalyzer {

    private final SymbolTable symbolTable = new SymbolTable();
    private LogoParser.ProgramContext parseTree;

    public void analyze(String text, String documentUri) {
        symbolTable.clear();

        CharStream input = CharStreams.fromString(text);
        LogoLexer lexer = new LogoLexer(input);

        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        LogoParser parser = new LogoParser(tokens);
        parser.removeErrorListeners();

        parseTree = parser.program();

        LogoSymbolVisitor visitor = new LogoSymbolVisitor(symbolTable, documentUri);
        visitor.collectProcedures(parseTree);

        visitor.analyze(parseTree);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public LogoParser.ProgramContext getParseTree() {
        return parseTree;
    }

    public CommonTokenStream getTokenStream(String text) {
        CharStream input = CharStreams.fromString(text);
        LogoLexer lexer = new LogoLexer(input);
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        return tokens;
    }
}