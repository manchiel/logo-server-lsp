package org.logo.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.concurrent.CompletableFuture;

public class LogoLanguageServer implements LanguageServer, LanguageClientAware {

    private LanguageClient client;
    private final LogoTextDocumentService textDocumentService;
    private final LogoWorkspaceService workspaceService;

    public LogoLanguageServer() {
        this.textDocumentService = new LogoTextDocumentService();
        this.workspaceService = new LogoWorkspaceService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();

        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);


        SemanticTokensWithRegistrationOptions semanticTokensOptions = new SemanticTokensWithRegistrationOptions();
        semanticTokensOptions.setFull(true);
        semanticTokensOptions.setLegend(LogoTextDocumentService.getSemanticTokensLegend());
        capabilities.setSemanticTokensProvider(semanticTokensOptions);

        capabilities.setDefinitionProvider(true);

        capabilities.setHoverProvider(true);

        InitializeResult result = new InitializeResult(capabilities);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public void initialized(InitializedParams params) {}

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        this.textDocumentService.connect(client);
    }
}
