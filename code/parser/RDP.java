package parser;

import lexer.Token;
import lexer.TokenType;
import util.SyntaxErrorHandler;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class RDP {
    private final List<Token> tokens;
    private final SyntaxErrorHandler errorHandler;
    private int current = 0;

    // Parser States
    private enum State {
        START,
        ACCESS_MODIFIER,
        NON_ACCESS_MODIFIER,
        CLASS_KEYWORD,
        CLASS_NAME,
        OPEN_BRACE,
        CLASS_BODY,
        CLOSE_BRACE,
        END
    }

    private static final Set<String> ACCESS_MODIFIERS = new HashSet<>(
        Arrays.asList("public", "private", "protected")
    );
    
    private static final Set<String> NON_ACCESS_MODIFIERS = new HashSet<>(
        Arrays.asList("static", "final", "abstract", "native", "strictfp")
    );

    public RDP(List<Token> tokens) {
        this.tokens = tokens;
        this.errorHandler = new SyntaxErrorHandler();
    }

    public void parse() {
        parseClass();
        errorHandler.printErrors();
    }

    private void parseClass() {
        State currentState = State.START;
        boolean hasAccessModifier = false;
        boolean hasNonAccessModifier = false;
        boolean hasError = false;
        
        while (currentState != State.END && !atEnd()) {
            Token currentToken = peek();
            
            switch (currentState) {
                case START:
                    if (isAccessModifier(currentToken)) {
                        if (hasAccessModifier) {
                            errorHandler.reportError(
                                "Duplicate access modifier",
                                currentToken.getLine(),
                                currentToken.getColumn(),
                                "Only one access modifier is allowed"
                            );
                            hasError = true;
                        }
                        advance();
                        hasAccessModifier = true;
                        currentState = State.ACCESS_MODIFIER;
                    } else if (isNonAccessModifier(currentToken)) {
                        advance();
                        hasNonAccessModifier = true;
                        currentState = State.NON_ACCESS_MODIFIER;
                    } else if (isClassKeyword(currentToken)) {
                        advance();
                        currentState = State.CLASS_NAME;
                    } else {
                        errorHandler.reportError(
                            "Expected class declaration",
                            currentToken.getLine(),
                            currentToken.getColumn(),
                            "Class declaration should start with a modifier or 'class' keyword"
                        );
                        hasError = true;
                        // Try to continue parsing
                        if (currentToken.getType() == TokenType.DELIM && currentToken.getLexeme().equals("{")) {
                            currentState = State.OPEN_BRACE;
                        } else {
                            advance();
                        }
                    }
                    break;

                case ACCESS_MODIFIER:
                case NON_ACCESS_MODIFIER:
                    if (isNonAccessModifier(currentToken)) {
                        advance();
                        hasNonAccessModifier = true;
                        currentState = State.NON_ACCESS_MODIFIER;
                    } else if (isClassKeyword(currentToken)) {
                        advance();
                        currentState = State.CLASS_NAME;
                    } else {
                        errorHandler.reportError(
                            "Expected class keyword",
                            currentToken.getLine(),
                            currentToken.getColumn(),
                            "Add 'class' keyword here"
                        );
                        hasError = true;
                        // Check if we can continue parsing
                        if (currentToken.getType() == TokenType.IDENTIFIER) {
                            advance();
                            currentState = State.CLASS_NAME;
                        } else if (currentToken.getType() == TokenType.DELIM && currentToken.getLexeme().equals("{")) {
                            errorHandler.reportError(
                                "Expected class name",
                                currentToken.getLine(),
                                currentToken.getColumn(),
                                "Add a class identifier before '{'"
                            );
                            currentState = State.OPEN_BRACE;
                        } else {
                            advance();
                        }
                    }
                    break;

                case CLASS_NAME:
                    if (currentToken.getType() == TokenType.DELIM && currentToken.getLexeme().equals("{")) {
                        advance();
                        currentState = State.OPEN_BRACE;
                    } else {
                        errorHandler.reportError(
                            "Expected opening brace '{'",
                            currentToken.getLine(),
                            currentToken.getColumn(),
                            "Add '{' to start class body"
                        );
                        hasError = true;
                        advance();
                    }
                    break;

                case OPEN_BRACE:
                    if (currentToken.getType() == TokenType.DELIM && currentToken.getLexeme().equals("}")) {
                        advance();
                        currentState = State.CLOSE_BRACE;
                    } else {
                        advance(); // Skip class body contents for now
                    }
                    break;

                case CLOSE_BRACE:
                    currentState = State.END;
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isAccessModifier(Token token) {
        return token.getType() == TokenType.RESERVED && 
                ACCESS_MODIFIERS.contains(token.getLexeme());
    }

    private boolean isNonAccessModifier(Token token) {
        return token.getType() == TokenType.RESERVED && 
                NON_ACCESS_MODIFIERS.contains(token.getLexeme());
    }

    private boolean isClassKeyword(Token token) {
        return token.getType() == TokenType.RESERVED && 
                token.getLexeme().equals("class");
    }

    // Helper methods
    private Token advance() {
        if (!atEnd()) current++;
        return previous();
    }

    private boolean atEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}