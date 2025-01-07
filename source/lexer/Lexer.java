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

    /**
     * Handles whitespace by advancing the reader position until non-whitespace is found.
     *
     * @throws SourceReader.SourceReaderException if an error occurs while reading
     */
    private void handleWhitespace() throws SourceReader.SourceReaderException {
        while (Character.isWhitespace(reader.peek())) {
            reader.readNext(); // Consume whitespace
        }
    }

    /**
     * Handles identifiers or keywords by reading characters and checking against reserved words.
     *
     * @param firstChar the first character of the identifier or keyword
     * @throws SourceReader.SourceReaderException if an error occurs while reading
     */
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
        }else if (specialWords.isNoiseWord(identifierStr)) {
            // Skip noise words
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, identifierStr, reader.getLine(), reader.getColumn()));
        }
    }

    /**
     * Handles number literals by reading digits and possibly handling decimal points.
     *
     * @param firstChar the first character of the number literal
     * @throws SourceReader.SourceReaderException if an error occurs while reading
     */
    private void handleNumberLiteral(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder number = new StringBuilder();
        number.append(firstChar);

        while (Character.isDigit(reader.peek())) {
            number.append(reader.readNext());
        }

        if (reader.peek() == '.') { // Handle decimals
            number.append(reader.readNext());
            while (Character.isDigit(reader.peek())) {
                number.append(reader.readNext());
            }
        }

        tokens.add(new Token(TokenType.LITERAL, number.toString(), reader.getLine(), reader.getColumn()));
    }

    /**
     * Handles operators by reading one or more characters that form an operator.
     *
     * @param firstChar the first character of the operator
     * @throws SourceReader.SourceReaderException if an error occurs while reading
     */
    private void handleOperator(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder operator = new StringBuilder();
        operator.append(firstChar);

        if (isOperatorSymbol(reader.peek())) {
            operator.append(reader.readNext()); // Handle multi-character operators
        }

        tokens.add(new Token(TokenType.OPERATOR, operator.toString(), reader.getLine(), reader.getColumn()));
    }

    /**
     * Handles delimiters and brackets by adding them as tokens.
     *
     * @param firstChar the character representing the delimiter or bracket
     */
    private void handleDelimiterOrBracket(char firstChar) {
        tokens.add(new Token(TokenType.DELIMITER, String.valueOf(firstChar), reader.getLine(), reader.getColumn()));
    }

    /**
     * Handles comments by reading and ignoring them, distinguishing between single-line and multi-line comments.
     *
     * @param firstChar the first character of the potential comment
     * @throws SourceReader.SourceReaderException if an error occurs while reading
     */
    private void handleComment(char firstChar) throws SourceReader.SourceReaderException {
        if (reader.peek() == '/') { // Single-line comment
            while (reader.readNext() != '\n' && reader.peek() != SourceReader.EOF) {}
        } else if (reader.peek() == '*') { // Multi-line comment
            reader.readNext(); // Consume '*'
            while (!(reader.readNext() == '*' && reader.peek() == '/')) {
                if (reader.peek() == SourceReader.EOF) {
                    handleError("Unterminated comment");
                    return;
                }
            }
            reader.readNext(); // Consume '/'
        } else {
            tokens.add(new Token(TokenType.OPERATOR, "/", reader.getLine(), reader.getColumn())); // Division operator
        }
    }

    /**
     * Handles unknown tokens by logging an error message.
     *
     * @param currentChar the character that is not recognized as a valid token
     */
    private void handleUnknownToken(char currentChar) {
        handleError("Unknown token: " + currentChar);
    }

    /**
     * Checks if a character is an operator symbol.
     *
     * @param c the character to check
     * @return true if the character is an operator symbol, false otherwise
     */
    private boolean isOperatorSymbol(char c) {
        return "+-*/=<>!&|".indexOf(c) != -1; // Includes logical and relational operators
    }

    /**
     * Checks if a character is a delimiter or bracket.
     *
     * @param c the character to check
     * @return true if the character is a delimiter or bracket, false otherwise
     */
    private boolean isDelimiterOrBracket(char c) {
        return "(){},;[]".indexOf(c) != -1; // Common delimiters and brackets
    }

    /**
     * Logs an error message with the current line and column information.
     *
     * @param message the error message to log
     */
    private void handleError(String message) {
        System.err.println("Error at line " + reader.getLine() + ", column " + reader.getColumn() + ": " + message);
    }
}