package org.logo.lsp.analysis;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;

public class LogoErrorListener extends BaseErrorListener {

    private final List<Diagnostic> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg,
                            RecognitionException e) {
        Diagnostic diag = new Diagnostic();
        diag.setRange(new Range(
                new Position(line - 1, charPositionInLine),
                new Position(line - 1, charPositionInLine + 1)
        ));
        diag.setSeverity(DiagnosticSeverity.Error);
        diag.setSource("logo-lsp");
        diag.setMessage("Syntax error: " + msg);
        errors.add(diag);
    }

    public List<Diagnostic> getErrors() {
        return errors;
    }
}
