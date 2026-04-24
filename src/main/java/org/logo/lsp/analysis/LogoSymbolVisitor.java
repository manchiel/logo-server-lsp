package org.logo.lsp.analysis;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.logo.lsp.parser.LogoBaseVisitor;
import org.logo.lsp.parser.LogoParser;
import org.logo.lsp.symbol.Reference;
import org.logo.lsp.symbol.Symbol;
import org.logo.lsp.symbol.SymbolTable;
import org.logo.lsp.symbol.SymbolType;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class LogoSymbolVisitor extends LogoBaseVisitor<Void> {

    private final SymbolTable symbolTable;
    private final String documentUri;
    private String currentScope = "global";

    private boolean collectingProcedures = false;

    public LogoSymbolVisitor(SymbolTable symbolTable, String documentUri) {
        this.symbolTable = symbolTable;
        this.documentUri = documentUri;
    }

    public void collectProcedures(LogoParser.ProgramContext tree) {
        collectingProcedures = true;
        visit(tree);
        collectingProcedures = false;
    }

    public void analyze(LogoParser.ProgramContext tree) {
        visit(tree);
    }

    @Override
    public Void visitProcedureDefinition(LogoParser.ProcedureDefinitionContext ctx) {
        Token nameToken = ctx.name.getStart();

        if (collectingProcedures) {
            List<String> paramNames = new ArrayList<>();
            if (ctx.parameterList() != null) {
                for (LogoParser.ParameterDeclarationContext param : ctx.parameterList().parameterDeclaration()) {
                    paramNames.add(param.NAME().getText());
                }
            }

            Symbol proc = new Symbol(
                    nameToken.getText(),
                    SymbolType.PROCEDURE,
                    documentUri,
                    tokenToRange(nameToken),
                    paramNames
            );
            symbolTable.define(proc, "global");
        } else {
            String previousScope = currentScope;
            currentScope = nameToken.getText().toLowerCase();

            if (ctx.parameterList() != null) {
                for (LogoParser.ParameterDeclarationContext param : ctx.parameterList().parameterDeclaration()) {
                    Token paramToken = param.NAME().getSymbol();
                    Symbol paramSymbol = new Symbol(
                            paramToken.getText(),
                            SymbolType.PARAMETER,
                            documentUri,
                            tokenToRange(paramToken)
                    );
                    symbolTable.define(paramSymbol, currentScope);
                }
            }

            if (ctx.procedureBody() != null) {
                visit(ctx.procedureBody());
            }

            currentScope = previousScope;
        }

        return null;
    }

    @Override
    public Void visitMakeCommand(LogoParser.MakeCommandContext ctx) {
        if (collectingProcedures) return null;

        String rawName = ctx.QUOTED_WORD().getText();
        String varName = rawName.substring(1);

        Token token = ctx.QUOTED_WORD().getSymbol();

        Symbol existing = symbolTable.resolve(varName, currentScope);
        if (existing == null || existing.getType() != SymbolType.PARAMETER) {

            Symbol varSymbol = new Symbol(
                    varName,
                    SymbolType.VARIABLE,
                    documentUri,
                    tokenToRange(token)
            );
            symbolTable.define(varSymbol, currentScope);
        }

        visit(ctx.expression());

        return null;
    }

    @Override
    public Void visitLocalCommand(LogoParser.LocalCommandContext ctx) {
        if (collectingProcedures) return null;

        String rawName = ctx.QUOTED_WORD().getText();
        String varName = rawName.substring(1);
        Token token = ctx.QUOTED_WORD().getSymbol();

        Symbol varSymbol = new Symbol(
                varName,
                SymbolType.VARIABLE,
                documentUri,
                tokenToRange(token)
        );
        symbolTable.define(varSymbol, currentScope);

        return null;
    }

    @Override
    public Void visitVariableReference(LogoParser.VariableReferenceContext ctx) {
        if (collectingProcedures) return null;

        Token colonToken = ctx.COLON().getSymbol();
        Token nameToken = ctx.NAME().getSymbol();
        Range range = new Range(
                new Position(colonToken.getLine() - 1, colonToken.getCharPositionInLine()),
                new Position(nameToken.getLine() - 1,
                        nameToken.getCharPositionInLine() + nameToken.getText().length())
        );

        Reference ref = new Reference(
                nameToken.getText().toLowerCase(),
                documentUri,
                range
        );
        symbolTable.addReference(ref);

        return null;
    }

    @Override
    public Void visitProcedureCall(LogoParser.ProcedureCallContext ctx) {
        if (collectingProcedures) return null;

        Token nameToken = ctx.name.getStart();
        Reference ref = new Reference(
                nameToken.getText().toLowerCase(),
                documentUri,
                tokenToRange(nameToken)
        );
        symbolTable.addReference(ref);

        for (LogoParser.ExpressionContext expr : ctx.expression()) {
            visit(expr);
        }

        return null;
    }

    private Range tokenToRange(Token token) {
        // LSP uses 0-based lines and columns but ANTLR uses 1-based lines and 0-based columns
        int line = token.getLine() - 1;
        int startChar = token.getCharPositionInLine();
        int endChar = startChar + token.getText().length();

        return new Range(
                new Position(line, startChar),
                new Position(line, endChar)
        );
    }
}