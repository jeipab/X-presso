package source.lexer;

/**
 * Represents the smallest unit of a source code, such as a keyword, identifier, 
 * operator, or delimiter. A Token encapsulates its type, value, and position 
 * (line and column) in the source code for precise error tracking and parsing.
 */

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int value;

    public Token(TokenType type, String lexeme, int line, int value) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

     public String getLexeme() {
          return lexeme;
     }

     public int getLine() {
          return line;
     }

     public int getValue() {
          return value;
     }

}
