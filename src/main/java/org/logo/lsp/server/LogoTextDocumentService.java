package org.logo.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class LogoTextDocumentService implements TextDocumentService {

    private LanguageClient client;

    public void connect(LanguageClient client) {
        this.client = client;
    }

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
        // TODO: Parse document and publish diagnostics
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        // TODO: Re-parse document and publish diagnostics
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO: Clear diagnostics for this document
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // No action needed — we re-analyze on every change
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(
            SemanticTokensParams params) {
        // TODO: Wire to SemanticTokensProvider
        return CompletableFuture.completedFuture(new SemanticTokens(new ArrayList<>()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
            DefinitionParams params) {
        // TODO: Wire to DefinitionProvider
        return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        // TODO: Wire to HoverProvider
        return CompletableFuture.completedFuture(null);
    }
}