package lexer;

import java.util.ArrayList;
import java.util.List;

import language.SpecialWords;
import util.SourceReader;

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

    private void handleColon(char firstChar) throws SourceReader.SourceReaderException {
        StringBuilder symbol = new StringBuilder();
        symbol.append(firstChar);
    
        if (reader.peek() == '>') {
            // Read the first '>'
            symbol.append(reader.readNext());
    
            if (reader.peek() == '>') {
                // Read the second '>' for :>>
                symbol.append(reader.readNext());
                tokens.add(new Token(TokenType.INHERIT_OP, symbol.toString(), reader.getLine(), reader.getColumn()));
            } else {
                // Handle :> (extends)
                tokens.add(new Token(TokenType.INHERIT_OP, symbol.toString(), reader.getLine(), reader.getColumn()));
            }
        } else if (isColon(reader.peek())) {
            // Handle :: (method operator)
            symbol.append(reader.readNext());
            tokens.add(new Token(TokenType.METHOD_OP, symbol.toString(), reader.getLine(), reader.getColumn()));
        } else {
            // Handle single colon
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

        // Check for `->` operator
        if (firstChar == '-' && reader.peek() == '>') {
            operator.append(reader.readNext());
            tokens.add(new Token(TokenType.METHOD_OP, operator.toString(), reader.getLine(), reader.getColumn()));
            return;
        }
        
        // Check for multi-character operators (++ and --)
        if ((firstChar == '+' || firstChar == '-') && reader.peek() == firstChar) {
            operator.append(reader.readNext());
            tokens.add(new Token(TokenType.UNARY_OP, operator.toString(), reader.getLine(), reader.getColumn()));
            return;
        }

        // Check for exponentiation operator (**)
        if (firstChar == '*' && reader.peek() == '*') {
            operator.append(reader.readNext());
            tokens.add(new Token(TokenType.UNARY_OP, operator.toString(), reader.getLine(), reader.getColumn()));
            return;
        }
        
        // Handle unary operators
        // Handle -> operator
        if ((firstChar == '-' && reader.peek() == '>')) {
            operator.append(reader.readNext());
            tokens.add(new Token(TokenType.METHOD_OP, operator.toString(), reader.getLine(), reader.getColumn()));
            return;
        }

        // Handle bitwise operators
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
                        tokens.add(new Token(TokenType.BIT_OP, operator.toString(), reader.getLine(), reader.getColumn()));
                        return; // For '<<<
                    }
                }
                tokens.add(new Token(TokenType.BIT_OP, operator.toString(), reader.getLine(), reader.getColumn()));
                return; // For '<<'
            }
    
            // Check for '>>'
            if (operator.toString().equals(">") && nextChar == '>') {
                operator.append(reader.readNext()); 
                nextChar = reader.peek();
                if (nextChar == '>') {
                    operator.append(reader.readNext()); 
                    nextChar = reader.peek();
                    if (nextChar == '>') {
                        operator.append(reader.readNext()); 
                        tokens.add(new Token(TokenType.BIT_OP, operator.toString(), reader.getLine(), reader.getColumn()));
                        return; // For '>>>'
                    }
                }
                tokens.add(new Token(TokenType.BIT_OP, operator.toString(), reader.getLine(), reader.getColumn()));
                return; // For '>>'
            }
        }
        if ((firstChar == '+' || firstChar == '-') && isUnaryContext()) {
            tokens.add(new Token(TokenType.UNARY_OP, String.valueOf(firstChar), reader.getLine(), reader.getColumn()));
            return;
        }
    
        // Handle ! as logical operator
        if (firstChar == '!' && reader.peek() != '=') {
            tokens.add(new Token(TokenType.LOG_OP, "!", reader.getLine(), reader.getColumn()));
            return;
        }
    
        // Handle ~ as bitwise operator
        if (firstChar == '~') {
            tokens.add(new Token(TokenType.BIT_OP, "~", reader.getLine(), reader.getColumn()));
            return;
        }
    
        // Handle method operator (.)
        if (firstChar == '.' && !Character.isDigit(reader.peek())) {
            tokens.add(new Token(TokenType.METHOD_OP, ".", reader.getLine(), reader.getColumn()));
            return;
        }
    
        // Handle multi-character operators
        while (isOperatorSymbol(reader.peek())) {
            char nextChar = reader.peek();
            String currentOp = operator.toString() + nextChar;
            
            // Handle >>> operator
            if (currentOp.equals(">>") && reader.peek() == '>') {
                operator.append(reader.readNext()).append(reader.readNext());
                tokens.add(new Token(TokenType.BIT_OP, ">>>", reader.getLine(), reader.getColumn()));
                return;
            }
            
            // Handle two-character operators
            if (isValidTwoCharOperator(currentOp)) {
                operator.append(reader.readNext());
                String op = operator.toString();
                TokenType type = categorizeOperator(op);
                tokens.add(new Token(type, op, reader.getLine(), reader.getColumn()));
                return;
            }
            
            break;
        }
    
        // Handle single-character operators
        String op = operator.toString();
        if (op.equals("<") || op.equals(">")) {
            tokens.add(new Token(TokenType.RELATIONAL_OP, op, reader.getLine(), reader.getColumn()));
        } else {
            TokenType type = categorizeOperator(op);
            tokens.add(new Token(type, op, reader.getLine(), reader.getColumn()));
        }
    }

    private TokenType categorizeOperator(String op) {
        return switch (op) {
            case "+", "-", "*", "/", "%" -> TokenType.ARITHMETIC_OP;
            case "%=", "?=", "=", "+=", "-=", "*=", "/=" -> TokenType.ASSIGN_OP;
            case ">", "<", ">=", "<=", "==", "!=" -> TokenType.RELATIONAL_OP;
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

    private boolean isUnaryContext() {
        if (tokens.isEmpty()) return true;
        Token lastToken = tokens.get(tokens.size() - 1);
        TokenType type = lastToken.getType();
        return type == TokenType.DELIM 
            || type == TokenType.PUNC_DELIM 
            || type == TokenType.WHITESPACE
            || type == TokenType.ARITHMETIC_OP 
            || type == TokenType.ASSIGN_OP
            || type == TokenType.RELATIONAL_OP 
            || type == TokenType.LOG_OP
            || type == TokenType.METHOD_OP 
            || type == TokenType.INHERIT_OP;
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
            tokens.add(new Token(TokenType.DELIM, String.valueOf(firstChar), reader.getLine(), reader.getColumn()));
        } else if (".;?@".indexOf(firstChar) != -1) {
            tokens.add(new Token(TokenType.PUNC_DELIM, String.valueOf(firstChar), reader.getLine(), reader.getColumn()));
        }
    }

    private void handleObjectDelimiterOrOperator(char firstChar) throws SourceReader.SourceReaderException {
        // This method remains unchanged as it correctly handles object delimiters
        // The operator part is now handled by handleOperator method
        int startLine = reader.getLine();
        int startColumn = reader.getColumn();
    
        if (firstChar == '<') {
            char nextChar = reader.peek();
    
            if (Character.isLetter(nextChar) || nextChar == '"' || nextChar == '\'') {
                tokens.add(new Token(TokenType.OBJECT_DELIM, "<", startLine, startColumn));
    
                if (nextChar == '"' || nextChar == '\'') {
                    handleStringLiteral(reader.readNext());
                } else {
                    StringBuilder typeName = new StringBuilder();
                    while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_') {
                        typeName.append(reader.readNext());
                    }
                    tokens.add(new Token(TokenType.STR_LIT, typeName.toString(), reader.getLine(), reader.getColumn()));
                }
    
                if (reader.peek() == '>') {
                    tokens.add(new Token(TokenType.OBJECT_DELIM, ">", reader.getLine(), reader.getColumn()));
                    reader.readNext();
                } else {
                    throw new SourceReader.SourceReaderException("Expected '>' to close object delimiter.");
                }
            } else {
                handleOperator(firstChar);
            }
        } else if (firstChar == '>') {
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).getType() == TokenType.OBJECT_DELIM) {
                tokens.add(new Token(TokenType.OBJECT_DELIM, ">", startLine, startColumn));
            } else {
                handleOperator(firstChar);
            }
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
        StringBuilder comment = new StringBuilder();
        comment.append(firstChar);
        if (reader.peek() == '/') { // Single-line comment
            while (reader.peek() != '\n' && reader.peek() != SourceReader.EOF) {
                comment.append(reader.readNext());
            }
        } else if (reader.peek() == '*') { // Multi-line comment
            comment.append(reader.readNext());
            while (!(comment.charAt(comment.length()-1) == '*' && reader.peek() == '/')) {
                comment.append(reader.readNext());
            }
            if (reader.peek()=='/') {
                comment.append(reader.readNext());
            }
            if (reader.peek() == SourceReader.EOF) {
                handleError("Unterminated comment");
                return;
            }
        } else {
            handleOperator('/');
            return;
        }

        tokens.add(new Token(TokenType.COMMENT, comment.toString(), reader.getLine(), reader.getColumn()));
    }

    private boolean isOperatorSymbol(char c) {
        return "+-*=%?<>!&|^~.:".indexOf(c) != -1;
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
