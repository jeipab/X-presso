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

    /**
     * Retrieves the error handler associated with this lexer.
     *
     * @return The error handler instance.
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Tokenizes the source code and returns a list of tokens.
     * It performs lexical analysis on the source code, splitting it into individual tokens.
     * Each token is analyzed and added to the token list.
     * If an error is encountered during lexical analysis, an error is reported to the error handler.
     * At the end of the analysis, an EOF token is added to the list of tokens.
     *
     * @return A list of tokens from the source code.
     */
    public List<Token> tokenize() {
        try {
            char currentChar;
            while ((currentChar = reader.readNext()) != SourceReader.EOF) {
                try {
                    // Handle whitespace characters
                    if (Character.isWhitespace(currentChar)) {
                        handleWhitespace(currentChar);
                    }
                    // Handle complex literals
                    else if (currentChar == '$') {
                        handleComplexLiteral(currentChar);
                    }
                    // Handle identifiers and keywords
                    else if (Character.isLetter(currentChar)) {
                        handleIdentifierOrKeyword(currentChar);
                    }
                    // Handle number literals
                    else if (Character.isDigit(currentChar)) {
                        handleNumberLiteral(currentChar);
                    }
                    // Handle colon characters
                    else if (isColon(currentChar)) {
                        handleColon(currentChar);
                    }
                    // Handle period characters
                    else if (currentChar == '.') {
                        handlePeriods(currentChar);
                    }
                    // Handle comments
                    else if (currentChar == '/') {
                        handleComment(currentChar);
                    }
                    // Handle object delimiters or operators
                    else if (currentChar == '<' || currentChar == '>') {
                        handleObjectDelimiterOrOperator(currentChar);
                    }
                    // Handle operator symbols
                    else if (isOperatorSymbol(currentChar)) {
                        handleOperator(currentChar);
                    }
                    // Handle delimiter or bracket characters
                    else if (isDelimiterOrBracket(currentChar)) {
                        handleDelimiterOrBracket(currentChar);
                    }
                    // Handle string literals
                    else if (currentChar == '"' || currentChar == '\'') {
                        handleStringLiteral(currentChar);
                    }
                    // Handle unknown characters
                    else {
                        errorHandler.handleInvalidCharacter(currentChar, reader.getLine(), reader.getColumn());
                    }
                } catch (Exception e) {
                    // Handle unknown errors
                    errorHandler.reportError(
                        ErrorType.UNKNOWN_TOKEN,
                        "Error processing token: " + e.getMessage(),
                        reader.getLine(),
                        reader.getColumn()
                    );
                }
            }

            // Add an EOF token to the list of tokens
            tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        } catch (SourceReaderException e) {
            // Handle file errors
            errorHandler.reportError(
                ErrorType.FILE_ERROR,
                "Error reading source file: " + e.getMessage(),
                reader.getLine(),
                reader.getColumn()
            );
        }

        // Return the list of tokens
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

    /**
     * Checks if the context is a unary context. A unary context is a location in the source code
     * where a unary operator can be used. This includes after a delimiter, a punctuation delimiter, 
     * an arithmetic operator, an assignment operator, a relational operator, a logical operator, a method operator, 
     * an inherit operator, or after a whitespace token. If the context is not a unary context, it is a binary context.
     * @return true if the context is a unary context, false otherwise
     */
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
        // Check if the last token is a delimiter, punctuation delimiter, arithmetic operator, assignment operator, relational operator, logical operator, method operator, or inherit operator
        return type == TokenType.DELIM 
            || type == TokenType.PUNC_DELIM 
            || type == TokenType.ARITHMETIC_OP 
            || type == TokenType.ASSIGN_OP
            || type == TokenType.REL_OP 
            || type == TokenType.LOG_OP
            || type == TokenType.METHOD_OP 
            || type == TokenType.INHERIT_OP;
    }

    /**
     * Checks if the given two-character string is a valid two-character operator.
     * This method checks if the string matches any of the two-character operators
     * in the language. It is used to determine if a two-character sequence is a
     * valid operator or not.
     *
     * @param op The two-character string to check.
     * @return True if the string is a valid two-character operator, false otherwise.
     */
    private boolean isValidTwoCharOperator(String op) {
        // Check if the operator is any of the two-character operators
        return op.equals("==") || op.equals("!=") || op.equals("<=") || op.equals(">=")
            || op.equals("&&") || op.equals("||") || op.equals("<<") || op.equals(">>")
            || op.equals("%=") || op.equals("?=") || op.equals("+=") || op.equals("-=")
            || op.equals("*=") || op.equals("/=") || op.equals("::") || op.equals("->")
            || op.equals("..") || op.equals(":>") || op.equals(":<");
    }

    /**
     * Validates if a given string is a valid identifier.
     * A valid identifier starts with a letter or underscore and may contain letters, digits, underscores, and hyphens.
     *
     * @param identifier The string to validate as an identifier.
     * @return true if the string is a valid identifier, false otherwise.
     */
    private boolean isValidIdentifier(String identifier) {
        // Check for empty string
        if (identifier.isEmpty()) {
            return false;
        }
        
        // Check first character - must be letter or underscore, not digit
        char firstChar = identifier.charAt(0);
        if (Character.isDigit(firstChar)) {
            return false;
        }
        
        // Check each character in the identifier
        for (char c : identifier.toCharArray()) {
            // Identifier characters must be letters, digits, underscores, or hyphens
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return false;
            }
        }
        
        // All checks passed, the identifier is valid
        return true;
    }

    /**
     * Checks if a given date is valid.
     * A valid date is in the range of 1-31 for the day, 1-12 for the month, and 0 for the year.
     * @param day the day of the date
     * @param month the month of the date
     * @param year the year of the date
     * @return true if the date is valid, false otherwise
     */
    private boolean isValidDate(String day, String month, String year) {
        try {
            int d = Integer.parseInt(day);
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            
            // Check day is in the range of 1-31
            // Check month is in the range of 1-12
            // Check year is 0
            return d >= 1 && d <= 31 && m >= 1 && m <= 12 && y >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a given fraction is valid.
     * A valid fraction is in the range of Integer.MAX_VALUE for the numerator and non-zero for the denominator.
     * @param numerator the numerator of the fraction
     * @param denominator the denominator of the fraction
     * @return true if the fraction is valid, false otherwise
     */
    private boolean isValidFraction(String numerator, String denominator) {
        try {
            int num = Integer.parseInt(numerator);
            int den = Integer.parseInt(denominator);
            return den != 0 && num <= Integer.MAX_VALUE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Handles whitespace characters in the source code.
     * Accumulates consecutive whitespace characters and adds them as a single token
     * to the token list.
     * 
     * @param firstChar The initial whitespace character.
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
    private void handleWhitespace(char firstChar) throws SourceReader.SourceReaderException {
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        // StringBuilder to accumulate whitespace characters
        StringBuilder whitespace = new StringBuilder();
        whitespace.append(firstChar);
        
        // Continue reading while the next character is whitespace
        while (Character.isWhitespace(reader.peek())) {
            whitespace.append(reader.readNext());
        }

        // Add the accumulated whitespace as a token
        tokens.add(new Token(TokenType.WHITESPACE, whitespace.toString(), startLine, startColumn));
    }

    /**
     * Handles delimiter and bracket characters in the source code.
     * Accumulates the character and adds it as a single token to the token list.
     * 
     * @param currentChar The delimiter or bracket character to be handled.
     */
    private void handleDelimiterOrBracket(char currentChar) {
        int line = reader.getLine();
        int startColumn = reader.getColumn();

        // If the character is a delimiter or bracket, add it to the token list
        if ("[](){}".indexOf(currentChar) != -1) {
            tokens.add(new Token(TokenType.DELIM, String.valueOf(currentChar), line, startColumn));
        } else if (",;?@".indexOf(currentChar) != -1) {
            tokens.add(new Token(TokenType.PUNC_DELIM, String.valueOf(currentChar), line, startColumn));
        }
    }

    /**
     * Handles string literals in the source code.
     * A string literal is enclosed within either single quotes or double quotes.
     * This method reads the string and adds it to the token list.
     * It also handles escape sequences and reports errors if the string is not properly terminated.
     * 
     * @param quote The quote that starts the string literal.
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
    private void handleStringLiteral(char quote) throws SourceReader.SourceReaderException {
        StringBuilder stringLiteral = new StringBuilder();
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        // Add the opening quote to the token list
        tokens.add(new Token(TokenType.STR_DELIM, String.valueOf(quote), startLine, startColumn));

        char currentChar;
        boolean terminated = false;
        while ((currentChar = reader.readNext()) != quote && !terminated) {
            if (currentChar == SourceReader.EOF) {
                // If the string is not properly terminated, report an error
                errorHandler.handleUnterminatedString(startLine, startColumn);
                return;
            }

            if (currentChar == '\\') {
                // Handle escape sequences
                char nextChar = reader.peek();
                switch (nextChar) {
                    case 'n', 't', 'r', '"', '\\' -> {
                        // Add the escape sequence to the string literal
                        stringLiteral.append(currentChar).append(reader.readNext());
                        // Add the escape sequence to the token list
                        tokens.add(new Token(TokenType.ESCAPE_CHAR, "\\" + nextChar, reader.getLine(), reader.getColumn()));
                    }
                    default -> {
                        // Report an error if the escape sequence is invalid
                        errorHandler.handleInvalidEscapeSequence("\\" + nextChar, reader.getLine(), reader.getColumn());
                        // Add the invalid escape sequence to the string literal
                        stringLiteral.append(currentChar).append(reader.readNext());
                    }
                }
            } else {
                // Add the character to the string literal
                stringLiteral.append(currentChar);
            }
        }

        // Add the closing quote to the token list
        tokens.add(new Token(TokenType.STR_DELIM, String.valueOf(quote), reader.getLine(), reader.getColumn()));
        // Add the string literal to the token list
        tokens.add(new Token(TokenType.STR_LIT, stringLiteral.toString(), startLine, startColumn));
    }

    /**
     * Handles identifiers or keywords in the source code.
     * Identifies and categorizes tokens as keywords, reserved words, boolean literals, or identifiers.
     * Also handles unary minus operators if encountered.
     * 
     * @param firstChar The initial character of the identifier.
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
    private void handleIdentifierOrKeyword(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder identifier = new StringBuilder();
        identifier.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        int hyphenCount = 0;
        boolean isUnaryMinus = false;
        
        // Read the full identifier or keyword
        while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_' || reader.peek() == '-') {
            char next = reader.peek();
            if (next == '-') {
                hyphenCount++;
                // Check for consecutive hyphens indicating a unary minus
                if (hyphenCount > 1) {
                    if (identifier.charAt(identifier.length() - 1) == '-') {
                        identifier.deleteCharAt(identifier.length() - 1);
                        isUnaryMinus = true;
                    }
                    break;
                }
            }
            identifier.append(reader.readNext());
        }

        String identifierStr = identifier.toString();
        // Validate the identifier
        if (!isValidIdentifier(identifierStr)) {
            errorHandler.handleInvalidIdentifier(identifierStr, startLine, startColumn);
            return;
        }

        // Categorize the identifier
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

        // Handle unary minus operator if applicable
        if (isUnaryMinus) {
            handleOperator('-');
        }
    }

    /**
     * Handles the operators encountered in the source code.
     * It differentiates between various operator types such as method, unary,
     * bitwise, logical, and relational operators, and adds them to the tokens list.
     * 
     * @param firstChar The initial character of the operator.
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
    private void handleOperator(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder operator = new StringBuilder();
        operator.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        // Check for `->` operator (method operator)
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

        // Handle bitwise shift operators (<<, <<<, >>, >>>)
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
        
        // Handle unary operators (+ and - in unary context)
        if ((firstChar == '+' || firstChar == '-') && isUnaryContext()) {
            tokens.add(new Token(TokenType.UNARY_OP, String.valueOf(firstChar), startLine, startColumn));
            return;
        }
    
        // Handle logical NOT (!)
        if (firstChar == '!' && reader.peek() != '=') {
            tokens.add(new Token(TokenType.LOG_OP, "!", startLine, startColumn));
            return;
        }
    
        // Handle bitwise NOT (~)
        if (firstChar == '~') {
            tokens.add(new Token(TokenType.BIT_OP, "~", startLine, startColumn));
            return;
        }
    
        // Handle method operator (.)
        if (firstChar == '.' && !Character.isDigit(reader.peek())) {
            tokens.add(new Token(TokenType.METHOD_OP, ".", startLine, startColumn));
            return;
        }
    
        // Handle multi-character operators by checking valid two-character operators
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
    
        // Handle single-character operators and categorize them
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

    /**
     * Handles the colon (:) operator and its variants.
     * A single colon is a method operator, a colon followed by a greater than symbol (:) is an
     * inheritance operator, and a colon followed by two greater than symbols (:>) is also an
     * inheritance operator.
     * If more than one colon is encountered, or if the colon is not followed by a valid operator,
     * an error is reported.
     * 
     * @param firstChar The initial colon character.
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
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

    /**
     * Handles the period operator (.) and its variants.
     * A period by itself is a method operator, two periods (..) is a loop operator,
     * and three periods (...) is a loop operator.
     * If more than three consecutive periods are encountered, an error is reported.
     * 
     * @param firstChar The initial period character.
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
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

        // Determine the type of operator
        if (count == 1) {
            // Single period is a method operator
            tokens.add(new Token(TokenType.METHOD_OP, symbol.toString(), startLine, startColumn));
        } else if (count == 3) {
            // Three periods is a loop operator
            tokens.add(new Token(TokenType.LOOP_OP, symbol.toString(), startLine, startColumn));
        } else if (count == 2) {
            // Two periods is a loop operator
            tokens.add(new Token(TokenType.LOOP_OP, symbol.toString(), startLine, startColumn));
        }
    }

    /**
     * Categorizes the given operator string into its corresponding token type.
     * This method uses a switch expression to map operators to token types.
     *
     * @param op The operator string to categorize.
     * @return The TokenType corresponding to the operator.
     */
    private TokenType categorizeOperator(String op) {
        return switch (op) {
            // Arithmetic operators
            case "+", "-", "*", "/", "%" -> TokenType.ARITHMETIC_OP;
            // Assignment operators
            case "%=", "?=", "=", "+=", "-=", "*=", "/=" -> TokenType.ASSIGN_OP;
            // Relational operators
            case ">", "<", ">=", "<=", "==", "!=" -> TokenType.REL_OP;
            // Logical operators
            case "&&", "||", "!" -> TokenType.LOG_OP;
            // Bitwise operators
            case "&", "|", "~", "<<", ">>", ">>>" -> TokenType.BIT_OP;
            // Method invocation operators
            case ".", "::", "->" -> TokenType.METHOD_OP;
            // Loop operators
            case "...", ".." -> TokenType.LOOP_OP;
            // Inheritance operators
            case ":>", ":>>" -> TokenType.INHERIT_OP;
            // Default case for unknown operators
            default -> TokenType.UNKNOWN;
        };
    }

    /**
     * Handles number literals.
     * It checks if the given string is a valid number, and if so, adds it to the tokens list.
     * If the string is neither a valid integer nor a valid float, an error is reported to the error handler.
     * 
     * @param firstChar The initial character indicating a number ('0' - '9')
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
    private void handleNumberLiteral(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder number = new StringBuilder();
        number.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        boolean isFloat = false;
        boolean periodOperator = false;
        
        while (Character.isDigit(reader.peek()) || reader.peek() == '.' || Character.isLetter(reader.peek())) {
            char nextChar = reader.peek();
            
            // If we find a letter, treat the entire sequence as an invalid identifier
            if (Character.isLetter(nextChar)) {
                while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_' || reader.peek() == '-') {
                    number.append(reader.readNext());
                }
                errorHandler.handleInvalidIdentifier(number.toString(), startLine, startColumn);
                return;
            }
            
            if (nextChar == '.') {
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

    /**
     * Handles date and fraction literals.
     * It checks if the given string is a valid date or fraction, and if so, adds it to the tokens list.
     * If the string is neither a valid date nor a valid fraction, an error is reported to the error handler.
     * 
     * @param value The string to be checked for a valid date or fraction.
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
    private void handleDateOrFraction(StringBuilder value) throws SourceReader.SourceReaderException {
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();
        int separatorCount = 1; // Already have one '|'
        
        // Read the string until we reach the end of the file or a non-digit character
        while (reader.peek() != SourceReader.EOF) {
            char current = reader.peek();
            if (current == '|') {
                // Count the number of '|' separators
                separatorCount++;
                if (separatorCount > 2) {
                    // If we have more than 2 separators, it's an invalid date format
                    errorHandler.handleInvalidDateFormat(value.toString(), startLine, startColumn);
                    return;
                }
            } else if (!Character.isDigit(current)) {
                // If we encounter a non-digit character, we're done
                break;
            }
            value.append(reader.readNext());
        }

        // Check if the string is a valid date or fraction
        if (separatorCount == 2) {
            // Split the string by '|'
            String[] parts = value.toString().split("\\|");
            if (parts.length == 3 && isValidDate(parts[0], parts[1], parts[2])) {
                // If the string is a valid date, add it to the tokens list
                tokens.add(new Token(TokenType.DATE_LIT, value.toString(), startLine, startColumn));
            } else {
                // If the string is not a valid date, report an error
                errorHandler.handleInvalidDateFormat(value.toString(), startLine, startColumn);
            }
        } else {
            // Split the string by '|'
            String[] parts = value.toString().split("\\|");
            if (parts.length == 2 && isValidFraction(parts[0], parts[1])) {
                // If the string is a valid fraction, add it to the tokens list
                tokens.add(new Token(TokenType.FRAC_LIT, value.toString(), startLine, startColumn));
            } else {
                // If the string is not a valid fraction, report an error
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

    /**
     * Handles complex literals.
     * Complex literals are defined as a string of the format $(real,imag),
     * where real and imag are numbers. The method checks the validity of the
     * complex literal and adds it to the token list if it is valid.
     * 
     * @throws SourceReaderException 
     */
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

    /**
     * Handles both single-line and multi-line comments.
     * Adds the comment as a token if properly terminated.
     * 
     * @param firstChar The initial character indicating a comment ('/')
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error
     */
    private void handleComment(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder comment = new StringBuilder();
        comment.append(firstChar);
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        // Check for single-line comment
        if (reader.peek() == '/') { 
            // Read until the end of the line or EOF
            while (reader.peek() != '\n' && reader.peek() != SourceReader.EOF) {
                comment.append(reader.readNext());
            }
        // Check for multi-line comment
        } else if (reader.peek() == '*') { 
            boolean terminated = false;
            
            // Read until the comment is terminated or EOF
            while (!terminated && reader.peek() != SourceReader.EOF) {
                char current = reader.readNext();
                comment.append(current);
                
                // Check for comment termination sequence '*/'
                if (current == '*' && reader.peek() == '/') {
                    comment.append(reader.readNext());
                    terminated = true;
                }
            }
            
            // Handle unterminated multi-line comment error
            if (!terminated) {
                errorHandler.handleUnterminatedComment(startLine, startColumn);
                return;
            }
        } else {
            // If not a comment, treat it as an operator
            handleOperator('/');
            return;
        }

        // Add the comment as a token
        tokens.add(new Token(TokenType.COMMENT, comment.toString(), startLine, startColumn));
    }

    /**
     * Handles object delimiters and operators, differentiating between object type declarations
     * and operator usage. Recognizes "<type>" for object delimiters and handles operators
     * like '<' and '>'.
     *
     * @param firstChar The first character to check ('<' or '>').
     * @throws SourceReader.SourceReaderException If reading from the source encounters an error.
     */
    private void handleObjectDelimiterOrOperator(char firstChar) throws SourceReader.SourceReaderException {
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();

        if (firstChar == '<') {
            char nextChar = reader.peek();

            // Check if it's beginning of an object type declaration
            if (Character.isLetter(nextChar) || nextChar == '"' || nextChar == '\'') {
                tokens.add(new Token(TokenType.OBJ_DELIM, "<", startLine, startColumn));

                // Handle string literal if enclosed in quotes
                if (nextChar == '"' || nextChar == '\'') {
                    handleStringLiteral(reader.readNext());
                } else {
                    // Read type name following the delimiter
                    StringBuilder typeName = new StringBuilder();
                    while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_') {
                        typeName.append(reader.readNext());
                    }

                    // Report error if type name is empty
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

                // Check for closing '>' delimiter
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
                // Treat as an operator if not followed by valid type identifier start
                handleOperator(firstChar);
            }
        } else if (firstChar == '>') {
            // Check if '>' is part of an object delimiter or an operator
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).getType() == TokenType.OBJ_DELIM) {
                tokens.add(new Token(TokenType.OBJ_DELIM, ">", startLine, startColumn));
            } else {
                handleOperator(firstChar);
            }
        }
    }
}