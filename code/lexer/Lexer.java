package lexer;

import java.util.ArrayList;
import java.util.List;

import language.SpecialWords;
import util.SourceReader;
import util.SourceReader.SourceReaderException;


/**
 * Lexer class that performs lexical analysis on a given source code.
 * It converts the input into a list of tokens.
 */
public class Lexer {
    private final SourceReader reader;
    private final List<Token> tokens;
    private final SpecialWords specialWords;

    /**
     * Constructs a Lexer with the given SourceReader.
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
     * @return a list of tokens
     */
    public List<Token> tokenize() {
        try {
            char currentChar;

            while ((currentChar = reader.readNext()) != SourceReader.EOF) {
                if (Character.isWhitespace(currentChar)) {
                    handleWhitespace(currentChar);
                } else if(currentChar=='$') {
                    handleComplexLiteral(currentChar);
                } else if (Character.isLetter(currentChar)) {
                    handleIdentifierOrKeyword(currentChar);
                } else if (Character.isDigit(currentChar)) {
                    handleNumberLiteral(currentChar);
                } else if (isColon(currentChar)) {
                    handleColon(currentChar);
                } else if (currentChar=='.') {
                    handlePeriods(currentChar);
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

    private void handleWhitespace(char firstChar) throws SourceReader.SourceReaderException {
        // Record the starting position of the whitespace
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        // Collect all contiguous whitespace characters
        StringBuilder whitespace = new StringBuilder();
        whitespace.append(firstChar);
        while (Character.isWhitespace(reader.peek())) {
            whitespace.append(reader.readNext());
        }

        // Add a WHITESPACE token with the collected value
        tokens.add(new Token(TokenType.WHITESPACE, whitespace.toString(), startLine, startColumn));
    }

    private void handleComplexLiteral(char firstChar) throws SourceReader.SourceReaderException{
        StringBuilder complex = new StringBuilder();
        complex.append(firstChar);

        int count = 0;
        if (reader.peek()=='(') {
            complex.append(reader.readNext());
            while (Character.isDigit(reader.peek()) || reader.peek()==',') {
                if (reader.peek()==',') {
                    count++;
                    if (count>1) break; //only one comma
                }
                complex.append(reader.readNext());
            }

            if (reader.peek()==')') {
                complex.append(reader.readNext());
                tokens.add(new Token(TokenType.COMP_LIT, complex.toString(), reader.getLine(), reader.getColumn()));
            }
        } else {
            handleError("Invalid token starting with '$'");
        } 
        
    }

    private void handleIdentifierOrKeyword(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder identifier = new StringBuilder();
        identifier.append(firstChar);

        while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_' || reader.peek() == '-') {
            identifier.append(reader.readNext());
        }

        String identifierStr = identifier.toString();
        if (specialWords.isKeyword(identifierStr)) {
            tokens.add(new Token(TokenType.KEYWORD, identifierStr, reader.getLine(), reader.getColumn()));
        } else if (specialWords.isReservedWord(identifierStr)) {
            tokens.add(new Token(TokenType.RESERVED_WORD, identifierStr, reader.getLine(), reader.getColumn()));
        } else if (identifierStr.indexOf('-') == -1) {
            tokens.add(new Token(TokenType.IDENTIFIER, identifierStr, reader.getLine(), reader.getColumn()));
        }
    }

    private void handleNumberLiteral(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder number = new StringBuilder();
        number.append(firstChar);

        boolean isFloat = false;
        boolean PeriodOperator = false;
        while (Character.isDigit(reader.peek()) || reader.peek() == '.') {
            if (reader.peek() == '.') {
                if (isFloat) {
                    PeriodOperator = true;
                    number.deleteCharAt(number.length()-1);
                    isFloat = false;    
                    break;
                } // Only one decimal point allowed
                isFloat = true;
            }
            number.append(reader.readNext());
        }

        if (reader.peek()=='|') {
            number.append(reader.readNext());
            if (Character.isDigit(reader.peek())) {
                handleDateOrFraction(number);
                return;
            } else if (isOperatorSymbol(reader.peek())) {
                handleOperator('|');
                return;
            }
        }

        TokenType type = isFloat ? TokenType.FLOAT_LIT : TokenType.INT_LIT;
        tokens.add(new Token(type, number.toString(), reader.getLine(), reader.getColumn()));

        if (PeriodOperator) {
            handlePeriods('.');
        }
    }

    private void handleColon(char firstChar) throws SourceReader.SourceReaderException{
        StringBuilder symbol = new StringBuilder();
        symbol.append(firstChar);

        if (isColon(reader.peek())) {
            symbol.append(reader.readNext());
            tokens.add(new Token(TokenType.METHOD_OP, symbol.toString(), reader.getLine(), reader.getColumn()));
        } else {
            tokens.add(new Token(TokenType.PUNC_DELIM, symbol.toString(), reader.getLine(), reader.getColumn()));
        }
    }

    private void handlePeriods(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder symbol = new StringBuilder();
        symbol.append(firstChar);

        int count = 1;
        while (reader.peek()=='.') {
            count++;
            if (count>3) break; //more than 3 periods not allowed
            symbol.append(reader.readNext());
        }

        if (count==1) {
            tokens.add(new Token(TokenType.METHOD_OP, symbol.toString(), reader.getLine(), reader.getColumn()));
        } else if (count==3) {
            tokens.add(new Token(TokenType.LOOP_OP, symbol.toString(), reader.getLine(), reader.getColumn()));
        } 
    }

    private void handleOperator(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder operator = new StringBuilder();
        operator.append(firstChar);

        if (isOperatorSymbol(reader.peek())) {
            operator.append(reader.readNext()); // Handle multi-character operators
        }

        tokens.add(new Token(TokenType.ARITHMETIC_OP, operator.toString(), reader.getLine(), reader.getColumn()));
    }

    private void handleDateOrFraction(StringBuilder value) throws SourceReader.SourceReaderException{
        int count = 1;
        while (Character.isDigit(reader.peek()) || reader.peek() == '|') {
            if (reader.peek()=='|') {
                count++;
                if (count>2) break; //More than 2 '|' is not permitted 
            }
            if (count<=2) {
                value.append(reader.readNext());
            }
        }

        TokenType type = (count==2) ? TokenType.DATE_LIT : TokenType.FRAC_LIT;
        tokens.add(new Token(type, value.toString(), reader.getLine(), reader.getColumn()));
    }

    private void handleDelimiterOrBracket(char firstChar) {
        if ("[](){}".indexOf(firstChar) != -1) {
            tokens.add(new Token(TokenType.DELIMITER, String.valueOf(firstChar), reader.getLine(), reader.getColumn()));
        } else if (".;?@".indexOf(firstChar) != -1) {
            tokens.add(new Token(TokenType.PUNC_DELIM, String.valueOf(firstChar), reader.getLine(), reader.getColumn()));
        }
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
        tokens.add(new Token(TokenType.STR_LIT, stringLiteral.toString(), reader.getLine(), reader.getColumn()));
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
            tokens.add(new Token(TokenType.ARITHMETIC_OP, "/", reader.getLine(), reader.getColumn()));
        }
    }

    private boolean isOperatorSymbol(char c) {
        return "+-*/=<>!&|".indexOf(c) != -1;
    }

    private boolean isDelimiterOrBracket(char c) {
        return "(){},;[]@?".indexOf(c) != -1;
    }

    private boolean isColon(char c) {
        return c == ':';
    }

    private void handleUnknownToken(char currentChar) {
        handleError("Unknown token: " + currentChar);
    }

    private void handleError(String message) {
        System.err.println("Error at line " + reader.getLine() + ", column " + reader.getColumn() + ": " + message);
    }
}
