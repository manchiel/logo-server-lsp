package org.logo.lsp.provider;

import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.lsp4j.SemanticTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logo.lsp.analysis.DocumentAnalyzer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticTokensProviderTest {

    private SemanticTokensProvider provider;
    private DocumentAnalyzer analyzer;

    private static final int TYPE_KEYWORD = 0;
    private static final int TYPE_FUNCTION = 1;
    private static final int TYPE_VARIABLE = 3;
    private static final int TYPE_NUMBER = 4;
    private static final int TYPE_STRING = 5;
    private static final int TYPE_OPERATOR = 6;
    private static final int TYPE_COMMENT = 7;

    @BeforeEach
    void setUp() {
        provider = new SemanticTokensProvider();
        analyzer = new DocumentAnalyzer();
    }

    private SemanticTokens getTokens(String code) {
        CommonTokenStream stream = analyzer.getTokenStream(code);
        return provider.provide(stream);
    }

    private int getTokenType(List<Integer> data, int tokenIndex) {
        return data.get(tokenIndex * 5 + 3);
    }

    private int getTokenLength(List<Integer> data, int tokenIndex) {
        return data.get(tokenIndex * 5 + 2);
    }

    @Test
    void testKeywordHighlighting() {
        SemanticTokens result = getTokens("FORWARD 100");
        List<Integer> data = result.getData();

        assertEquals(10, data.size()); // 2 tokens * 5

        assertEquals(TYPE_KEYWORD, getTokenType(data, 0));
        assertEquals(TYPE_NUMBER, getTokenType(data, 1));
    }

    @Test
    void testCaseInsensitiveKeyword() {
        SemanticTokens result = getTokens("forward 50");
        List<Integer> data = result.getData();

        assertEquals(TYPE_KEYWORD, getTokenType(data, 0));
        assertEquals(7, getTokenLength(data, 0)); // "forward" = 7 chars
    }

    @Test
    void testVariableHighlighting() {
        SemanticTokens result = getTokens("FD :size");
        List<Integer> data = result.getData();

        assertEquals(TYPE_KEYWORD, getTokenType(data, 0));
        assertEquals(TYPE_VARIABLE, getTokenType(data, 1)); // colon
    }

    @Test
    void testOperatorHighlighting() {
        SemanticTokens result = getTokens("FD 10 + 20");
        List<Integer> data = result.getData();

        assertEquals(TYPE_KEYWORD, getTokenType(data, 0));
        assertEquals(TYPE_NUMBER, getTokenType(data, 1));
        assertEquals(TYPE_OPERATOR, getTokenType(data, 2));
        assertEquals(TYPE_NUMBER, getTokenType(data, 3));
    }

    @Test
    void testStringHighlighting() {
        SemanticTokens result = getTokens("PRINT \"hello");
        List<Integer> data = result.getData();

        assertEquals(TYPE_KEYWORD, getTokenType(data, 0));
        assertEquals(TYPE_STRING, getTokenType(data, 1));
    }

    @Test
    void testCommentHighlighting() {
        SemanticTokens result = getTokens("FD 100 ; move forward");
        List<Integer> data = result.getData();

        assertEquals(TYPE_KEYWORD, getTokenType(data, 0));
        assertEquals(TYPE_NUMBER, getTokenType(data, 1));
        assertEquals(TYPE_COMMENT, getTokenType(data, 2));
    }

    @Test
    void testMultilineDeltas() {
        SemanticTokens result = getTokens("FD 100\nRT 90");
        List<Integer> data = result.getData();

        assertEquals(0, data.get(0)); // deltaLine = 0

        int rtIndex = 2;
        assertEquals(1, data.get(rtIndex * 5)); // deltaLine = 1
    }

    @Test
    void testProcedureDefinition() {
        SemanticTokens result = getTokens("TO square :size\nEND");
        List<Integer> data = result.getData();

        assertEquals(TYPE_KEYWORD, getTokenType(data, 0));  // TO
        assertEquals(TYPE_FUNCTION, getTokenType(data, 1));  // square
        assertEquals(TYPE_VARIABLE, getTokenType(data, 2));  // :
        assertEquals(TYPE_FUNCTION, getTokenType(data, 3));  // size (NAME token)
        assertEquals(TYPE_KEYWORD, getTokenType(data, 4));   // END
    }

    @Test
    void testEmptyProgram() {
        SemanticTokens result = getTokens("");
        assertTrue(result.getData().isEmpty());
    }
}