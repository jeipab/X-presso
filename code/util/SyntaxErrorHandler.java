package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import lexer.Token;
import parser.core.Parser;

public class SyntaxErrorHandler {
    private final List<SyntaxError> errors;
    private String currentFile;
    private final boolean immediateLogging;
    private final Stack<String> contextStack; // Tracks parsing context
    private static final int MAX_ERRORS = 25; // Prevent infinite error cascading
    private int errorCount;
    private Parser parser;

    /**
     * Represents different types of syntactic errors that can occur.
     */
    public enum ErrorType {
        UNEXPECTED_TOKEN,
        MISSING_TOKEN,
        UNMATCHED_DELIMITER,
        INVALID_SYNTAX_STRUCTURE,
        EMPTY_RULE,
        END_OF_INPUT_UNEXPECTED,
        INVALID_GRAMMAR_RULE,
        MISMATCHED_PARENTHESIS,
        INVALID_STATEMENT,
        SYNTAX_AMBIGUITY
    }

    private enum RecoveryStrategy {
        SKIP_TOKEN,        // Skip the problematic token
        INSERT_TOKEN,      // Insert missing token
        DELETE_TOKEN,      // Delete unexpected token
        PANIC_MODE        // Skip until synchronization point
    }


    public static class SyntaxError extends RuntimeException {
        private final ErrorType type;
        private final String message;
        private final int line;
        private final int column;
        private final String suggestion;
        private final String context;

        public SyntaxError(ErrorType type, String message, int line, int column, String suggestion, String context) {
            this.type = type;
            this.message = message;
            this.line = line;
            this.column = column;
            this.suggestion = suggestion;
            this.context = context;
        }

        public ErrorType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public String getSuggestion() {
            return suggestion;
        }

         public String getContext() {
            return context;
        }
        @Override
        public String toString() {
            StringBuilder error = new StringBuilder();
            error.append(String.format("Error at line %d, column %d: %s%n", line, column, type));
            error.append(String.format("Description: %s%n", message));
            if (suggestion != null && !suggestion.isEmpty()) {
                error.append(String.format("Suggestion: %s%n", suggestion));
            }
             if (context != null && !context.isEmpty()) {
                error.append(String.format("Context: %s%n", context));
            }
            return error.toString();
        }
    }

    public SyntaxErrorHandler() {
        this(true);
    }

    public SyntaxErrorHandler(Parser parser) {
        this.errors = new ArrayList<>();
        this.immediateLogging = true;
        this.contextStack = new Stack<>();
        this.errorCount = 0;
        this.parser = parser;
    }

    public SyntaxErrorHandler(boolean immediateLogging) {
        this.errors = new ArrayList<>();
        this.immediateLogging = immediateLogging;
        this.contextStack = new Stack<>();
        this.errorCount = 0;
    }

    /**
     * Records a syntactic error with optional recovery suggestion.
     */
    public void reportError(ErrorType type, String message, int line, int column, String suggestion) {
        if (errorCount >= MAX_ERRORS) {
            throw new RuntimeException("Maximum error limit reached. Stopping parsing.");
        }
    
        String context = contextStack.isEmpty() ? "" : contextStack.peek();
        SyntaxError error = new SyntaxError(type, message, line, column, suggestion, context);
        errors.add(error);
        errorCount++;
    
        if (immediateLogging) {
            System.err.println(error);
        }
    
        // Apply error recovery
        RecoveryStrategy strategy = determineRecoveryStrategy(type);
        applyRecoveryStrategy(strategy);
    }

    /**
     * Determines appropriate recovery strategy based on error type
     */
    private RecoveryStrategy determineRecoveryStrategy(ErrorType type) {
        return switch (type) {
            case UNEXPECTED_TOKEN -> RecoveryStrategy.SKIP_TOKEN;
            case MISSING_TOKEN -> RecoveryStrategy.INSERT_TOKEN;
            case UNMATCHED_DELIMITER, INVALID_SYNTAX_STRUCTURE -> RecoveryStrategy.PANIC_MODE;
            default -> RecoveryStrategy.SKIP_TOKEN;
        };
    }

    /**
     * Applies the selected recovery strategy
     */
    private void applyRecoveryStrategy(RecoveryStrategy strategy) {
        switch (strategy) {
            case SKIP_TOKEN -> {
            }
            case INSERT_TOKEN -> {
            }
            case PANIC_MODE -> synchronize();

            default -> throw new IllegalArgumentException("Unexpected value: " + strategy);
        }
        // Implementation for skipping token
        // Implementation for inserting missing token
            }

    /**
     * Synchronizes the parser state by skipping tokens until a safe point
     */
    private void synchronize() {
        while (!parser.atEnd()) {
            Token token = parser.advance();
            if (token.getLexeme().equals(";") || token.getLexeme().equals("{") || token.getLexeme().equals("}")) {
                return; // Stop skipping when reaching a statement boundary
            }
        }
    }

    /**
     * Pushes a new context onto the context stack
     */
    public void pushContext(String context) {
        contextStack.push(context);
    }

    /**
     * Pops the current context from the stack
     */
    public String popContext() {
        return contextStack.isEmpty() ? "" : contextStack.pop();
    }

    /**
     * Enhanced error handlers with context tracking
     */
    public void handleUnexpectedToken(Token token, Token expected, int line, int column) {
        String tokenValue = token != null ? token.getLexeme() : "null";
        String expectedValue = expected != null ? expected.getLexeme() : "null";
    
        reportError(
            ErrorType.UNEXPECTED_TOKEN,
            String.format("Unexpected token: '%s'. Expected: %s", tokenValue, expectedValue),
            line,
            column,
            "Check syntax and ensure correct token usage."
        );
    }

    /**
     * Handles missing token errors.
     */
    public void handleMissingToken(String expected, int line, int column) {
        reportError(
            ErrorType.MISSING_TOKEN,
            String.format("Missing token: '%s'", expected),
            line,
            column,
            "Add the missing token to complete the syntax structure"
        );
    }


    /**
     * Handles unmatched delimiter errors.
     */
    public void handleUnmatchedDelimiter(String delimiter, int line, int column) {
        reportError(
            ErrorType.UNMATCHED_DELIMITER,
            String.format("Unmatched delimiter: '%s'", delimiter),
            line,
            column,
            "Ensure all opening and closing delimiters are properly matched"
        );
    }

    /**
     * Handles invalid syntax structure errors.
     */
    public void handleInvalidSyntaxStructure(String structure, int line, int column) {
        reportError(
            ErrorType.INVALID_SYNTAX_STRUCTURE,
            String.format("Invalid syntax structure: '%s'", structure),
            line,
            column,
            "Check the grammar rules for the correct structure"
        );
    }

    /**
     * Handles end-of-input unexpected errors.
     */
    public void handleUnexpectedEndOfInput(int line, int column) {
        reportError(
            ErrorType.END_OF_INPUT_UNEXPECTED,
            "Unexpected end of input encountered",
            line,
            column,
            "Ensure all constructs are properly closed or completed"
        );
    }

    /**
     * Returns all recorded errors.
     */

    public String getErrorStatistics() {
        if (errors.isEmpty()) {
            return "No errors found.";
        }

        StringBuilder stats = new StringBuilder();
        stats.append(String.format("Total errors: %d%n", errors.size()));
        
        // Count errors by type
        java.util.Map<ErrorType, Integer> errorCounts = new java.util.HashMap<>();
        for (SyntaxError error : errors) {
            errorCounts.merge(error.getType(), 1, Integer::sum);
        }

        stats.append("Error breakdown:\n");
        errorCounts.forEach((type, count) -> 
            stats.append(String.format("- %s: %d%n", type, count)));

        return stats.toString();
    }

    public List<SyntaxError> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Returns whether any errors have been recorded.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Prints all recorded errors.
     */
    public void printErrors() {
        if (errors.isEmpty()) {
            System.out.println("No syntactic errors found.");
            return;
        }

        System.err.println("\nSyntactic Errors:");
        for (SyntaxError error : errors) {
            System.err.println(error);
        }
    }

    /**
     * Clears all recorded errors.
     */
    public void clearErrors() {
        errors.clear();
    }

    /**
     * Sets the current file being processed.
     */
    public void setCurrentFile(String filename) {
        this.currentFile = filename;
    }

    /**
     * Gets the current file being processed.
     */
    public String getCurrentFile() {
        return this.currentFile;
    }
}
