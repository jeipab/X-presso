package source.lexer;

import source.util.SourceReader;
import source.language.SpecialWords;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexer class that performs lexical analysis on a given source code.
 * It reads characters from the source and classifies them into tokens 
 * such as keywords, identifiers, operators, delimiters, literals, etc.
 */
public class Lexer {
    private final SourceReader reader;
    private final List<Token> tokens;
    private final SpecialWords specialWords;

    /**
     * Constructs a Lexer with a given SourceReader.
     *
     * @param reader the SourceReader to read characters from
     */
    public Lexer(SourceReader reader) {
        this.reader = reader;
        this.tokens = new ArrayList<>();
        this.specialWords = new SpecialWords();
    }

    /**
     * Tokenizes the source code into a list of tokens.
     *
     * @return a list of tokens representing the lexical components of the source code
     */
    public List<Token> tokenize() {
        try {
            char currentChar;

            while ((currentChar = reader.readNext()) != SourceReader.EOF) {
                if (Character.isWhitespace(currentChar)) {
                    handleWhitespace();
                } else if (Character.isLetter(currentChar)) {
                    handleIdentifierOrKeyword(currentChar);
                } else if (Character.isDigit(currentChar)) {
                    handleNumberLiteral(currentChar);
                } else if (isOperatorSymbol(currentChar)) {
                    handleOperator(currentChar);
                } else if (isDelimiterOrBracket(currentChar)) {
                    handleDelimiterOrBracket(currentChar);
                } else if (currentChar == '/') {
                    handleComment(currentChar);
                } else if (currentChar == '"' || currentChar == '\'') {
                    handleStringLiteral(currentChar);
                } else {
                    handleUnknownToken(currentChar);
                }
            }

            tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        } catch (SourceReader.SourceReaderException e) {
            System.err.println("Error during lexical analysis: " + e.getMessage());
        }

        return tokens;
    }

    // 1. Handle whitespace
    private void handleWhitespace() throws SourceReader.SourceReaderException {
        while (Character.isWhitespace(reader.peek())) {
            reader.readNext(); // Consume whitespace
        }
    }

    // 2. Handle identifiers and keywords
    private void handleIdentifierOrKeyword(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder identifier = new StringBuilder();
        identifier.append(firstChar);

        while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_') {
            identifier.append(reader.readNext());
        }

        String identifierStr = identifier.toString();
        if (specialWords.isKeyword(identifierStr)) {
            tokens.add(new Token(TokenType.KEYWORD, identifierStr, reader.getLine(), reader.getColumn()));
        } else if (specialWords.isReservedWord(identifierStr)) {
            tokens.add(new Token(TokenType.RESERVED_WORD, identifierStr, reader.getLine(), reader.getColumn()));
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, identifierStr, reader.getLine(), reader.getColumn()));
        }
    }

    // 3. Handle number literals
    private void handleNumberLiteral(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder number = new StringBuilder();
        number.append(firstChar);

        boolean isFloat = false;
        while (Character.isDigit(reader.peek()) || reader.peek() == '.') {
            if (reader.peek() == '.') {
                if (isFloat) break; // Only one decimal point allowed
                isFloat = true;
            }
            number.append(reader.readNext());
        }

        if (isFloat) {
            tokens.add(new Token(TokenType.FLOAT_LITERAL, number.toString(), reader.getLine(), reader.getColumn()));
        } else {
            tokens.add(new Token(TokenType.INTEGER_LITERAL, number.toString(), reader.getLine(), reader.getColumn()));
        }
    }

    // Additional token handlers

    private void handleOperator(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder operator = new StringBuilder();
        operator.append(firstChar);

        if (isOperatorSymbol(reader.peek())) {
            operator.append(reader.readNext()); // Handle multi-character operators
        }

        tokens.add(new Token(TokenType.OPERATOR, operator.toString(), reader.getLine(), reader.getColumn()));
    }

    private void handleDelimiterOrBracket(char firstChar) {
        tokens.add(new Token(TokenType.DELIMITER, String.valueOf(firstChar), reader.getLine(), reader.getColumn()));
    }

    private void handleStringLiteral(char quote) throws SourceReader.SourceReaderException {
        StringBuilder stringLiteral = new StringBuilder();
        stringLiteral.append(quote);

        char currentChar;
        while ((currentChar = reader.readNext()) != quote) {
            if (currentChar == '\\') {
                stringLiteral.append(reader.readNext()); // Escape sequences
            } else {
                stringLiteral.append(currentChar);
            }
        }
        stringLiteral.append(quote);
        tokens.add(new Token(TokenType.STRING_LITERAL, stringLiteral.toString(), reader.getLine(), reader.getColumn()));
    }

    private void handleComment(char firstChar) throws SourceReader.SourceReaderException {
        if (reader.peek() == '/') { // Single-line comment
            while (reader.readNext() != '\n' && reader.peek() != SourceReader.EOF) {}
        } else if (reader.peek() == '*') { // Multi-line comment
            reader.readNext();
            while (!(reader.readNext() == '*' && reader.peek() == '/')) {
                if (reader.peek() == SourceReader.EOF) {
                    handleError("Unterminated comment");
                    return;
                }
            }
            reader.readNext();
        } else {
            tokens.add(new Token(TokenType.DIVIDE_OPERATOR, "/", reader.getLine(), reader.getColumn()));
        }
    }

    private boolean isOperatorSymbol(char c) {
        return "+-*/=<>!&|".indexOf(c) != -1;
    }

    private boolean isDelimiterOrBracket(char c) {
        return "(){},;[]<>".indexOf(c) != -1;
    }

    private void handleError(String message) {
        System.err.println("Error at line " + reader.getLine() + ", column " + reader.getColumn() + ": " + message);
    }
}
