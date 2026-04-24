package org.logo.lsp.provider;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.SemanticTokens;
import org.logo.lsp.parser.LogoLexer;
import org.logo.lsp.symbol.Reference;
import org.logo.lsp.symbol.Symbol;
import org.logo.lsp.symbol.SymbolTable;
import org.logo.lsp.symbol.SymbolType;

import java.util.ArrayList;
import java.util.List;

public class SemanticTokensProvider {

    private static final int TYPE_KEYWORD = 0;
    private static final int TYPE_FUNCTION = 1;
    private static final int TYPE_PARAMETER = 2;
    private static final int TYPE_VARIABLE = 3;
    private static final int TYPE_NUMBER = 4;
    private static final int TYPE_STRING = 5;
    private static final int TYPE_OPERATOR = 6;
    private static final int TYPE_COMMENT = 7;

    public SemanticTokens provide(CommonTokenStream tokenStream, SymbolTable symbolTable, String uri) {
        List<Integer> data = new ArrayList<>();

        int prevLine = 0;
        int prevColumn = 0;

        for (Token token : tokenStream.getTokens()) {
            int antlrType = token.getType();
            int tokenType = mapTokenType(antlrType);

            if (tokenType == -1) {
                continue;
            }

            if (antlrType == LogoLexer.NAME) {
                int line = token.getLine() - 1;
                int charPos = token.getCharPositionInLine();
                
                Reference ref = symbolTable.findReferenceAtPosition(uri, line, charPos);
                if (ref != null) {
                    Symbol resolved = symbolTable.resolve(ref.lookupKey(), ref.scope());
                    if (resolved != null) {
                        if (resolved.getType() == SymbolType.PARAMETER) {
                            tokenType = TYPE_PARAMETER;
                        } else if (resolved.getType() == SymbolType.VARIABLE) {
                            tokenType = TYPE_VARIABLE;
                        } else if (resolved.getType() == SymbolType.PROCEDURE) {
                            tokenType = TYPE_FUNCTION;
                        }
                    }
                } else {
                    Symbol sym = symbolTable.findSymbolAtPosition(uri, line, charPos);
                    if (sym != null) {
                        if (sym.getType() == SymbolType.PARAMETER) {
                            tokenType = TYPE_PARAMETER;
                        } else if (sym.getType() == SymbolType.VARIABLE) {
                            tokenType = TYPE_VARIABLE;
                        } else if (sym.getType() == SymbolType.PROCEDURE) {
                            tokenType = TYPE_FUNCTION;
                        }
                    }
                }
            }

            int line = token.getLine() - 1;
            int column = token.getCharPositionInLine();
            int length = token.getText().length();

            int deltaLine = line - prevLine;
            int deltaColumn = (deltaLine == 0) ? column - prevColumn : column;

            data.add(deltaLine);
            data.add(deltaColumn);
            data.add(length);
            data.add(tokenType);
            data.add(0);

            prevLine = line;
            prevColumn = column;
        }

        return new SemanticTokens(data);
    }

    private int mapTokenType(int antlrType) {
        return switch (antlrType) {
            case LogoLexer.TO,
                 LogoLexer.END,
                 LogoLexer.FORWARD,
                 LogoLexer.BACK,
                 LogoLexer.LEFT,
                 LogoLexer.RIGHT,
                 LogoLexer.PENUP,
                 LogoLexer.PENDOWN,
                 LogoLexer.HIDETURTLE,
                 LogoLexer.SHOWTURTLE,
                 LogoLexer.HOME,
                 LogoLexer.CLEARSCREEN,
                 LogoLexer.SETXY,
                 LogoLexer.REPEAT,
                 LogoLexer.IF,
                 LogoLexer.IFELSE,
                 LogoLexer.STOP,
                 LogoLexer.OUTPUT,
                 LogoLexer.PRINT,
                 LogoLexer.MAKE,
                 LogoLexer.LOCAL -> TYPE_KEYWORD;

            case LogoLexer.PLUS,
                 LogoLexer.MINUS,
                 LogoLexer.MULTIPLY,
                 LogoLexer.DIVIDE,
                 LogoLexer.EQUALS,
                 LogoLexer.NOTEQUALS,
                 LogoLexer.LESSTHAN,
                 LogoLexer.GREATERTHAN -> TYPE_OPERATOR;

            case LogoLexer.NUMBER -> TYPE_NUMBER;
            case LogoLexer.QUOTED_WORD -> TYPE_STRING;

            case LogoLexer.COLON -> TYPE_VARIABLE;

            case LogoLexer.NAME -> TYPE_FUNCTION;

            case LogoLexer.COMMENT -> TYPE_COMMENT;

            default -> -1;
        };
    }
}