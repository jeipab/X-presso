package lexer;

import java.util.ArrayList;
import java.util.List;

import language.SpecialWords;
import util.ErrorHandler;
import util.SourceReader;
import util.ErrorHandler.ErrorType;
import util.SourceReader.SourceReaderException;

/**
 * Lexer class that performs lexical analysis on a given source code.
 * It converts the input into a list of tokens.
 */
public class Lexer {
    private final SourceReader reader;
    private final List<Token> tokens;
    private final SpecialWords specialWords;
    private final ErrorHandler errorHandler;

    public Lexer(SourceReader reader) {
        this.reader = reader;
        this.tokens = new ArrayList<>();
        this.specialWords = new SpecialWords();
        this.errorHandler = new ErrorHandler();
        this.errorHandler.setCurrentFile(reader.getFilePath());
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public List<Token> tokenize() {
        try {
            char currentChar;
            while ((currentChar = reader.readNext()) != SourceReader.EOF) {
                try {
                    if (Character.isWhitespace(currentChar)) {
                        handleWhitespace(currentChar);
                    } else if (currentChar == '$') {
                        handleComplexLiteral(currentChar);
                    } else if (Character.isLetter(currentChar)) {
                        handleIdentifierOrKeyword(currentChar);
                    } else if (Character.isDigit(currentChar)) {
                        handleNumberLiteral(currentChar);
                    } else if (isColon(currentChar)) {
                        handleColon(currentChar);
                    } else if (currentChar == '.') {
                        handlePeriods(currentChar);
                    } else if (currentChar == '/') {
                        handleComment(currentChar);
                    } else if (currentChar == '<' || currentChar == '>') {
                        handleObjectDelimiterOrOperator(currentChar);
                    } else if (isOperatorSymbol(currentChar)) {
                        handleOperator(currentChar);
                    } else if (isDelimiterOrBracket(currentChar)) {
                        handleDelimiterOrBracket(currentChar);
                    } else if (currentChar == '"' || currentChar == '\'') {
                        handleStringLiteral(currentChar);
                    } else {
                        errorHandler.handleInvalidCharacter(currentChar, reader.getLine(), reader.getColumn());
                    }
                } catch (Exception e) {
                    errorHandler.reportError(
                        ErrorType.UNKNOWN_TOKEN,
                        "Error processing token: " + e.getMessage(),
                        reader.getLine(),
                        reader.getColumn()
                    );
                }
            }

            tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        } catch (SourceReaderException e) {
            errorHandler.reportError(
                ErrorType.FILE_ERROR,
                "Error reading source file: " + e.getMessage(),
                reader.getLine(),
                reader.getColumn()
            );
        }

        return tokens;
    }

    // Helper methods for character classification
    private boolean isOperatorSymbol(char c) {
        return "+-*=%?<>!&|^~.:".indexOf(c) != -1;
    }

    private boolean isDelimiterOrBracket(char c) {
        return "(){},;[]@?".indexOf(c) != -1;
    }

    private boolean isColon(char c) {
        return c == ':';
    }

    private boolean isUnaryContext() {
        if (tokens.isEmpty()) return true;
        
        // Find the last non-whitespace token
        Token lastToken = tokens.get(tokens.size() - 1);
        while (lastToken.getType() == TokenType.WHITESPACE) {
            tokens.remove(tokens.size() - 1);
            
            if (tokens.isEmpty()) return true;
            lastToken = tokens.get(tokens.size() - 1);
        }
    
        TokenType type = lastToken.getType();
        return type == TokenType.DELIM 
            || type == TokenType.PUNC_DELIM 
            || type == TokenType.ARITHMETIC_OP 
            || type == TokenType.ASSIGN_OP
            || type == TokenType.REL_OP 
            || type == TokenType.LOG_OP
            || type == TokenType.METHOD_OP 
            || type == TokenType.INHERIT_OP;
    }

    private boolean isValidIdentifier(String identifier) {
        if (identifier.isEmpty() || !Character.isLetter(identifier.charAt(0))) {
            return false;
        }
        
        for (char c : identifier.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return false;
            }
        }
        
        return true;
    }

    private boolean isValidDate(String day, String month, String year) {
        try {
            int d = Integer.parseInt(day);
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            
            return d >= 1 && d <= 31 && m >= 1 && m <= 12 && y >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidFraction(String numerator, String denominator) {
        try {
            @SuppressWarnings("unused")
            int num = Integer.parseInt(numerator);
            int den = Integer.parseInt(denominator);
            return den != 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private TokenType categorizeOperator(String op) {
        return switch (op) {
            case "+", "-", "*", "/", "%" -> TokenType.ARITHMETIC_OP;
            case "%=", "?=", "=", "+=", "-=", "*=", "/=" -> TokenType.ASSIGN_OP;
            case ">", "<", ">=", "<=", "==", "!=" -> TokenType.REL_OP;
            case "&&", "||", "!" -> TokenType.LOG_OP;
            case "&", "|", "~", "<<", ">>", ">>>" -> TokenType.BIT_OP;
            case ".", "::", "->" -> TokenType.METHOD_OP;
            case "...", ".." -> TokenType.LOOP_OP;
            case ":>", ":>>" -> TokenType.INHERIT_OP;
            default -> TokenType.UNKNOWN;
        };
    }

    private boolean isValidTwoCharOperator(String op) {
        return op.equals("==") || op.equals("!=") || op.equals("<=") || op.equals(">=") 
            || op.equals("&&") || op.equals("||") || op.equals("<<") || op.equals(">>")
            || op.equals("%=") || op.equals("?=") || op.equals("+=") || op.equals("-=")
            || op.equals("*=") || op.equals("/=") || op.equals("::") || op.equals("->")
            || op.equals("..") || op.equals(":>") || op.equals(":<");
    }

    // Basic token handlers
    private void handleWhitespace(char firstChar) throws SourceReader.SourceReaderException {
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        StringBuilder whitespace = new StringBuilder();
        whitespace.append(firstChar);
        
        while (Character.isWhitespace(reader.peek())) {
            whitespace.append(reader.readNext());
        }

        tokens.add(new Token(TokenType.WHITESPACE, whitespace.toString(), startLine, startColumn));
    }

    private void handleDelimiterOrBracket(char currentChar) {
        int line = reader.getLine();
        int column = reader.getColumn();
        
        if ("[](){}".indexOf(currentChar) != -1) {
            tokens.add(new Token(TokenType.DELIM, String.valueOf(currentChar), line, column));
        } else if (",;?@".indexOf(currentChar) != -1) {
            tokens.add(new Token(TokenType.PUNC_DELIM, String.valueOf(currentChar), line, column));
        }
    }

    private void handleColon(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder symbol = new StringBuilder();
        symbol.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        if (reader.peek() == '>') {
            symbol.append(reader.readNext());

            if (reader.peek() == '>') {
                symbol.append(reader.readNext());
                tokens.add(new Token(TokenType.INHERIT_OP, symbol.toString(), startLine, startColumn));
            } else {
                tokens.add(new Token(TokenType.INHERIT_OP, symbol.toString(), startLine, startColumn));
            }
        } else if (isColon(reader.peek())) {
            symbol.append(reader.readNext());
            tokens.add(new Token(TokenType.METHOD_OP, symbol.toString(), startLine, startColumn));
        } else {
            tokens.add(new Token(TokenType.PUNC_DELIM, symbol.toString(), startLine, startColumn));
        }
    }

    private void handlePeriods(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder symbol = new StringBuilder();
        symbol.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        int count = 1;
        while (reader.peek() == '.') {
            count++;
            if (count > 3) {
                errorHandler.reportError(
                    ErrorType.INVALID_OPERATOR,
                    "More than three consecutive periods are not allowed",
                    startLine,
                    startColumn
                );
                break;
            }
            symbol.append(reader.readNext());
        }

        if (count == 1) {
            tokens.add(new Token(TokenType.METHOD_OP, symbol.toString(), startLine, startColumn));
        } else if (count == 3) {
            tokens.add(new Token(TokenType.LOOP_OP, symbol.toString(), startLine, startColumn));
        } else if (count == 2) {
            tokens.add(new Token(TokenType.LOOP_OP, symbol.toString(), startLine, startColumn));
        }
    }

    private void handleStringLiteral(char quote) throws SourceReader.SourceReaderException {
        StringBuilder stringLiteral = new StringBuilder();
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        tokens.add(new Token(TokenType.STR_DELIM, String.valueOf(quote), startLine, startColumn));

        char currentChar;
        while ((currentChar = reader.readNext()) != quote) {
            if (currentChar == SourceReader.EOF) {
                errorHandler.handleUnterminatedString(startLine, startColumn);
                return;
            }
            
            if (currentChar == '\\') {
                char nextChar = reader.peek();
                switch (nextChar) {
                    case 'n', 't', 'r', '"', '\\' -> {
                        stringLiteral.append(currentChar).append(reader.readNext());
                        tokens.add(new Token(TokenType.ESCAPE_CHAR, "\\" + nextChar, reader.getLine(), reader.getColumn()));
                    }
                    default -> {
                        errorHandler.handleInvalidEscapeSequence("\\" + nextChar, reader.getLine(), reader.getColumn());
                        stringLiteral.append(currentChar).append(reader.readNext());
                    }
                }
            } else {
                stringLiteral.append(currentChar);
            }
        }

        tokens.add(new Token(TokenType.STR_DELIM, String.valueOf(quote), reader.getLine(), reader.getColumn()));
        tokens.add(new Token(TokenType.STR_LIT, stringLiteral.toString(), startLine, startColumn));
    }

    private void handleIdentifierOrKeyword(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder identifier = new StringBuilder();
        identifier.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        int hyphenCount = 0;
        boolean isUnaryMinus = false;
        
        while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_' || reader.peek() == '-') {
            char next = reader.peek();
            if (next == '-') {
                hyphenCount++;
                if (hyphenCount > 1) {
                    if (identifier.charAt(identifier.length()-1) == '-') {
                        identifier.deleteCharAt(identifier.length()-1);
                        isUnaryMinus = true;
                    }
                    break;
                }
            }
            identifier.append(reader.readNext());
        }

        String identifierStr = identifier.toString();
        if (!isValidIdentifier(identifierStr)) {
            errorHandler.handleInvalidIdentifier(identifierStr, startLine, startColumn);
            return;
        }

        if (specialWords.isKeyword(identifierStr)) {
            tokens.add(new Token(TokenType.KEYWORD, identifierStr, startLine, startColumn));
        } else if (specialWords.isReservedWord(identifierStr)) {
            tokens.add(new Token(TokenType.RESERVED, identifierStr, startLine, startColumn));
        } else if (identifierStr.indexOf('-') == -1) {
            if (identifierStr.equals("true") || identifierStr.equals("false")) {
                tokens.add(new Token(TokenType.BOOL_LIT, identifierStr, startLine, startColumn));
            } else {
                tokens.add(new Token(TokenType.IDENTIFIER, identifierStr, startLine, startColumn));
            }
        }

        if (isUnaryMinus) {
            handleOperator('-');
        }
    }

    private void handleNumberLiteral(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder number = new StringBuilder();
        number.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        boolean isFloat = false;
        boolean periodOperator = false;
        
        while (Character.isDigit(reader.peek()) || reader.peek() == '.') {
            if (reader.peek() == '.') {
                if (isFloat) {
                    periodOperator = true;
                    number.deleteCharAt(number.length()-1);
                    isFloat = false;    
                    break;
                }
                isFloat = true;
            }
            number.append(reader.readNext());
        }

        if (number.toString().endsWith(".")) {
            errorHandler.handleInvalidNumber(number.toString(), startLine, startColumn);
            return;
        }

        if (reader.peek() == '|') {
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
        tokens.add(new Token(type, number.toString(), startLine, startColumn));

        if (periodOperator) {
            handlePeriods('.');
        }
    }

    private void handleDateOrFraction(StringBuilder value) throws SourceReader.SourceReaderException {
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();
        int separatorCount = 1; // Already have one '|'
        
        while (reader.peek() != SourceReader.EOF) {
            char current = reader.peek();
            if (current == '|') {
                separatorCount++;
                if (separatorCount > 2) {
                    errorHandler.handleInvalidDateFormat(value.toString(), startLine, startColumn);
                    return;
                }
            } else if (!Character.isDigit(current)) {
                break;
            }
            value.append(reader.readNext());
        }

        if (separatorCount == 2) {
            String[] parts = value.toString().split("\\|");
            if (parts.length == 3 && isValidDate(parts[0], parts[1], parts[2])) {
                tokens.add(new Token(TokenType.DATE_LIT, value.toString(), startLine, startColumn));
            } else {
                errorHandler.handleInvalidDateFormat(value.toString(), startLine, startColumn);
            }
        } else {
            String[] parts = value.toString().split("\\|");
            if (parts.length == 2 && isValidFraction(parts[0], parts[1])) {
                tokens.add(new Token(TokenType.FRAC_LIT, value.toString(), startLine, startColumn));
            } else {
                errorHandler.reportError(
                    ErrorType.INVALID_FRACTION_FORMAT,
                    "Invalid fraction format: " + value,
                    startLine,
                    startColumn,
                    "Fractions should be in the format numerator|denominator"
                );
            }
        }
    }

    private void handleOperator(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder operator = new StringBuilder();
        operator.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        // Check for `->` operator
        if (firstChar == '-' && reader.peek() == '>') {
            operator.append(reader.readNext());
            tokens.add(new Token(TokenType.METHOD_OP, operator.toString(), startLine, startColumn));
            return;
        }
        
        // Check for increment/decrement operators (++ and --)
        if ((firstChar == '+' || firstChar == '-') && reader.peek() == firstChar) {
            operator.append(reader.readNext());
            tokens.add(new Token(TokenType.UNARY_OP, operator.toString(), startLine, startColumn));
            return;
        }

        // Check for exponentiation operator (**)
        if (firstChar == '*' && reader.peek() == '*') {
            operator.append(reader.readNext());
            tokens.add(new Token(TokenType.UNARY_OP, operator.toString(), startLine, startColumn));
            return;
        }

        // Handle bitwise shift operators
        if (firstChar == '<' || firstChar == '>') {
            char nextChar = reader.peek();
            if (operator.toString().equals("<") && nextChar == '<') {
                operator.append(reader.readNext()); 
                nextChar = reader.peek();
                if (nextChar == '<') {
                    operator.append(reader.readNext()); 
                    nextChar = reader.peek();
                    if (nextChar == '<') {
                        operator.append(reader.readNext()); 
                        tokens.add(new Token(TokenType.BIT_OP, operator.toString(), startLine, startColumn));
                        return;
                    }
                }
                tokens.add(new Token(TokenType.BIT_OP, operator.toString(), startLine, startColumn));
                return;
            }
    
            if (operator.toString().equals(">") && nextChar == '>') {
                operator.append(reader.readNext()); 
                nextChar = reader.peek();
                if (nextChar == '>') {
                    operator.append(reader.readNext()); 
                    nextChar = reader.peek();
                    if (nextChar == '>') {
                        operator.append(reader.readNext()); 
                        tokens.add(new Token(TokenType.BIT_OP, operator.toString(), startLine, startColumn));
                        return;
                    }
                }
                tokens.add(new Token(TokenType.BIT_OP, operator.toString(), startLine, startColumn));
                return;
            }
        }
        
        // Handle unary operators
        if ((firstChar == '+' || firstChar == '-') && isUnaryContext()) {
            tokens.add(new Token(TokenType.UNARY_OP, String.valueOf(firstChar), startLine, startColumn));
            return;
        }
    
        // Handle logical NOT
        if (firstChar == '!' && reader.peek() != '=') {
            tokens.add(new Token(TokenType.LOG_OP, "!", startLine, startColumn));
            return;
        }
    
        // Handle bitwise NOT
        if (firstChar == '~') {
            tokens.add(new Token(TokenType.BIT_OP, "~", startLine, startColumn));
            return;
        }
    
        // Handle method operator (dot)
        if (firstChar == '.' && !Character.isDigit(reader.peek())) {
            tokens.add(new Token(TokenType.METHOD_OP, ".", startLine, startColumn));
            return;
        }
    
        // Handle multi-character operators
        while (isOperatorSymbol(reader.peek())) {
            char nextChar = reader.peek();
            String currentOp = operator.toString() + nextChar;
            
            if (isValidTwoCharOperator(currentOp)) {
                operator.append(reader.readNext());
                String op = operator.toString();
                TokenType type = categorizeOperator(op);
                tokens.add(new Token(type, op, startLine, startColumn));
                return;
            }
            
            break;
        }
    
        // Handle single-character operators
        String op = operator.toString();
        if (op.equals("<") || op.equals(">")) {
            tokens.add(new Token(TokenType.REL_OP, op, startLine, startColumn));
        } else {
            TokenType type = categorizeOperator(op);
            if (type == TokenType.UNKNOWN) {
                errorHandler.reportError(
                    ErrorType.INVALID_OPERATOR,
                    "Invalid operator: " + op,
                    startLine,
                    startColumn
                );
            } else {
                tokens.add(new Token(type, op, startLine, startColumn));
            }
        }
    }

    private void handleComment(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder comment = new StringBuilder();
        comment.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        if (reader.peek() == '/') { // Single-line comment
            while (reader.peek() != '\n' && reader.peek() != SourceReader.EOF) {
                comment.append(reader.readNext());
            }
        } else if (reader.peek() == '*') { // Multi-line comment
            comment.append(reader.readNext());
            boolean terminated = false;
            
            while (!terminated && reader.peek() != SourceReader.EOF) {
                char current = reader.readNext();
                comment.append(current);
                
                if (current == '*' && reader.peek() == '/') {
                    comment.append(reader.readNext());
                    terminated = true;
                }
            }
            
            if (!terminated) {
                errorHandler.handleUnterminatedComment(startLine, startColumn);
                return;
            }
        } else {
            handleOperator('/');
            return;
        }

        tokens.add(new Token(TokenType.COMMENT, comment.toString(), startLine, startColumn));
    }

    private void handleComplexLiteral(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder complex = new StringBuilder();
        complex.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        if (reader.peek() == '(') {
            complex.append(reader.readNext());
            int commaCount = 0;
            boolean valid = true;
            
            while (reader.peek() != ')' && reader.peek() != SourceReader.EOF) {
                char current = reader.readNext();
                if (current == ',') {
                    commaCount++;
                    if (commaCount > 1) {
                        valid = false;
                        break;
                    }
                } else if (!Character.isDigit(current) && current != '-' && current != '.') {
                    valid = false;
                    break;
                }
                complex.append(current);
            }

            if (reader.peek() == ')') {
                complex.append(reader.readNext());
                if (valid && commaCount == 1) {
                    tokens.add(new Token(TokenType.COMP_LIT, complex.toString(), startLine, startColumn));
                    return;
                }
            } else if (reader.peek() == SourceReader.EOF) {
                errorHandler.reportError(
                    ErrorType.INVALID_COMPLEX_LITERAL,
                    "Unterminated complex literal",
                    startLine,
                    startColumn,
                    "Add closing parenthesis"
                );
                return;
            }
            
            errorHandler.handleInvalidComplexLiteral(complex.toString(), startLine, startColumn);
        } else {
            errorHandler.reportError(
                ErrorType.INVALID_COMPLEX_LITERAL,
                "Complex literal must start with $(", 
                startLine, 
                startColumn,
                "Use format $(real,imag)"
            );
        }
    }

    private void handleObjectDelimiterOrOperator(char firstChar) throws SourceReader.SourceReaderException {
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        if (firstChar == '<') {
            char nextChar = reader.peek();

            if (Character.isLetter(nextChar) || nextChar == '"' || nextChar == '\'') {
                tokens.add(new Token(TokenType.OBJ_DELIM, "<", startLine, startColumn));

                if (nextChar == '"' || nextChar == '\'') {
                    handleStringLiteral(reader.readNext());
                } else {
                    StringBuilder typeName = new StringBuilder();
                    while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_') {
                        typeName.append(reader.readNext());
                    }
                    
                    if (typeName.length() == 0) {
                        errorHandler.reportError(
                            ErrorType.INVALID_IDENTIFIER,
                            "Empty type name in object delimiter",
                            reader.getLine(),
                            reader.getColumn(),
                            "Provide a valid type name"
                        );
                        return;
                    }
                    
                    tokens.add(new Token(TokenType.STR_LIT, typeName.toString(), reader.getLine(), reader.getColumn()));
                }

                if (reader.peek() == '>') {
                    tokens.add(new Token(TokenType.OBJ_DELIM, ">", reader.getLine(), reader.getColumn()));
                    reader.readNext();
                } else {
                    errorHandler.reportError(
                        ErrorType.MISMATCHED_DELIMITERS,
                        "Expected '>' to close object delimiter",
                        reader.getLine(),
                        reader.getColumn(),
                        "Add matching '>' delimiter"
                    );
                }
            } else {
                // Handle as operator if not followed by valid type identifier start
                handleOperator(firstChar);
            }
        } else if (firstChar == '>') {
            // Handle '>' as operator if not part of object delimiter
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).getType() == TokenType.OBJ_DELIM) {
                tokens.add(new Token(TokenType.OBJ_DELIM, ">", startLine, startColumn));
            } else {
                handleOperator(firstChar);
            }
        }
    }
}