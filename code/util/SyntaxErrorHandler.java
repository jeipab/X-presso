package util;

import java.util.ArrayList;
import java.util.List;

public class SyntaxErrorHandler {
    private final List<SyntaxError> errors;
    private final boolean immediateLogging;
    
    public enum ErrorType {
        UNEXPECTED_TOKEN,
        MISSING_SEMICOLON,
        MISSING_PARENTHESIS,
        MISSING_BRACE,
        MISSING_BRACKET,
        INVALID_EXPRESSION,
        INVALID_STATEMENT,
        MISSING_IDENTIFIER,
        MISSING_OPERATOR,
        INVALID_DECLARATION,
        DUPLICATE_DECLARATION,
        MISSING_RETURN_STATEMENT
    }

    public static class SyntaxError {
        private final ErrorType type;
        private final String message;
        private final int line;
        private final int column;
        private final String expected;
        private final String found;
        private final String suggestion;

        public SyntaxError(ErrorType type, String message, int line, int column, 
                         String expected, String found, String suggestion) {
            this.type = type;
            this.message = message;
            this.line = line;
            this.column = column;
            this.expected = expected;
            this.found = found;
            this.suggestion = suggestion;
        }

        @Override
        public String toString() {
            StringBuilder error = new StringBuilder();
            error.append(String.format("Syntax Error at line %d, column %d: %s%n", line, column, type));
            error.append(String.format("Description: %s%n", message));
            if (expected != null) {
                error.append(String.format("Expected: %s%n", expected));
            }
            if (found != null) {
                error.append(String.format("Found: %s%n", found));
            }
            if (suggestion != null) {
                error.append(String.format("Suggestion: %s%n", suggestion));
            }
            return error.toString();
        }
    }

    public SyntaxErrorHandler(boolean immediateLogging) {
        this.errors = new ArrayList<>();
        this.immediateLogging = immediateLogging;
    }

    public void reportError(ErrorType type, String message, int line, int column,
                          String expected, String found, String suggestion) {
        SyntaxError error = new SyntaxError(type, message, line, column, expected, found, suggestion);
        errors.add(error);
        
        if (immediateLogging) {
            System.err.println(error);
        }
    }

    public void handleUnexpectedToken(String expected, String found, int line, int column) {
        reportError(
            ErrorType.UNEXPECTED_TOKEN,
            "Unexpected token encountered during parsing",
            line,
            column,
            expected,
            found,
            String.format("Replace '%s' with '%s'", found, expected)
        );
    }

    public void handleMissingSemicolon(int line, int column) {
        reportError(
            ErrorType.MISSING_SEMICOLON,
            "Missing semicolon at end of statement",
            line,
            column,
            ";",
            "",
            "Add a semicolon to terminate the statement"
        );
    }

    public void handleMissingParenthesis(boolean isOpening, int line, int column) {
        String expected = isOpening ? "(" : ")";
        reportError(
            ErrorType.MISSING_PARENTHESIS,
            String.format("Missing %s parenthesis", isOpening ? "opening" : "closing"),
            line,
            column,
            expected,
            "",
            String.format("Add %s parenthesis", isOpening ? "opening" : "closing")
        );
    }

    public void handleMissingBrace(boolean isOpening, int line, int column) {
        String expected = isOpening ? "{" : "}";
        reportError(
            ErrorType.MISSING_BRACE,
            String.format("Missing %s brace", isOpening ? "opening" : "closing"),
            line,
            column,
            expected,
            "",
            String.format("Add %s brace", isOpening ? "opening" : "closing")
        );
    }

    public void handleInvalidExpression(String expression, int line, int column) {
        reportError(
            ErrorType.INVALID_EXPRESSION,
            "Invalid expression syntax",
            line,
            column,
            "valid expression",
            expression,
            "Check expression syntax and operator precedence"
        );
    }

    public List<SyntaxError> getErrors() {
        return new ArrayList<>(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors() {
        if (errors.isEmpty()) {
            System.out.println("No syntax errors found.");
            return;
        }

        System.err.println("\nSyntax Errors:");
        for (SyntaxError error : errors) {
            System.err.println(error);
        }
    }

    public void clearErrors() {
        errors.clear();
    }
}

