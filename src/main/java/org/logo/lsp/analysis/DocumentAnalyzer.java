package org.logo.lsp.analysis;

import org.antlr.v4.runtime.*;
import org.logo.lsp.parser.LogoLexer;
import org.logo.lsp.parser.LogoParser;
import org.logo.lsp.symbol.SymbolTable;
import org.logo.lsp.parser.LogoParser;
import org.eclipse.lsp4j.Diagnostic;
import java.util.ArrayList;
import java.util.List;



public class DocumentAnalyzer {

    private final SymbolTable symbolTable = new SymbolTable();
    private LogoParser.ProgramContext parseTree;
    private final List<Diagnostic> syntaxErrors = new ArrayList<>();


    public void analyze(String text, String documentUri) {
        symbolTable.clear();
        syntaxErrors.clear();

        CharStream input = CharStreams.fromString(text);
        LogoLexer lexer = new LogoLexer(input);
        lexer.removeErrorListeners();

        LogoErrorListener errorListener = new LogoErrorListener();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        LogoParser parser = new LogoParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        parseTree = parser.program();

        syntaxErrors.addAll(errorListener.getErrors());

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

    public String getScopeAtPosition(int line) {
        if (parseTree == null) return "global";

        for (LogoParser.LineContext lineCtx : parseTree.line()) {
            for (LogoParser.StatementContext stmt : lineCtx.statement()) {
                if (stmt.procedureDefinition() != null) {
                    LogoParser.ProcedureDefinitionContext proc = stmt.procedureDefinition();
                    int startLine = proc.getStart().getLine() - 1;
                    int endLine = proc.getStop().getLine() - 1;

                    if (line >= startLine && line <= endLine) {
                        return proc.name.getText().toLowerCase();
                    }
                }
            }
        }

        return "global";
    }

    public List<Diagnostic> getSyntaxErrors() {
        return syntaxErrors;
    }


}