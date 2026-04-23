package org.logo.lsp.server;

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

public class LogoLanguageServerLauncher {

    public static void main(String[] args) throws Exception {
        InputStream in = System.in;
        OutputStream out = System.out;

        System.setOut(new java.io.PrintStream(System.err, true));

        LogoLanguageServer server = new LogoLanguageServer();

        var launcher = LSPLauncher.createServerLauncher(server, in, out);
        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);

        Future<?> startListening = launcher.startListening();
        startListening.get();
    }
}