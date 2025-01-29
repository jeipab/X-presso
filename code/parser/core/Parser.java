package parser.core;

import lexer.Token;
import parser.grammar.NonTerminal;
import util.SyntaxErrorHandler;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current;
    private final SyntaxErrorHandler errorHandler;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
        this.errorHandler = new SyntaxErrorHandler();
    }

    public void parse() {
        System.out.println("Parsing started...");
        while (!isAtEnd()) {
            advance();
        }
        System.out.println("Parsing completed.");
    }

    private Token advance() {
        if (!isAtEnd()) {
            return tokens.get(current++);
        }
        return null;
    }

    private Token peek() {
        if (!isAtEnd()) {
            return tokens.get(current);
        }
        return null;
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }
}
