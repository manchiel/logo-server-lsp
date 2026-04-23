package org.logo.lsp.server;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

// LSP4J requires me to provide an implementation but for this project we don't need workspace features

public class LogoWorkspaceService implements WorkspaceService {

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // No configuration support needed
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // No file watching needed
    }
}
