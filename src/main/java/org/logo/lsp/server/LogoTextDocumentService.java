package org.logo.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import org.logo.lsp.analysis.DocumentAnalyzer;
import org.logo.lsp.provider.SemanticTokensProvider;
import org.antlr.v4.runtime.CommonTokenStream;
import java.util.HashMap;
import java.util.Map;

import org.logo.lsp.provider.DefinitionProvider;
import org.logo.lsp.provider.HoverProvider;
import org.logo.lsp.provider.DiagnosticsProvider;





public class LogoTextDocumentService implements TextDocumentService {

    private LanguageClient client;

    public void connect(LanguageClient client) {
        this.client = client;
    }
    private final Map<String, DocumentAnalyzer> analyzers = new HashMap<>();
    private final SemanticTokensProvider semanticTokensProvider = new SemanticTokensProvider();
    private final Map<String, String> documentTexts = new HashMap<>();
    private final DefinitionProvider definitionProvider = new DefinitionProvider();
    private final HoverProvider hoverProvider = new HoverProvider();
    private final DiagnosticsProvider diagnosticsProvider = new DiagnosticsProvider();



    public static SemanticTokensLegend getSemanticTokensLegend() {
        List<String> tokenTypes = List.of(
                "keyword",
                "function",
                "parameter",
                "variable",
                "number",
                "string",
                "operator",
                "comment"
        );
        List<String> tokenModifiers = List.of(
                "declaration",
                "definition"
        );
        return new SemanticTokensLegend(tokenTypes, tokenModifiers);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();

        DocumentAnalyzer analyzer = new DocumentAnalyzer();
        analyzer.analyze(text, uri);
        analyzers.put(uri, analyzer);
        documentTexts.put(uri, text);
        analyzeAndPublishDiagnostics(uri);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getContentChanges().get(0).getText();

        DocumentAnalyzer analyzer = analyzers.get(uri);
        if (analyzer == null) {
            analyzer = new DocumentAnalyzer();
            analyzers.put(uri, analyzer);
        }
        analyzer.analyze(text, uri);
        documentTexts.put(uri, text);
        analyzeAndPublishDiagnostics(uri);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        analyzers.remove(uri);
        documentTexts.remove(uri);

        if (client != null) {
            client.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<>()));
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // No action needed — we re-analyze on every change
    }

    private String getDocumentText(String uri) {
        return documentTexts.get(uri);
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        String uri = params.getTextDocument().getUri();
        DocumentAnalyzer analyzer = analyzers.get(uri);

        if (analyzer == null) {
            return CompletableFuture.completedFuture(new SemanticTokens(new ArrayList<>()));
        }

        String text = getDocumentText(uri);
        if (text == null) {
            return CompletableFuture.completedFuture(new SemanticTokens(new ArrayList<>()));
        }

        CommonTokenStream tokens = analyzer.getTokenStream(text);
        SemanticTokens result = semanticTokensProvider.provide(tokens, analyzer.getSymbolTable(), uri);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(
            DeclarationParams params) {
        return definition(new DefinitionParams(params.getTextDocument(), params.getPosition()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
            DefinitionParams params) {
        String uri = params.getTextDocument().getUri();
        DocumentAnalyzer analyzer = analyzers.get(uri);

        if (analyzer == null) {
            return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
        }

        int line = params.getPosition().getLine();
        String scope = analyzer.getScopeAtPosition(line);

        Location location = definitionProvider.provide(
                analyzer.getSymbolTable(), uri, params.getPosition(), scope);

        List<Location> result = new ArrayList<>();
        if (location != null) {
            result.add(location);
        }

        return CompletableFuture.completedFuture(Either.forLeft(result));
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        String uri = params.getTextDocument().getUri();
        DocumentAnalyzer analyzer = analyzers.get(uri);

        if (analyzer == null) {
            return CompletableFuture.completedFuture(null);
        }

        int line = params.getPosition().getLine();
        String scope = analyzer.getScopeAtPosition(line);

        Hover hover = hoverProvider.provide(
                analyzer.getSymbolTable(), uri, params.getPosition(), scope);

        return CompletableFuture.completedFuture(hover);
    }

    private void analyzeAndPublishDiagnostics(String uri) {
        DocumentAnalyzer analyzer = analyzers.get(uri);
        if (analyzer == null || client == null) {
            return;
        }

        List<Diagnostic> diagnostics = diagnosticsProvider.provide(
                analyzer.getSymbolTable(), uri);

        client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    }


}