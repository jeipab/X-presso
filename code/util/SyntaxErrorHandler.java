package util;

import java.util.ArrayList;
import java.util.List;

import lexer.Token;
import parser.RDP;

public class SyntaxErrorHandler {
    private final List<SyntaxError> errors;

    public SyntaxErrorHandler(RDP rdp) {
        this.errors = new ArrayList<>();
    }

    public static class SyntaxError {
        private final String message;
        private final int line;
        private final int column;
        private final String suggestion;

        public SyntaxError(String message, int line, int column, String suggestion) {
            this.message = message;
            this.line = line;
            this.column = column;
            this.suggestion = suggestion;
        }

        @Override
        public String toString() {
            return String.format("Error at line %d, column %d: %s\nSuggestion: %s",
                    line, column, message, suggestion);
        }
    }

    // Report an error
    public void reportError(String message, int line, int column, String suggestion) {
        errors.add(new SyntaxError(message, line, column, suggestion));
    }

    // Print all errors
    public void printErrors() {
        if (errors.isEmpty()) {
            System.out.println("No syntax errors found.");
        } else {
            System.err.println("\nSyntax Errors:");
            for (SyntaxError error : errors) {
                System.err.println(error);
            }
        }
    }

    // Check if errors exist
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void handleUnexpectedToken(Token peek, Object object, int line, int column) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleUnexpectedToken'");
    }
}
