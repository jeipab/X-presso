package parser.grammar;
import lexer.TokenType;

class Terminal implements Symbol {
    private TokenType tokenType;

    public Terminal(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public String getName() {
        return tokenType.toString();
    }

    @Override
    public String toString() {
        return tokenType.toString();
    }
}
