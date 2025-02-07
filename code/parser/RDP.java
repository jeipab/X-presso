package parser;

import lexer.Token;
import lexer.TokenType;
import util.SyntaxErrorHandler;
import java.util.List;

/**
 * Comprehensive Recursive Descent Parser implementing the complete grammar
 * including error recovery and proper operator precedence handling.
 */
public class RDP {
    private final List<Token> tokens;
    private final SyntaxErrorHandler errorHandler;
    private int current = 0;
    private int nestingLevel = 0;

    public RDP(List<Token> tokens) {
        this.tokens = tokens;
        this.errorHandler = new SyntaxErrorHandler(this);
    }

    /**
     * Main parsing entry point.
     * <SP_Prog> ::= <Class> | <Class> <SP_Prog>
     */
    public void parse() {
        try {
            while (!atEnd()) {
                if (!parseClass()) {
                    errorHandler.reportError(
                        "Expected class definition", 
                        peek().getLine(), 
                        peek().getColumn(),
                        "Start with a class definition"
                    );
                    // Move to next token before synchronizing
                    advance();
                    synchronize();
                }
            }
        } finally {
            errorHandler.printErrors();
        }
    }

    // ======== Error Recovery Methods ========

    /**
     * Synchronizes parser state after error
     */
    private void synchronize() {
        while (!atEnd()) {
            if (previous().getLexeme().equals(";")) return;
            if (peek().getLexeme().equals("}")) return;
            
            switch (peek().getType()) {
                case KEYWORD:
                    String lexeme = peek().getLexeme();
                    if (lexeme.equals("if") || lexeme.equals("while") || 
                        lexeme.equals("for") || lexeme.equals("switch") ||
                        lexeme.equals("return")) {
                        return;
                    }
                    break;
                case RESERVED:
                    if (peek().getLexeme().equals("class")) {
                        return;
                    }
                    break;
                default:
                    break;
            }
            advance();
        }
    }

    private void enterBlock() {
        nestingLevel++;
    }

    private void exitBlock() {
        nestingLevel--;
        if (nestingLevel < 0) {
            errorHandler.reportError(
                "Unmatched closing brace",
                previous().getLine(),
                previous().getColumn(),
                "Check for missing opening brace"
            );
            nestingLevel = 0;
        }
    }

    // ======== Expression Parsing (With Precedence Levels) ========

    /**
     * Entry point for expression parsing
     * Starts with lowest precedence (assignment)
     */
    private boolean parseExpression() {
        return parseAssignment();
    }

    /**
     * Level 17: Assignment operators (lowest precedence)
     * Right-to-left associative
     */
    private boolean parseAssignment() {
        Token identifier = peek();
        boolean success = parseTernary();
        
        if (match(TokenType.ASSIGN_OP)) {
            String operator = previous().getLexeme();
            if (!parseAssignment()) {
                errorHandler.reportError(
                    "Invalid right-hand side in assignment",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after " + operator
                );
                return false;
            }
        }
        
        return success;
    }

    /**
     * Level 16: Ternary operator
     * Right-to-left associative
     */
    private boolean parseTernary() {
        if (!parseLogicalOr()) return false;

        if (match(TokenType.TERNARY_OP)) {
            if (!parseExpression()) {
                errorHandler.reportError(
                    "Missing expression after '?'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide an expression for the true case"
                );
                return false;
            }

            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(":")) {
                errorHandler.reportError(
                    "Missing ':' in ternary expression",
                    peek().getLine(),
                    peek().getColumn(),
                    "Add ':' followed by the false case expression"
                );
                return false;
            }

            if (!parseExpression()) {
                errorHandler.reportError(
                    "Missing expression after ':'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide an expression for the false case"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 15: Logical OR
     * Left-to-right associative
     */
    private boolean parseLogicalOr() {
        if (!parseLogicalAnd()) return false;

        while (match(TokenType.LOG_OP) && previous().getLexeme().equals("||")) {
            if (!parseLogicalAnd()) {
                errorHandler.reportError(
                    "Invalid right operand for '||'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '||'"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 14: Logical AND
     * Left-to-right associative
     */
    private boolean parseLogicalAnd() {
        if (!parseEquality()) return false;

        while (match(TokenType.LOG_OP) && previous().getLexeme().equals("&&")) {
            if (!parseEquality()) {
                errorHandler.reportError(
                    "Invalid right operand for '&&'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '&&'"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 13: Equality operators
     * Left-to-right associative
     */
    private boolean parseEquality() {
        if (!parseRelational()) return false;

        while (match(TokenType.REL_OP) && 
                (previous().getLexeme().equals("==") || 
                previous().getLexeme().equals("!="))) {
            if (!parseRelational()) {
                errorHandler.reportError(
                    "Invalid right operand for equality comparison",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after equality operator"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 12: Relational operators
     * Left-to-right associative
     */
    private boolean parseRelational() {
        if (!parseBitwiseOr()) return false;

        while (match(TokenType.REL_OP) && 
                (previous().getLexeme().equals("<") || 
                previous().getLexeme().equals(">") ||
                previous().getLexeme().equals("<=") || 
                previous().getLexeme().equals(">="))) {
            if (!parseBitwiseOr()) {
                errorHandler.reportError(
                    "Invalid right operand for relational comparison",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after relational operator"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Levels 9-11: Bitwise operators
     * Left-to-right associative
     */
    private boolean parseBitwiseOr() {
        if (!parseBitwiseXor()) return false;

        while (match(TokenType.BIT_OP) && previous().getLexeme().equals("|")) {
            if (!parseBitwiseXor()) {
                errorHandler.reportError(
                    "Invalid right operand for '|'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '|'"
                );
                return false;
            }
        }

        return true;
    }

    private boolean parseBitwiseXor() {
        if (!parseBitwiseAnd()) return false;

        while (match(TokenType.BIT_OP) && previous().getLexeme().equals("^")) {
            if (!parseBitwiseAnd()) {
                errorHandler.reportError(
                    "Invalid right operand for '^'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '^'"
                );
                return false;
            }
        }

        return true;
    }

    private boolean parseBitwiseAnd() {
        if (!parseShift()) return false;

        while (match(TokenType.BIT_OP) && previous().getLexeme().equals("&")) {
            if (!parseShift()) {
                errorHandler.reportError(
                    "Invalid right operand for '&'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '&'"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 8: Shift operators
     * Left-to-right associative
     */
    private boolean parseShift() {
        if (!parseAdditive()) return false;

        while (match(TokenType.BIT_OP) && 
                (previous().getLexeme().equals("<<") || 
                previous().getLexeme().equals(">>") ||
                previous().getLexeme().equals(">>>"))) {
            if (!parseAdditive()) {
                errorHandler.reportError(
                    "Invalid right operand for shift operator",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after shift operator"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 7: Additive operators
     * Left-to-right associative
     */
    private boolean parseAdditive() {
        if (!parseMultiplicative()) return false;

        while (match(TokenType.ARITHMETIC_OP) && 
                (previous().getLexeme().equals("+") || 
                previous().getLexeme().equals("-"))) {
            if (!parseMultiplicative()) {
                errorHandler.reportError(
                    "Invalid right operand for additive operator",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '+' or '-'"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 6: Multiplicative operators
     * Left-to-right associative
     */
    private boolean parseMultiplicative() {
        if (!parseExponential()) return false;

        while (match(TokenType.ARITHMETIC_OP) && 
                (previous().getLexeme().equals("*") || 
                previous().getLexeme().equals("/") ||
                previous().getLexeme().equals("%"))) {
            if (!parseExponential()) {
                errorHandler.reportError(
                    "Invalid right operand for multiplicative operator",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '*', '/', or '%'"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 5: Exponential operator
     * Right-to-left associative
     */
    private boolean parseExponential() {
        if (!parseUnary()) return false;

        while (match(TokenType.ARITHMETIC_OP) && previous().getLexeme().equals("^")) {
            if (!parseUnary()) {  // Right associative, so recurse to parseUnary
                errorHandler.reportError(
                    "Invalid right operand for '^'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after '^'"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 4: Prefix/Unary operators
     * Right-to-left associative
     */
    private boolean parseUnary() {
        if (match(TokenType.UNARY_OP)) {
            String op = previous().getLexeme();
            if (!parseUnary()) {  // Right associative, so recurse
                errorHandler.reportError(
                    "Invalid operand for unary operator " + op,
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression after " + op
                );
                return false;
            }
            return true;
        }

        return parsePostfix();
    }

    /**
     * Level 3: Postfix operators
     * Left-to-right associative
     */
    private boolean parsePostfix() {
        if (!parseAccess()) return false;

        while (match(TokenType.UNARY_OP) && 
                (previous().getLexeme().equals("++") || 
                previous().getLexeme().equals("--") ||
                previous().getLexeme().equals("**"))) {
            // Postfix operators don't need right operands
            continue;
        }

        return true;
    }

    /**
     * Level 2: Access operators
     * Left-to-right associative
     */
    private boolean parseAccess() {
        if (!parsePrimary()) return false;

        while (match(TokenType.METHOD_OP)) {
            String op = previous().getLexeme();
            if (!parsePrimary()) {
                errorHandler.reportError(
                    "Invalid right operand for " + op,
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid identifier after " + op
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Level 1: Primary expressions and grouping
     * Handles literals, identifiers, and parenthesized expressions
     */
    private boolean parsePrimary() {
        if (match(TokenType.IDENTIFIER)) return true;
        if (parseLiteral()) return true;

        if (match(TokenType.DELIM) && previous().getLexeme().equals("(")) {
            if (!parseExpression()) {
                errorHandler.reportError(
                    "Invalid expression in parentheses",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid expression between parentheses"
                );
                return false;
            }

            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
                errorHandler.reportError(
                    "Missing closing parenthesis",
                    peek().getLine(),
                    peek().getColumn(),
                    "Add closing parenthesis"
                );
                return false;
            }

            return true;
        }

        return false;
    }

    private boolean parseBlock() {
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError(
                "Expected '{' to begin block",
                peek().getLine(),
                peek().getColumn(),
                "Add opening brace"
            );
            return false;
        }
    
        enterBlock();
        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseStatement()) {
                errorHandler.reportError(
                    "Invalid statement in block",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide valid statements inside block"
                );
                synchronize();
            }
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError(
                "Expected '}' to close block",
                peek().getLine(),
                peek().getColumn(),
                "Add closing brace"
            );
            return false;
        }
    
        exitBlock();
        return true;
    }

    // ======== Statement Parsing ========

    /**
     * Parses statements according to grammar rules
     * <Statements> ::= <Dec_State> | <In_State> | <Out_State> | <Ass_State>
     *                | <Con_State> | <Iter_State>
     */
    private boolean parseStatement() {
        try {
            // Control statements
            if (match(TokenType.KEYWORD)) {
                switch (previous().getLexeme()) {
                    case "if": return parseIfStatement();
                    case "switch":
                    case "switch-fall": return parseSwitchStatement();
                    case "while": return parseWhileLoop();
                    case "for": return parseForLoop();
                    case "do": 
                        if (check(TokenType.KEYWORD) && peek().getLexeme().equals("for")) {
                            return parseEnhancedForLoop();
                        }
                        return parseDoWhileLoop();
                    case "inline_query": return parseQueryBlock();
                    case "break":
                    case "exit":
                        // Handle break and exit statements
                        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
                            errorHandler.reportError(
                                "Missing semicolon after " + previous().getLexeme(),
                                peek().getLine(),
                                peek().getColumn(),
                                "Add semicolon to end statement"
                            );
                        }
                        return true;
                }
            }
    
            // Inspect blocks
            if (match(TokenType.RESERVED) && previous().getLexeme().equals("inspect")) {
                return parseInspectBlock();
            }
    
            // Declaration statements
            if (parseSingleDeclaration() || parseMultiDeclaration()) {
                return true;
            }
    
            // Input/Output statements
            if (parseInputStatement() || parseOutputStatement()) {
                return true;
            }
    
            // Export expressions
            if (match(TokenType.IDENTIFIER)) {
                Token identifier = previous();
                if (match(TokenType.METHOD_OP) && previous().getLexeme().equals(".")) {
                    if (match(TokenType.RESERVED) && previous().getLexeme().equals("export_as")) {
                        // Reset position if not a valid export expression
                        int startPos = current - 3;
                        if (!parseExportExpression()) {
                            current = startPos;
                        } else {
                            return true;
                        }
                    }
                }
                // Reset position if not an export
                current = current - 2;
            }
    
            // Assignment statements
            if (parseAssignmentStatement()) {
                return true;
            }
    
            // Filter expressions and data operations
            if (parseFilterExpression() || parseValidateExpression() || 
                parseDateFunction() || parseModifyBlock() ||
                parseToMixedExpression() || parseAliasDeclaration()) {
                return true;
            }
    
            return false;
    
        } catch (Exception e) {
            synchronize();
            return false;
        }
    }

    /**
     * Parses a valid expression inside filter/select clauses.
     */
    private boolean parseQueryExpression() {
        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid expression inside query clause",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid filter or selection expression"
            );
            return false;
        }
        return true;
    }

    /**
     * Parses the order_by clause in query statements.
     * <Order_Clause> ::= "order_by" "(" <Identifier> ["," <Sort_Order>] ")"
     */
    private boolean parseOrderByClause() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("order_by")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'order_by'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected identifier (column name) in order_by",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid column identifier"
            );
            return false;
        }

        // Optional sorting order (ASC or DESC)
        if (match(TokenType.DELIM) && previous().getLexeme().equals(",")) {
            if (!match(TokenType.IDENTIFIER) || 
                !(previous().getLexeme().equals("asc") || previous().getLexeme().equals("desc"))) {
                errorHandler.reportError(
                    "Expected sorting order ('asc' or 'desc')",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide either 'asc' or 'desc'"
                );
                return false;
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after order_by parameters",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses the limit clause in query statements.
     * <Limit_Clause> ::= "limit" "(" <Int_Lit> ")"
     */
    private boolean parseLimitClause() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("limit")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'limit'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        if (!match(TokenType.INT_LIT)) {
            errorHandler.reportError(
                "Expected integer value in limit",
                peek().getLine(),
                peek().getColumn(),
                "Provide a numeric limit value"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after limit value",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        return true;
    }

    private boolean parseIfStatement() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("if")) {
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'if'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid condition in if statement",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid boolean expression"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after condition",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        if (!parseBlock()) {
            errorHandler.reportError(
                "Invalid body in if statement",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid block of statements"
            );
            return false;
        }
    
        return true;
    }
    
    private boolean parseSwitchStatement() {
        if (!match(TokenType.KEYWORD) || (!previous().getLexeme().equals("switch") &&
                                            !previous().getLexeme().equals("switch-fall"))) {
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'switch'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid switch condition",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid expression"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after switch condition",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError(
                "Expected '{' to open switch block",
                peek().getLine(),
                peek().getColumn(),
                "Add opening brace"
            );
            return false;
        }
    
        enterBlock();
        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseSwitchCase()) {
                errorHandler.reportError(
                    "Invalid case statement in switch",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid case or default statement"
                );
                synchronize();
            }
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError(
                "Expected '}' to close switch block",
                peek().getLine(),
                peek().getColumn(),
                "Add closing brace"
            );
            return false;
        }
    
        exitBlock();
        return true;
    }

    private boolean parseSwitchCase() {
        if (match(TokenType.KEYWORD)) {
            String lexeme = previous().getLexeme();
            if (lexeme.equals("case")) {
                if (!match(TokenType.STR_LIT) && !match(TokenType.INT_LIT) &&
                    !match(TokenType.CHAR_LIT) && !match(TokenType.FLOAT_LIT)) {
                    errorHandler.reportError(
                        "Invalid case value",
                        peek().getLine(),
                        peek().getColumn(),
                        "Provide a valid case literal"
                    );
                    return false;
                }
    
                if (!match(TokenType.PUNC_DELIM) || !previous().getLexeme().equals(":")) {
                    errorHandler.reportError(
                        "Expected ':' after case value",
                        peek().getLine(),
                        peek().getColumn(),
                        "Add colon to separate case value and block"
                    );
                    return false;
                }
    
                return parseBlock();
            } else if (lexeme.equals("default")) {
                if (!match(TokenType.PUNC_DELIM) || !previous().getLexeme().equals(":")) {
                    errorHandler.reportError(
                        "Expected ':' after 'default'",
                        peek().getLine(),
                        peek().getColumn(),
                        "Add colon to separate default case and block"
                    );
                    return false;
                }
    
                return parseBlock();
            }
        }
    
        return false;
    }
    
    private boolean parseWhileLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("while")) {
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'while'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid condition in while loop",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid boolean expression"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after condition",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        return parseBlock();
    }    

    private boolean parseDoWhileLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("do")) {
            return false;
        }
    
        if (!parseBlock()) {
            errorHandler.reportError(
                "Invalid do-while loop body",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid block of statements"
            );
            return false;
        }
    
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("while")) {
            errorHandler.reportError(
                "Expected 'while' after do-while block",
                peek().getLine(),
                peek().getColumn(),
                "Use 'while' to close the loop"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'while'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid condition in do-while loop",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid boolean expression"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after condition",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Expected ';' after do-while loop",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon after loop condition"
            );
            return false;
        }
    
        return true;
    }

    private boolean parseForLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("for")) {
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'for'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        // Optional initialization statement
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            if (!parseAssignmentStatement()) {
                errorHandler.reportError(
                    "Invalid initialization in for loop",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid initialization statement"
                );
                return false;
            }
        }
    
        // Condition expression
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            if (!parseExpression()) {
                errorHandler.reportError(
                    "Invalid condition in for loop",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid boolean expression"
                );
                return false;
            }
        }
    
        // Optional increment statement
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            if (!parseAssignmentStatement()) {
                errorHandler.reportError(
                    "Invalid update statement in for loop",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid update statement"
                );
                return false;
            }
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after for loop header",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        return parseBlock();
    }

    private boolean parseEnhancedForLoop() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("do")) {
            return false;
        }
    
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("for")) {
            errorHandler.reportError(
                "Expected 'for' after 'do'",
                peek().getLine(),
                peek().getColumn(),
                "Use 'do for' for enhanced loop"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'do for'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        if (!parseDataType()) {
            errorHandler.reportError(
                "Expected type in enhanced for loop",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid data type"
            );
            return false;
        }
    
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected loop variable in enhanced for loop",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid identifier"
            );
            return false;
        }
    
        if (!match(TokenType.PUNC_DELIM) || !previous().getLexeme().equals(":")) {
            errorHandler.reportError(
                "Expected ':' in enhanced for loop",
                peek().getLine(),
                peek().getColumn(),
                "Use ':' to iterate over collection"
            );
            return false;
        }
    
        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid iterable in enhanced for loop",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid collection or range"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after enhanced for loop",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        return parseBlock();
    }

    /**
     * Parses a single declaration
     * <Single_Dec> ::= <Parameter>; | <Data_Type> <Ass_State>; | 
     *                  <Data_Type> <Ass_State>; <Dec_State>
     */
    private boolean parseSingleDeclaration() {
        if (!parseDataType()) return false;

        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected identifier after type",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid identifier name"
            );
            return false;
        }

        // Check for assignment
        if (match(TokenType.ASSIGN_OP)) {
            if (!parseExpression()) {
                errorHandler.reportError(
                    "Invalid expression in declaration",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid initialization value"
                );
                return false;
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Missing semicolon after declaration",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end declaration"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses multiple declarations
     * <Multi_Dec> ::= <Data_Type> <Multi_Identifier>;
     */
    private boolean parseMultiDeclaration() {
        if (!parseDataType()) return false;

        do {
            if (!match(TokenType.IDENTIFIER)) {
                errorHandler.reportError(
                    "Expected identifier in declaration",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid identifier"
                );
                return false;
            }

            // Optional assignment
            if (match(TokenType.ASSIGN_OP)) {
                if (!parseExpression()) {
                    errorHandler.reportError(
                        "Invalid expression in declaration",
                        peek().getLine(),
                        peek().getColumn(),
                        "Provide a valid initialization value"
                    );
                    return false;
                }
            }

        } while (match(TokenType.DELIM) && previous().getLexeme().equals(","));

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Missing semicolon after declaration list",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end declaration"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses data types
     * <Data_Type> ::= "int" | "char" | "bool" | "str" | "float" | "double"
     *               | "long" | "byte" | "Date" | "Frac" | "Complex"
     */
    private boolean parseDataType() {
        if (!match(TokenType.RESERVED)) return false;
        String type = previous().getLexeme();
        return type.equals("int") || type.equals("char") || type.equals("bool") ||
                type.equals("str") || type.equals("float") || type.equals("double") ||
                type.equals("long") || type.equals("byte") || type.equals("Date") ||
                type.equals("Frac") || type.equals("Complex");
    }

    /**
     * Parses output statements
     * <Out_State> ::= "Output" "::" "print" (<Identifier>)";" | 
     *                 "Output" "::" "print" "("<str>" "+" "<Data_Type>")" ";"
     */
    private boolean parseOutputStatement() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("Output")) {
            return false;
        }

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals("::")) {
            errorHandler.reportError(
                "Expected '::' after 'Output'",
                peek().getLine(),
                peek().getColumn(),
                "Use '::' for method reference"
            );
            return false;
        }

        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("print")) {
            errorHandler.reportError(
                "Expected 'print' in output statement",
                peek().getLine(),
                peek().getColumn(),
                "Use 'print()' for output"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'print'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        // Parse either an identifier or a string concatenation
        if (match(TokenType.IDENTIFIER)) {
            // Simple identifier output
        } else if (match(TokenType.STR_LIT)) {
            if (match(TokenType.ARITHMETIC_OP) && previous().getLexeme().equals("+")) {
                if (!parseExpression()) {
                    errorHandler.reportError(
                        "Invalid expression after '+'",
                        peek().getLine(),
                        peek().getColumn(),
                        "Provide a valid expression for concatenation"
                    );
                    return false;
                }
            }
        } else {
            errorHandler.reportError(
                "Expected identifier or string in print statement",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid output expression"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' in print statement",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Missing semicolon after print statement",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end statement"
            );
            return false;
        }

        return true;
    }

    private boolean parseInputStatement() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("Input")) {
            return false;
        }
    
        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals("::")) {
            errorHandler.reportError(
                "Expected '::' after 'Input'",
                peek().getLine(),
                peek().getColumn(),
                "Use '::' for method reference"
            );
            return false;
        }
    
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("get")) {
            errorHandler.reportError(
                "Expected 'get' in input statement",
                peek().getLine(),
                peek().getColumn(),
                "Use 'get()' for input"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'get'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        if (!match(TokenType.STR_LIT) && !match(TokenType.DELIM) && !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected prompt string or empty parentheses",
                peek().getLine(),
                peek().getColumn(),
                "Provide a string or leave empty"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' in input statement",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Missing semicolon after input statement",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end statement"
            );
            return false;
        }
    
        return true;
    }    

    /**
     * Parses filter expressions
     * <Filter_Expr> ::= <Identifier> "." "filter_by" "(" <Lambda_Expr> ")"
     */
    private boolean parseFilterExpression() {
        if (!match(TokenType.IDENTIFIER)) return false;

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals(".")) {
            return false;
        }

        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("filter_by")) {
            errorHandler.reportError(
                "Expected 'filter_by' in filter expression",
                peek().getLine(),
                peek().getColumn(),
                "Use 'filter_by' for filtering"
            );
            return false;
        }

        return parseLambdaExpression();
    }

    /**
     * Parses validate expressions
     * <Validate_Expr> ::= <Identifier> "." "validate" "(" <Lambda_Expr> ")"
     */
    private boolean parseValidateExpression() {
        if (!match(TokenType.IDENTIFIER)) return false;

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals(".")) {
            return false;
        }

        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("validate")) {
            errorHandler.reportError(
                "Expected 'validate' in validation expression",
                peek().getLine(),
                peek().getColumn(),
                "Use 'validate' for validation"
            );
            return false;
        }

        return parseLambdaExpression();
    }

    /**
     * Parses inspection blocks
     * <Inspect_Block> ::= "inspect" "{" <Statement>* "}"
     */
    private boolean parseInspectBlock() {
        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("inspect")) {
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError(
                "Expected '{' after 'inspect'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening brace"
            );
            return false;
        }

        enterBlock();
        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseStatement()) {
                errorHandler.reportError(
                    "Invalid statement in inspect block",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide valid statements within inspect block"
                );
                synchronize();
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError(
                "Expected '}' to close inspect block",
                peek().getLine(),
                peek().getColumn(),
                "Add closing brace"
            );
            return false;
        }
        exitBlock();

        return true;
    }

    /**
     * Parses date functions
     * <Date_Func> ::= <Date_Obj> "." <Date_Op> ["(" <Date_Lit> ")"]
     */
    private boolean parseDateFunction() {
        if (!match(TokenType.IDENTIFIER) && !match(TokenType.RESERVED)) {
            return false;
        }

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals(".")) {
            return false;
        }

        if (!match(TokenType.RESERVED)) {
            errorHandler.reportError(
                "Expected date operation",
                peek().getLine(),
                peek().getColumn(),
                "Use a valid date operation (before, after, year, month, day, today)"
            );
            return false;
        }

        String dateOp = previous().getLexeme();
        if (!isValidDateOp(dateOp)) {
            errorHandler.reportError(
                "Invalid date operation: " + dateOp,
                previous().getLine(),
                previous().getColumn(),
                "Use a valid date operation"
            );
            return false;
        }

        // Optional date literal parameter
        if (match(TokenType.DELIM) && previous().getLexeme().equals("(")) {
            if (!match(TokenType.DATE_LIT)) {
                errorHandler.reportError(
                    "Expected date literal",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid date literal [YYYY|MM|DD]"
                );
                return false;
            }

            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
                errorHandler.reportError(
                    "Expected ')' after date literal",
                    peek().getLine(),
                    peek().getColumn(),
                    "Add closing parenthesis"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Parses modify blocks
     * <Modify_Block> ::= <Identifier> "." "modify" "(" <Lambda_Block> ")"
     */
    private boolean parseModifyBlock() {
        if (!match(TokenType.IDENTIFIER)) return false;

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals(".")) {
            return false;
        }

        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("modify")) {
            errorHandler.reportError(
                "Expected 'modify' in modify block",
                peek().getLine(),
                peek().getColumn(),
                "Use 'modify' for modification operations"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'modify'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        if (!parseLambdaBlock()) {
            errorHandler.reportError(
                "Invalid lambda block in modify expression",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid lambda block"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after modify block",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses query blocks
     * <Query_Block> ::= "inline_query" "{" <Query_Statement>* "}"
     */
    private boolean parseQueryBlock() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("inline_query")) {
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError(
                "Expected '{' after 'inline_query'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening brace"
            );
            return false;
        }
    
        enterBlock();
        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseQueryStatement()) {
                errorHandler.reportError(
                    "Invalid query statement",
                    peek().getLine(),
                    peek().getColumn(),
                    "Use valid query statements"
                );
                synchronize();
            }
            
            // Optional order by clause
            parseOrderByClause();
            
            // Optional limit clause
            parseLimitClause();
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError(
                "Expected '}' to close query block",
                peek().getLine(),
                peek().getColumn(),
                "Add closing brace"
            );
            return false;
        }
        exitBlock();
    
        return true;
    }

    /**
     * Parses individual query statements
     * <Query_Statement> ::= <From_Clause> | <Filter_Clause> | <Select_Clause>
     */
    private boolean parseQueryStatement() {
        if (match(TokenType.KEYWORD)) {
            switch (previous().getLexeme()) {
                case "from":
                    return parseFromClause();
                case "filter_by":
                    return parseFilterClause();
                case "select":
                    return parseSelectClause();
                default:
                    errorHandler.reportError(
                        "Invalid query statement keyword",
                        previous().getLine(),
                        previous().getColumn(),
                        "Use 'from', 'filter_by', or 'select'"
                    );
                    return false;
            }
        }
        return false;
    }

    /**
     * Parses from clause in query statements
     * <From_Clause> ::= "from" <Identifier> ";"
     */
    private boolean parseFromClause() {
        // "from" already matched in parseQueryStatement

        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected identifier after 'from'",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid data source identifier"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Expected semicolon after from clause",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end clause"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses filter clause in query statements
     * <Filter_Clause> ::= "filter_by" "(" <Lambda_Expr> ")" ";"
     */
    private boolean parseFilterClause() {
        // "filter_by" already matched in parseQueryStatement

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'filter_by'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        if (!parseLambdaExpression()) {
            errorHandler.reportError(
                "Invalid lambda expression in filter clause",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid filter condition"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after filter expression",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Expected semicolon after filter clause",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end clause"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses select clause in query statements
     * <Select_Clause> ::= "select" "(" <Lambda_Expr> ")" ";"
     */
    private boolean parseSelectClause() {
        // "select" already matched in parseQueryStatement

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'select'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        if (!parseLambdaExpression()) {
            errorHandler.reportError(
                "Invalid lambda expression in select clause",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid selection expression"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after select expression",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Expected semicolon after select clause",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end clause"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses export expressions
     * <Export_Expr> ::= <Identifier> "." "export_as" "(" <Str_Lit> "," <Str_Lit> ")"
     */
    private boolean parseExportExpression() {
        if (!match(TokenType.IDENTIFIER)) return false;

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals(".")) {
            return false;
        }

        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("export_as")) {
            errorHandler.reportError(
                "Expected 'export_as' in export expression",
                peek().getLine(),
                peek().getColumn(),
                "Use 'export_as' for export operations"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'export_as'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        // First string literal (format)
        if (!match(TokenType.STR_LIT)) {
            errorHandler.reportError(
                "Expected string literal for export format",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid export format string"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(",")) {
            errorHandler.reportError(
                "Expected comma after format string",
                peek().getLine(),
                peek().getColumn(),
                "Add comma between parameters"
            );
            return false;
        }

        // Second string literal (path)
        if (!match(TokenType.STR_LIT)) {
            errorHandler.reportError(
                "Expected string literal for export path",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid export path string"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after export parameters",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses ToMixed expressions
     * <ToMixed_Expr> ::= <Identifier> "." "toMixed" "(" ")"
     */
    private boolean parseToMixedExpression() {
        if (!match(TokenType.IDENTIFIER)) return false;

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals(".")) {
            return false;
        }

        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("toMixed")) {
            errorHandler.reportError(
                "Expected 'toMixed' in conversion expression",
                peek().getLine(),
                peek().getColumn(),
                "Use 'toMixed' for case conversion"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'toMixed'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after toMixed",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses lambda expressions
     * <Lambda_Expr> ::= <Identifier> "->" <Expr>
     */
    private boolean parseLambdaExpression() {
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected identifier in lambda expression",
                peek().getLine(),
                peek().getColumn(),
                "Provide a parameter name"
            );
            return false;
        }

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals("->")) {
            errorHandler.reportError(
                "Expected '->' in lambda expression",
                peek().getLine(),
                peek().getColumn(),
                "Use '->' to define lambda"
            );
            return false;
        }

        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid expression in lambda",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid lambda body expression"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses lambda blocks
     * <Lambda_Block> ::= <Identifier> "->" "{" <Statement>* "}"
     */
    private boolean parseLambdaBlock() {
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected identifier in lambda block",
                peek().getLine(),
                peek().getColumn(),
                "Provide a parameter name"
            );
            return false;
        }

        if (!match(TokenType.METHOD_OP) || !previous().getLexeme().equals("->")) {
            errorHandler.reportError(
                "Expected '->' in lambda block",
                peek().getLine(),
                peek().getColumn(),
                "Use '->' to define lambda"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError(
                "Expected '{' in lambda block",
                peek().getLine(),
                peek().getColumn(),
                "Add opening brace"
            );
            return false;
        }

        enterBlock();
        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseStatement()) {
                errorHandler.reportError(
                    "Invalid statement in lambda block",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide valid statements in lambda body"
                );
                synchronize();
            }
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError(
                "Expected '}' to close lambda block",
                peek().getLine(),
                peek().getColumn(),
                "Add closing brace"
            );
            return false;
        }
        exitBlock();

        return true;
    }

    /**
     * Parses literals
     * <Literal> ::= <Int_Lit> | <Float_Lit> | <Str_Lit> | <Char_Lit>
     *             | <Bool_Lit> | <Date_Lit> | <Frac_Lit> | <Comp_Lit>
     */
    private boolean parseLiteral() {
        if (match(TokenType.INT_LIT) || match(TokenType.FLOAT_LIT) ||
            match(TokenType.STR_LIT) || match(TokenType.CHAR_LIT) ||
            match(TokenType.BOOL_LIT)) {
            return true;
        }

        // Handle special literals that might need additional validation
        return parseDateLiteral() || parseFractionLiteral() || parseComplexLiteral();
    }

    /**
     * Parses date literals
     * <Date_Lit> ::= "[" <Year> "|" <Month> "|" <Day> "]"
     */
    private boolean parseDateLiteral() {
        if (!match(TokenType.DATE_LIT)) {
            return false;
        }

        // The lexer has already validated the date format [YYYY|MM|DD]
        return true;
    }

    /**
     * Parses fraction literals
     * <Frac_Lit> ::= "[" <Int_Lit> "|" <Int_Lit> "]"
     */
    private boolean parseFractionLiteral() {
        if (!match(TokenType.FRAC_LIT)) {
            return false;
        }

        // The lexer has already validated the fraction format [n|d]
        return true;
    }

    /**
     * Parses complex number literals
     * <Comp_Lit> ::= "(" <Number> "," <Number> ")"
     */
    private boolean parseComplexLiteral() {
        if (!match(TokenType.COMP_LIT)) {
            return false;
        }

        // The lexer has already validated the complex number format (a,b)
        return true;
    }

    /**
     * Parses assignment statements
     * <Ass_State> ::= <Identifier> <Assign_Op> <Expr>;
     */
    private boolean parseAssignmentStatement() {
        if (!match(TokenType.IDENTIFIER)) {
            return false;
        }

        if (!match(TokenType.ASSIGN_OP)) {
            errorHandler.reportError(
                "Expected assignment operator",
                peek().getLine(),
                peek().getColumn(),
                "Use '=', '+=', '-=', '*=', '/=', '%=', or '?='"
            );
            return false;
        }

        if (!parseExpression()) {
            errorHandler.reportError(
                "Invalid expression in assignment",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid expression after the assignment operator"
            );
            return false;
        }

        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Expected semicolon after assignment",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end assignment statement"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses alias declarations
     * <Alias_Dec> ::= <Data_Type> <Identifier> "=" "ALIAS" <Identifier>
     */
    private boolean parseAliasDeclaration() {
        // Parse data type
        if (!parseDataType()) {
            return false;
        }

        // Parse alias name
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected identifier for alias name",
                peek().getLine(),
                peek().getColumn(),
                "Provide a name for the alias"
            );
            return false;
        }

        // Parse assignment
        if (!match(TokenType.ASSIGN_OP) || !previous().getLexeme().equals("=")) {
            errorHandler.reportError(
                "Expected '=' in alias declaration",
                peek().getLine(),
                peek().getColumn(),
                "Use '=' before ALIAS keyword"
            );
            return false;
        }

        // Parse ALIAS keyword
        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("ALIAS")) {
            errorHandler.reportError(
                "Expected 'ALIAS' keyword",
                peek().getLine(),
                peek().getColumn(),
                "Use 'ALIAS' keyword for type aliasing"
            );
            return false;
        }

        // Parse target type identifier
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected identifier after ALIAS",
                peek().getLine(),
                peek().getColumn(),
                "Provide the type to alias"
            );
            return false;
        }

        // Parse ending semicolon
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Expected semicolon after alias declaration",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end alias declaration"
            );
            return false;
        }

        return true;
    }

    /**
     * Parses type constraints used in declarations.
     * <Type_Constraint> ::= "where type" <Identifier> ["," <Identifier>]*
     */
    private boolean parseTypeConstraint() {
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("where")) {
            return false;
        }

        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("type")) {
            errorHandler.reportError(
                "Expected 'type' after 'where'",
                peek().getLine(),
                peek().getColumn(),
                "Use 'where type' for type constraints"
            );
            return false;
        }

        // Parse first type constraint
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected type identifier in constraint",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid type identifier"
            );
            return false;
        }

        // Allow multiple types in constraints
        while (match(TokenType.DELIM) && previous().getLexeme().equals(",")) {
            if (!match(TokenType.IDENTIFIER)) {
                errorHandler.reportError(
                    "Expected additional type identifier after ','",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide another valid type identifier"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Parses class declarations with inheritance
     * <Class> ::= [<Access_Mod>] [<Non_Access_Mod>] "class" <Identifier>
     *             [<Inherit>] "{" <Class_Body> "}"
     */
    private boolean parseClass() {
        // Parse optional modifiers
        while (peek().getType() == TokenType.RESERVED) {
            String nextWord = peek().getLexeme();
            if (nextWord.equals("class")) break;
            
            advance();
            if (!isValidClassModifier(previous().getLexeme())) {
                errorHandler.reportError(
                    "Invalid class modifier: " + previous().getLexeme(),
                    previous().getLine(),
                    previous().getColumn(),
                    "Use valid access or non-access modifier"
                );
                return false;
            }
        }
    
        // Parse 'class' keyword
        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("class")) {
            return false;
        }
    
        // Parse class name
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected class name",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid class identifier"
            );
            return false;
        }
    
        // Parse optional inheritance
        if (match(TokenType.INHERIT_OP)) {
            String inheritType = previous().getLexeme();
            if (inheritType.equals(":>")) {
                if (!parseClassInheritance()) return false;
            } else if (inheritType.equals(":>>")) {
                if (!parseInterfaceInheritance()) return false;
            }
        }
    
        // Parse class body
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("{")) {
            errorHandler.reportError(
                "Expected '{' after class header",
                peek().getLine(),
                peek().getColumn(),
                "Add opening brace for class body"
            );
            return false;
        }
    
        enterBlock();
        while (!check(TokenType.DELIM) || !peek().getLexeme().equals("}")) {
            if (!parseClassMember()) {
                errorHandler.reportError(
                    "Invalid class member",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide valid field, method, or main method declaration"
                );
                synchronize();
            }
            
            if (atEnd()) {
                errorHandler.reportError(
                    "Unterminated class body",
                    peek().getLine(),
                    peek().getColumn(),
                    "Add closing brace"
                );
                return false;
            }
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("}")) {
            errorHandler.reportError(
                "Expected '}' to close class",
                peek().getLine(),
                peek().getColumn(),
                "Add closing brace for class body"
            );
            return false;
        }
        exitBlock();
    
        return true;
    }

    /**
     * Checks if a modifier is valid for class declarations
     */
    private boolean isValidClassModifier(String modifier) {
        return modifier.equals("public") || modifier.equals("private") || 
                modifier.equals("protected") || modifier.equals("abstract") ||
                modifier.equals("final") || modifier.equals("static");
    }

    /**
     * Parses class inheritance list
     * <Class_Inherit> ::= ":>" <Identifier> ["," <Identifier>]*
     */
    private boolean parseClassInheritance() {
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected superclass name",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid superclass identifier"
            );
            return false;
        }

        while (match(TokenType.DELIM) && previous().getLexeme().equals(",")) {
            if (!match(TokenType.IDENTIFIER)) {
                errorHandler.reportError(
                    "Expected superclass name after ','",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid superclass identifier"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Parses interface inheritance list
     * <Interface_Inherit> ::= ":>>" <Identifier> ["," <Identifier>]*
     */
    private boolean parseInterfaceInheritance() {
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected interface name",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid interface identifier"
            );
            return false;
        }

        while (match(TokenType.DELIM) && previous().getLexeme().equals(",")) {
            if (!match(TokenType.IDENTIFIER)) {
                errorHandler.reportError(
                    "Expected interface name after ','",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid interface identifier"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Parses class members (fields, methods, main method)
     * <Class_Body> ::= <SP_Main> | <SP_Method> | <Field>
     */
    private boolean parseClassMember() {
        // Store current position
        int startPos = current;
    
        // Try simplified main first
        if (peek().getLexeme().equals("main")) {
            if (parseMainMethod()) return true;
            current = startPos;
        }
        
        // Try parsing field
        if (parseField()) return true;
        current = startPos;
        
        // Try parsing full main or regular method
        return parseMethod();
    }

    /**
     * Parses method declarations
     * <SP_Method> ::= [<Access_Mod>] [<Non_Access_Mod>] <Data_Type> 
     *                 <Identifier> "(" [<Parameters>] ")" "{" <Statements> "}"
     */
    private boolean parseMethod() {
        // Parse modifiers
        while (match(TokenType.RESERVED)) {
            String modifier = previous().getLexeme();
            if (!isValidMethodModifier(modifier)) {
                errorHandler.reportError(
                    "Invalid method modifier: " + modifier,
                    previous().getLine(),
                    previous().getColumn(),
                    "Use valid access or non-access modifier"
                );
                return false;
            }
        }
    
        // Parse return type
        if (!parseDataType()) {
            return false;
        }
    
        // Parse method name
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected method name",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid method identifier"
            );
            return false;
        }
    
        // Parse parameters
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after method name",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis for parameters"
            );
            return false;
        }
    
        if (!check(TokenType.DELIM) || !peek().getLexeme().equals(")")) {
            if (!parseParameters()) {
                return false;
            }
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after parameters",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        // Parse type constraints if present
        if (match(TokenType.KEYWORD) && previous().getLexeme().equals("where")) {
            if (!parseTypeConstraint()) {
                errorHandler.reportError(
                    "Invalid type constraint",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide valid type constraints"
                );
                return false;
            }
        }
    
        // Parse method body
        return parseBlock();
    }

    /**
     * Parses the main method
     * <SP_Main> ::= "public" "static" "void" "main" "(" "str" "[" "]" "args" ")" 
     *               "{" <Statements> "}"
     */
    private boolean parseMainMethod() {
        int startPos = current;
        
        // Check for simplified main signature
        if (match(TokenType.KEYWORD) && previous().getLexeme().equals("main")) {
            if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
                current = startPos;
                return false;
            }
            
            if (!match(TokenType.IDENTIFIER) || !previous().getLexeme().equals("args")) {
                current = startPos;
                return false;
            }
            
            if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
                errorHandler.reportError(
                    "Expected ')' after 'args'",
                    peek().getLine(),
                    peek().getColumn(),
                    "Add closing parenthesis"
                );
                return false;
            }
            
            return parseBlock();
        }
        
        // Reset position for full signature check
        current = startPos;
    
        // Check for full main method signature
        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("public")) {
            return false;
        }
    
        // Rest of the full signature check remains the same
        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("static")) {
            errorHandler.reportError(
                "Main method must be static",
                peek().getLine(),
                peek().getColumn(),
                "Add 'static' modifier"
            );
            return false;
        }
    
        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("void")) {
            errorHandler.reportError(
                "Main method must return void",
                peek().getLine(),
                peek().getColumn(),
                "Use 'void' return type"
            );
            return false;
        }
    
        if (!match(TokenType.KEYWORD) || !previous().getLexeme().equals("main")) {
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("(")) {
            errorHandler.reportError(
                "Expected '(' after 'main'",
                peek().getLine(),
                peek().getColumn(),
                "Add opening parenthesis"
            );
            return false;
        }
    
        // Parse str[] args
        if (!match(TokenType.RESERVED) || !previous().getLexeme().equals("str")) {
            errorHandler.reportError(
                "Main method parameter must be of type 'str'",
                peek().getLine(),
                peek().getColumn(),
                "Use 'str' as parameter type"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("[")) {
            errorHandler.reportError(
                "Expected '[' after 'str'",
                peek().getLine(),
                peek().getColumn(), 
                "Add opening bracket"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals("]")) {
            errorHandler.reportError(
                "Expected ']' after '['",
                peek().getLine(),
                peek().getColumn(),
                "Add closing bracket"
            );
            return false;
        }
    
        if (!match(TokenType.IDENTIFIER) || !previous().getLexeme().equals("args")) {
            errorHandler.reportError(
                "Expected 'args' parameter name",
                peek().getLine(),
                peek().getColumn(),
                "Use 'args' as parameter name"
            );
            return false;
        }
    
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(")")) {
            errorHandler.reportError(
                "Expected ')' after parameters",
                peek().getLine(),
                peek().getColumn(),
                "Add closing parenthesis"
            );
            return false;
        }
    
        return parseBlock();
    }

    /**
     * Parses method parameters
     * <Parameters> ::= <Parameter> | <Parameter> "," <Parameters>
     * <Parameter> ::= <Data_Type> <Identifier>
     */
    private boolean parseParameters() {
        do {
            if (!parseDataType()) {
                errorHandler.reportError(
                    "Expected parameter type",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid data type"
                );
                return false;
            }

            if (!match(TokenType.IDENTIFIER)) {
                errorHandler.reportError(
                    "Expected parameter name",
                    peek().getLine(),
                    peek().getColumn(),
                    "Provide a valid parameter identifier"
                );
                return false;
            }

        } while (match(TokenType.DELIM) && previous().getLexeme().equals(","));

        return true;
    }

    /**
     * Parses field declarations
     * <Field> ::= [<Access_Mod>] [<Non_Access_Mod>] <Data_Type> <Identifier> ";"
     */
    private boolean parseField() {
        // Parse modifiers, but check for data type first
        while (peek().getType() == TokenType.RESERVED) {
            String nextWord = peek().getLexeme();
            if (isDataType(nextWord)) break;
            
            advance();
            if (!isValidFieldModifier(previous().getLexeme())) {
                errorHandler.reportError(
                    "Invalid field modifier: " + previous().getLexeme(),
                    previous().getLine(),
                    previous().getColumn(),
                    "Use valid access or non-access modifier"
                );
                return false;
            }
        }
    
        // Parse field type
        if (!parseDataType()) {
            return false;
        }
    
        // Parse field name
        if (!match(TokenType.IDENTIFIER)) {
            errorHandler.reportError(
                "Expected field name",
                peek().getLine(),
                peek().getColumn(),
                "Provide a valid field identifier"
            );
            return false;
        }
    
        // Check for semicolon
        if (!match(TokenType.DELIM) || !previous().getLexeme().equals(";")) {
            errorHandler.reportError(
                "Expected semicolon after field declaration",
                peek().getLine(),
                peek().getColumn(),
                "Add semicolon to end field declaration"
            );
            return false;
        }
    
        return true;
    }

    /**
     * Checks if a modifier is valid for method declarations
     */
    private boolean isValidMethodModifier(String modifier) {
        return modifier.equals("public") || modifier.equals("private") || 
                modifier.equals("protected") || modifier.equals("static") ||
                modifier.equals("final") || modifier.equals("abstract") ||
                modifier.equals("native") || modifier.equals("strictfp");
    }

    /**
     * Checks if a modifier is valid for field declarations
     */
    private boolean isValidFieldModifier(String modifier) {
        return modifier.equals("public") || modifier.equals("private") || 
                modifier.equals("protected") || modifier.equals("static") ||
                modifier.equals("final") || modifier.equals("transient") ||
                modifier.equals("volatile");
    }

    /**
     * Token handling utility methods
     */

    /**
     * Advances to the next token and returns the previous one
     */
    private Token advance() {
        if (!atEnd()) current++;
        return previous();
    }

    /**
     * Returns true if the current token matches the given type
     */
    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Returns true if the current token is of the given type
     */
    private boolean check(TokenType type) {
        return !atEnd() && peek().getType() == type;
    }

    /**
     * Returns true if we've reached the end of input
     */
    private boolean atEnd() {
        return peek().getType() == TokenType.EOF;
    }

    /**
     * Returns the current token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the previous token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Synchronization point handling
     */
    private boolean isStatementStart() {
        if (atEnd()) return false;
        
        TokenType type = peek().getType();
        if (type == TokenType.KEYWORD) {
            String lexeme = peek().getLexeme();
            return lexeme.equals("if") || lexeme.equals("while") ||
                    lexeme.equals("for") || lexeme.equals("do") ||
                    lexeme.equals("switch") || lexeme.equals("return") ||
                    lexeme.equals("break") || lexeme.equals("continue");
        }
        
        return type == TokenType.IDENTIFIER ||
                type == TokenType.RESERVED;
    }

    /**
     * Checks if the current token could start a declaration
     */
    private boolean isDeclarationStart() {
        if (atEnd()) return false;
        
        if (peek().getType() == TokenType.RESERVED) {
            String lexeme = peek().getLexeme();
            return lexeme.equals("public") || lexeme.equals("private") ||
                    lexeme.equals("protected") || lexeme.equals("static") ||
                    lexeme.equals("final") || lexeme.equals("abstract") ||
                    lexeme.equals("class");
        }
        
        return false;
    }

    private boolean isValidDateOp(String op) {
        return op.equals("before") || op.equals("after") ||
                op.equals("year") || op.equals("month") ||
                op.equals("day") || op.equals("today");
    }

    private boolean isDataType(String word) {
        return word.equals("int") || word.equals("char") || word.equals("bool") ||
                word.equals("str") || word.equals("float") || word.equals("double") ||
                word.equals("long") || word.equals("byte") || word.equals("Date") ||
                word.equals("Frac") || word.equals("Complex");
    }
}