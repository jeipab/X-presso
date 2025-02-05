package parser.core;

import lexer.Token;
import lexer.TokenType;
import util.SyntaxErrorHandler;
import java.util.List;

/**
 * Recursive Descent Parser (RDP) for Syntax Analysis.
 * - Implements grammar-based parsing functions.
 * - Uses GrammarRule.java as the basis for parsing.
 * - Reports syntax errors and tracks line/column numbers.
 * - Constructs a ParseTree for visualization.
 */
public class RDP {
    private final List<Token> tokens;
    private final SyntaxErrorHandler errorHandler;
    private int current = 0;

    public RDP(List<Token> tokens) {
        this.tokens = tokens;
        this.errorHandler = new SyntaxErrorHandler(this);
    }

    /**
     * Main entry point for parsing the program.
     */
    public void parse() {
        parseProgram();
        errorHandler.printErrors();
    }

    /**
     * Parses the entire program (<SP_Prog> ::= <Class> | <Class> <SP_Prog>).
     */
    private void parseProgram() {
        while (!atEnd()) {
            if (!parseClass()) {
                errorHandler.reportError("Unexpected token in program.", peek().getLine(), peek().getColumn(), "Expected class definition.");
                advance();
            }
        }
    }

    /**
     * Parses a class declaration (<Class> ::= "class" Identifier ...).
     */
    private boolean parseClass() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("class")) {
            return false;
        }

        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError("Expected class name after 'class'.", previous().getLine(), previous().getColumn(), "Provide a valid identifier.");
            return false;
        }

        // Optional inheritance
        if (match(TokenType.INHERIT_OP)) {
            if (!match(TokenType.IDENTIFIER)) {
                errorHandler.reportError("Expected identifier after inheritance operator.", previous().getLine(), previous().getColumn(), "Provide a valid superclass/interface name.");
                return false;
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError("Expected '{' after class declaration.", previous().getLine(), previous().getColumn(), "Use '{' to start the class body.");
            return false;
        }

        // Parse class body
        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseMethod() && !parseField() && !parseMainFunction()) {
                errorHandler.reportError("Unexpected statement inside class.", peek().getLine(), peek().getColumn(), "Only fields, methods, or main function allowed.");
                advance();
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError("Expected '}' to close class body.", previous().getLine(), previous().getColumn(), "Ensure class is properly closed.");
            return false;
        }

        return true;
    }

    /**
     * Parses the main function (<SP_Main> ::= "public" "static" "void" "main"...).
     */
    private boolean parseMainFunction() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("main")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError("Expected '(' after 'main'.", previous().getLine(), previous().getColumn(), "Use '(' before parameters.");
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after 'main' parameters.", previous().getLine(), previous().getColumn(), "Use ')' to close parameter list.");
            return false;
        }

        return parseBlock();
    }

    /**
     * Parses a block of statements enclosed in '{' and '}'.
     */
    private boolean parseBlock() {
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError("Expected '{' to begin block.", previous().getLine(), previous().getColumn(), "Use '{' to start the block.");
            return false;
        }

        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseStatement()) {
                errorHandler.reportError("Unexpected statement inside block.", peek().getLine(), peek().getColumn(), "Check syntax inside block.");
                advance();
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError("Expected '}' to close block.", previous().getLine(), previous().getColumn(), "Use '}' to close the block.");
            return false;
        }

        return true;
    }


    /**
     * Parses a method declaration.
     */
    private boolean parseMethod() {
        if (!match(TokenType.IDENTIFIER)) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            return false;
        }

        parseParameters();

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after method parameters.", previous().getLine(), previous().getColumn(), "Use ')' to close parameter list.");
            return false;
        }

        return parseBlock();
    }

    /**
 * Parses a class field declaration.
 */
private boolean parseField() {
    if (!match(TokenType.IDENTIFIER)) {
        return false;
    }

    if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
        errorHandler.reportError("Expected ';' after field declaration.", previous().getLine(), previous().getColumn(), "Use ';' to end field declaration.");
        return false;
    }

    return true;
}


    /**
     * Parses method parameters.
     */
    private void parseParameters() {
        if (match(TokenType.IDENTIFIER)) {
            while (match(TokenType.DELIM) && previous().getLexeme().equals(",")) {
                if (!match(TokenType.IDENTIFIER)) {
                    errorHandler.reportError("Expected parameter identifier.", previous().getLine(), previous().getColumn(), "Provide valid parameter name.");
                    return;
                }
            }
        }
    }

    /**
     * Parses a statement.
     */
    private boolean parseStatement() {
        return parseExpression() 
            || parseIfStatement() 
            || parseLoop() 
            || parseAssignment()
            || parseAliasDeclaration()
            || parseQueryBlock()
            || parseSwitchStatement();
    }

    /**
     * Parses an inline query block.
     */
    private boolean parseQueryBlock() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("inline_query")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError("Expected '{' after 'inline_query'.", previous().getLine(), previous().getColumn(), "Use '{' to start query block.");
            return false;
        }

        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseQueryStatement()) {
                errorHandler.reportError("Unexpected token in query block.", peek().getLine(), peek().getColumn(), "Use 'from', 'filter_by', or 'select'.");
                advance();
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError("Expected '}' at the end of query block.", previous().getLine(), previous().getColumn(), "Use '}' to close query block.");
            return false;
        }

        return true;
    }

    
    /**
     * Parses a query statement inside an inline query block.
     */
    private boolean parseQueryStatement() {
        if (match(TokenType.KEYWORD)) {
            String keyword = previous().getLexeme();

            switch (keyword) {
                case "from":
                case "filter_by":
                case "select":
                    return parseExpression();
                default:
                    errorHandler.reportError("Unexpected keyword in query block.", previous().getLine(), previous().getColumn(), "Use 'from', 'filter_by', or 'select'.");
                    return false;
            }
        }
        return false;
    }

    /**
     * Parses a ternary expression (condition ? expr1 : expr2).
     */
    private boolean parseTernaryExpression() {
        parseExpression();

        if (!match(TokenType.TERNARY_OP) || !previous().getLexeme().equals("?")) {
            return false;
        }

        parseExpression(); // True case

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(":")) {
            errorHandler.reportError("Expected ':' in ternary expression.", previous().getLine(), previous().getColumn(), "Use ':' after true expression.");
            return false;
        }

        parseExpression(); // False case

        return true;
    }

    /**
     * Parses an assignment statement.
     */
    private boolean parseAssignment() {
        if (!match(TokenType.IDENTIFIER)) {
            return false;
        }

        if (!match(TokenType.ASSIGN_OP)) {
            errorHandler.reportError("Expected assignment operator '='.", previous().getLine(), previous().getColumn(), "Use '=' for assignment.");
            return false;
        }

        parseExpression();

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError("Expected ';' after assignment.", previous().getLine(), previous().getColumn(), "Use ';' to end assignment.");
            return false;
        }

        return true;
    }

    /**
     * Parses an alias declaration: DataType Identifier = ALIAS Identifier;
     */
    private boolean parseAliasDeclaration() {
        if (!match(TokenType.IDENTIFIER)) {
            return false;
        }

        if (!match(TokenType.ASSIGN_OP) || !previous().getLexeme().equals("=")) {
            return false;
        }

        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("ALIAS")) {
            errorHandler.reportError("Expected 'ALIAS' keyword.", previous().getLine(), previous().getColumn(), "Use 'ALIAS' to declare an alias.");
            return false;
        }

        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError("Expected identifier after 'ALIAS'.", previous().getLine(), previous().getColumn(), "Provide a valid identifier.");
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError("Expected ';' after alias declaration.", previous().getLine(), previous().getColumn(), "Use ';' to end alias declaration.");
            return false;
        }

        return true;
    }

    /**
     * Parses a function call.
     */
    private boolean parseFunctionCall() {
        if (!match(TokenType.IDENTIFIER)) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            parseExpression();
            while (match(TokenType.DELIM) && previous().getLexeme().equals(",")) {
                parseExpression();
            }

            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
                errorHandler.reportError("Expected ')' after function call arguments.", previous().getLine(), previous().getColumn(), "Use ')' to close arguments.");
                return false;
            }
        }

        return true;
    }

    /**
     * Parses an expression, including function calls, lambdas, ternary expressions, and operators.
     */
    private boolean parseExpression() {
        if (match(TokenType.IDENTIFIER)) {
            // Check if this is a function call
            if (check(TokenType.DELIM) && peek().getLexeme().equals("(")) {
                return parseFunctionCall();
            }
            // Check if this is a lambda expression
            if (check(TokenType.METHOD_OP) && peek().getLexeme().equals("->")) {
                return parseLambdaExpression();
            }
            return true;
        }

        // Handle parentheses for grouping expressions
        if (match(TokenType.DELIM) && previous().getLexeme().equals("(")) {
            boolean valid = parseExpression();
            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
                errorHandler.reportError("Expected ')' to close expression.", previous().getLine(), previous().getColumn(), "Use ')' to close.");
                return false;
            }
            return valid;
        }

        // Handle unary expressions
        if (match(TokenType.UNARY_OP)) {
            return parseExpression(); // Unary operators should be followed by an expression
        }

        // Handle literal values
        if (match(TokenType.INT_LIT) || match(TokenType.FLOAT_LIT) || parseLiteral()) {
            return true;
        }

        // Handle binary expressions (arithmetic, relational, logical)
        if (parseBinaryExpression()) {
            return true;
        }

        // Handle ternary expressions (condition ? expr1 : expr2)
        if (parseTernaryExpression()) {
            return true;
        }

        return false;
    }

    /**
     * Parses a binary expression (left operand, operator, right operand).
     */
    private boolean parseBinaryExpression() {
        // Parse the left-hand operand first
        if (!parseOperand()) {
            return false;
        }

        // Check if there is a binary operator following the operand
        while (match(TokenType.ARITHMETIC_OP) || match(TokenType.REL_OP) || match(TokenType.LOG_OP) || match(TokenType.BIT_OP)) {
            // Capture the operator
            Token operator = previous();

            // Parse the right-hand operand
            if (!parseOperand()) {
                errorHandler.reportError("Expected right-hand operand after operator '" + operator.getLexeme() + "'.", 
                    operator.getLine(), operator.getColumn(), "Provide a valid operand.");
                return false;
            }
        }

        return true;
    }

    /**
     * Parses an operand (identifier, literal, or grouped expression).
     */
    private boolean parseOperand() {
        if (match(TokenType.IDENTIFIER) || parseLiteral()) {
            return true;
        }

        // Handle grouped expressions: (expr)
        if (match(TokenType.DELIM) && previous().getLexeme().equals("(")) {
            boolean valid = parseExpression();
            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
                errorHandler.reportError("Expected ')' to close expression.", previous().getLine(), previous().getColumn(), "Use ')' to close.");
                return false;
            }
            return valid;
        }

        return false;
    }

    /**
     * Parses a lambda expression: Identifier -> Expr
     */
    private boolean parseLambdaExpression() {
        if (!match(TokenType.IDENTIFIER)) {
            return false;
        }

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals("->")) {
            return false;
        }

        return parseExpression();
    }

    /**
     * Parses a literal value (integer, float, string, date, etc.).
     */
    private boolean parseLiteral() {
        if (match(TokenType.INT_LIT) || match(TokenType.FLOAT_LIT) || match(TokenType.STR_LIT) || match(TokenType.BOOL_LIT) || match(TokenType.CHAR_LIT)) {
            return true;
        }

        if (match(TokenType.DATE_LIT)) {
            return true;
        }

        if (match(TokenType.FRAC_LIT)) {
            return true;
        }

        if (match(TokenType.COMP_LIT)) {
            return true;
        }

        return false;
    }

    /**
     * Parses a switch statement.
     */
    private boolean parseSwitchStatement() {
        if (!match(TokenType.KEYWORD) || (!previous().getLexeme().equals("switch") && !previous().getLexeme().equals("switch-fall"))) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError("Expected '(' after 'switch'.", previous().getLine(), previous().getColumn(), "Use '(' before switch expression.");
            return false;
        }

        parseExpression(); // Switch condition

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after switch expression.", previous().getLine(), previous().getColumn(), "Use ')' to close expression.");
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError("Expected '{' to open switch block.", previous().getLine(), previous().getColumn(), "Use '{' to start switch.");
            return false;
        }

        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseCase()) {
                errorHandler.reportError("Invalid case inside switch.", peek().getLine(), peek().getColumn(), "Use 'case' or 'default'.");
                advance();
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError("Expected '}' to close switch block.", previous().getLine(), previous().getColumn(), "Ensure switch ends properly.");
            return false;
        }

        return true;
    }

    /**
     * Parses a switch case or default block.
     */
    private boolean parseCase() {
        if (match(TokenType.KEYWORD) && previous().getLexeme().equals("case")) {
            parseExpression(); // Case value

            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(":")) {
                errorHandler.reportError("Expected ':' after case value.", previous().getLine(), previous().getColumn(), "Use ':' to separate case value.");
                return false;
            }

            return parseBlock();
        }

        if (match(TokenType.KEYWORD) && previous().getLexeme().equals("default")) {
            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(":")) {
                errorHandler.reportError("Expected ':' after 'default'.", previous().getLine(), previous().getColumn(), "Use ':' after 'default'.");
                return false;
            }

            return parseBlock();
        }

        return false;
    }

    /**
     * Parses an if-statement.
     */
    private boolean parseIfStatement() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("if")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError("Expected '(' after 'if'.", previous().getLine(), previous().getColumn(), "Use '(' before condition.");
            return false;
        }

        parseExpression();

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after condition.", previous().getLine(), previous().getColumn(), "Use ')' to close condition.");
            return false;
        }

        return parseBlock();
    }

    /**
     * Parses a loop statement (for, while, do-while, enhanced for).
     */
    private boolean parseLoop() {
        if (match(TokenType.KEYWORD)) {
            String keyword = previous().getLexeme();

            switch (keyword) {
                case "for":
                    return parseForLoop();
                case "while":
                    return parseWhileLoop();
                case "do":
                    // Check for enhanced for-loop (do for (...))
                    if (check(TokenType.KEYWORD) && peek().getLexeme().equals("for")) {
                        return parseEnhancedForLoop();
                    }
                    return parseDoWhileLoop();
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Parses a for-loop statement.
     */
    private boolean parseForLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("for")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError("Expected '(' after 'for'.", previous().getLine(), previous().getColumn(), "Use '(' before loop declaration.");
            return false;
        }

        parseAssignment(); // Initialization

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError("Expected ';' after initialization.", previous().getLine(), previous().getColumn(), "Use ';' after initialization.");
            return false;
        }

        parseExpression(); // Condition

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError("Expected ';' after condition.", previous().getLine(), previous().getColumn(), "Use ';' after condition.");
            return false;
        }

        parseExpression(); // Update

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after update.", previous().getLine(), previous().getColumn(), "Use ')' to close loop header.");
            return false;
        }

        return parseBlock();
    }


    /**
     * Token Utility Methods
     */
    private Token advance() {
        if (!atEnd()) current++;
        return previous();
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Parses a while-loop statement.
     */
    private boolean parseWhileLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("while")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError("Expected '(' after 'while'.", previous().getLine(), previous().getColumn(), "Use '(' before condition.");
            return false;
        }

        parseExpression(); // Condition

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after condition.", previous().getLine(), previous().getColumn(), "Use ')' to close condition.");
            return false;
        }

        return parseBlock();
    }

    /**
     * Parses a do-while loop statement.
     */
    private boolean parseDoWhileLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("do")) {
            return false;
        }

        parseBlock();

        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("while")) {
            errorHandler.reportError("Expected 'while' after 'do' block.", previous().getLine(), previous().getColumn(), "Use 'while' after the block.");
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError("Expected '(' after 'while'.", previous().getLine(), previous().getColumn(), "Use '(' before condition.");
            return false;
        }

        parseExpression(); // Condition

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after condition.", previous().getLine(), previous().getColumn(), "Use ')' to close condition.");
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError("Expected ';' after while-loop.", previous().getLine(), previous().getColumn(), "Use ';' after 'while'.");
            return false;
        }

        return true;
    }

    /**
     * Parses an enhanced for-loop (for-each style).
     */
    private boolean parseEnhancedForLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("do")) {
            return false;
        }

        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("for")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError("Expected '(' after 'do for'.", previous().getLine(), previous().getColumn(), "Use '(' to start loop.");
            return false;
        }

        parseExpression(); // Variable or collection

        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals(":")) {
            errorHandler.reportError("Expected ':' in enhanced for-loop.", previous().getLine(), previous().getColumn(), "Use ':' to separate variable and collection.");
            return false;
        }

        parseExpression(); // Collection to iterate over

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError("Expected ')' after enhanced for-loop.", previous().getLine(), previous().getColumn(), "Use ')' to close loop.");
            return false;
        }

        return parseBlock();
    }

    private boolean check(TokenType type) {
        return !atEnd() && peek().getType() == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean atEnd() {
        return current >= tokens.size();
    }
}
