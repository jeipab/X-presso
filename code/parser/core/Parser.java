package parser.core;

import lexer.Token;
import lexer.TokenType;
import parser.grammar.NonTerminal;
import parser.grammar.GrammarRule;
import parser.symbol.SymbolTable;
import util.SyntaxErrorHandler;

import java.util.List;

/**
 * The Parser class implements an LL(1) parser to construct a parse tree.
 * It follows the correct process by interacting with the automaton, symbol table, and parse tree.
 */
public class Parser {
    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final ParserAutomaton automaton;
    private final ParseTree parseTree;
    private final SyntaxErrorHandler errorHandler;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.symbolTable = new SymbolTable();
        this.automaton = new ParserAutomaton();
        this.parseTree = new ParseTree(NonTerminal.SP_PROG);
        this.errorHandler = new SyntaxErrorHandler();
    }

    public ParseTree parse() {
        try {
            parseProgram();
        } catch (Exception e) {
            errorHandler.reportError(
                SyntaxErrorHandler.ErrorType.INVALID_SYNTAX_STRUCTURE,  // Error type
                "Critical Parsing Error: " + e.getMessage(),            // Message
                -1,                                                     // Line (unknown, use -1)
                -1,                                                     // Column (unknown, use -1)
                "Check the source code structure and syntax."           // Suggestion
            );
        }
        return parseTree;
    }

    private void parseProgram() {
        automaton.pushState(NonTerminal.SP_PROG);
        ParseTreeNode classNode = parseTree.addChild(NonTerminal.CLASS); // Fix: Use ParseTreeNode
        parseClass((ParseTree.Node) classNode);
    }

    private void parseClass(ParseTree.Node parent) {
        automaton.transition(peek());
        Token className = expectIdentifier("Expected class name");
        symbolTable.enterScope(className.getLexeme());
        expect("{", "Expected '{' after class declaration");
        ParseTree.Node classBodyNode = parent.addChild(NonTerminal.CLASS_BODY);
        parseClassBody(classBodyNode);
        expect("}", "Expected '}' to close class definition");
    }

    private void parseClassBody(ParseTree.Node parent) {
        ParseTree.Node mainNode = parent.addChild(NonTerminal.SP_MAIN);
        parseMain(mainNode);
    }

    private void parseMain(ParseTree.Node parent) {
        automaton.transition(peek());
        expect("(", "Expected '(' after main");
        expectIdentifier("Expected parameter inside main");
        expect(")", "Expected ')' to close main parameters");
        expect("{", "Expected '{' to start main block");
        ParseTree.Node statementsNode = parent.addChild(NonTerminal.STATEMENTS);
        parseStatements(statementsNode);
        expect("}", "Expected '}' to close main block");
    }

    private void parseStatements(ParseTree.Node parent) {
        while (!check("}")) {
            ParseTree.Node statementNode = parent.addChild(NonTerminal.STATEMENT);
            parseStatement(statementNode);
        }
    }

    private void parseStatement(ParseTree.Node parent) {
    Token currentToken = peek();
    NonTerminal currentState = automaton.getCurrentState();

    if (!automaton.processToken(currentToken)) {
        errorHandler.handleUnexpectedToken(currentToken, null, currentToken.getLine(), currentToken.getColumn());
        return;
    }

    switch (currentState) {
        case DEC_STATE -> parseDeclaration(parent);
        case OUT_STATE -> parseOutput(parent);
        case ASS_STATE -> parseAssignment(parent);
        case CON_STATE -> parseConditional(parent);
        case ITER_STATE -> parseLoop(parent);
        case FUNCTION -> parseFunction(parent);
        case EXPR -> parseExpression(parent);
        case IN_STATE -> parseInput(parent);
        default -> errorHandler.handleInvalidSyntaxStructure("Unknown statement type", currentToken.getLine(), currentToken.getColumn());
    }
}


    private void parseDeclaration(ParseTree.Node parent) {
        automaton.transition(peek());
        Token varType = advance();
        Token varName = expectIdentifier("Expected variable name");
        symbolTable.insert(varName.getLexeme(), varType.getLexeme());
        expect("=", "Expected '=' in declaration");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(exprNode);
        expect(";", "Expected ';' at the end of statement");
    }

    private void parseAssignment(ParseTree.Node parent) {
        automaton.transition(peek());
        Token varName = expectIdentifier("Expected variable name");
        expect("=", "Expected '=' in assignment");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(exprNode);
        expect(";", "Expected ';' at the end of statement");
    }

    private void parseInput(ParseTree.Node parent) {
        automaton.transition(peek());
        expect("STRICT", "Expected 'STRICT' keyword for input");
        expect("Input", "Expected 'Input'");
        expect("::", "Expected '::' after Input");
        expect("get", "Expected 'get' function");
        expect("(", "Expected '(' before input prompt");
        expect("STRING_LITERAL", "Expected string literal as input prompt");
        expect(")", "Expected ')' to close input function");
        expect(";", "Expected ';' after input statement");
    }

    private void parseConditional(ParseTree.Node parent) {
        expect("if", "Expected 'if' keyword");
        expect("(", "Expected '(' before condition");
        ParseTree.Node conditionNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(conditionNode);
        expect(")", "Expected ')' after condition");
        expect("{", "Expected '{' before if block");
        parseStatements(parent.addChild(NonTerminal.STATEMENTS));
        expect("}", "Expected '}' after if block");
    }

    private void parseLoop(ParseTree.Node parent) {
        expect("while", "Expected 'while' keyword");
        expect("(", "Expected '(' before loop condition");
        ParseTree.Node conditionNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(conditionNode);
        expect(")", "Expected ')' after loop condition");
        expect("{", "Expected '{' before loop block");
        parseStatements(parent.addChild(NonTerminal.STATEMENTS));
        expect("}", "Expected '}' after loop block");
    }

    private void parseFunction(ParseTree.Node parent) {
        Token functionName = expectIdentifier("Expected function name");
        expect("(", "Expected '(' before function parameters");
        parseParameters(parent.addChild(NonTerminal.PARAMETERS));
        expect(")", "Expected ')' after function parameters");
        expect("{", "Expected '{' before function body");
        parseStatements(parent.addChild(NonTerminal.STATEMENTS));
        expect("}", "Expected '}' after function body");
    }

    private void parseOutput(ParseTree.Node parent) {
        automaton.transition(peek());
        expect("Output", "Expected 'Output'");
        expect("::", "Expected '::' after Output");
        expect("print", "Expected 'print' function");
        expect("(", "Expected '(' before output content");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(exprNode);
        expect(")", "Expected ')' to close output");
        expect(";", "Expected ';' after output statement");
    }

    private void parseExpression(ParseTree.Node parent) {
        parsePrecedence(parent, 1); // Start with the lowest precedence level
    }

    private void parsePrecedence(ParseTreeNode  parent, int precedence) {
        if (precedence > 17) {
            parsePrimary(parent); // Parse primary expressions at the deepest level
            return;
        }

        ParseTree.Node left = new ParseTree.Node(NonTerminal.EXPR);
        parsePrecedence(left, precedence + 1); // Parse higher precedence expressions first

        while (true) {
            Token currentToken = peek();

            // Check if the current token matches an operator for this precedence level
            if (!GrammarRule.isOperatorAtPrecedence(currentToken, precedence)) {
                break; // Stop parsing if the operator does not match this precedence level
            }

            Token operator = advance(); // Consume the operator
            ParseTreeNode  operatorNode = parent.addChild(operator.getLexeme());

            if (isRightAssociative(precedence)) {
                parsePrecedence(operatorNode, precedence); // Right-to-left associativity
            } else {
                parsePrecedence(operatorNode, precedence + 1); // Left-to-right associativity
            }

            parent.addChild(left); // Add the parsed left-hand expression
        }
    }

    private void parsePrimary(ParseTreeNode parent) {
        Token currentToken = peek();

        if (currentToken.getLexeme().equals("(")) {
            advance(); // Consume '('
            parsePrecedence(parent, 1); // Parse inner expression
            expect(")", "Expected ')' to close grouping");
        } else if (GrammarRule.isLiteral(currentToken)) {
            parent.addChild(advance().getLexeme()); // Add literal
        } else if (GrammarRule.isIdentifier(currentToken)) {
            parent.addChild(advance().getLexeme()); // Add identifier
        } else {
            throw new SyntaxErrorHandler.SyntaxError(
                    SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                    "Unexpected token in expression",
                    currentToken.getLine(),
                    currentToken.getColumn(),
                    "Check the syntax of the expression.",
                    "Parsing expression"
                );
        }
    }

    private void parseParameters(ParseTree.Node parent) {
        if (check(")")) {
            return; // No parameters (empty parentheses)
        }
    
        do {
            ParseTree.Node parameterNode = parent.addChild(NonTerminal.PARAMETER);
            parseParameter(parameterNode);
        } while (match(",")); // Continue parsing if there's a comma
    
        expect(")", "Expected ')' after parameters");
    }
    
    /**
     * Parses a single parameter in a function declaration.
     */
    private void parseParameter(ParseTree.Node parent) {
        Token type = expectIdentifier("Expected parameter type");
        Token name = expectIdentifier("Expected parameter name");
    
        parent.addChild(type.getLexeme());
        parent.addChild(name.getLexeme());
    }
    

    private boolean isRightAssociative(int precedence) {
        return precedence == 4 || precedence == 5 || precedence == 16 || precedence == 17; // Based on provided table
    }

    public Token advance() {
        return tokens.get(current++);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean check(String expected) {
        return current < tokens.size() && tokens.get(current).getLexeme().equals(expected);
    }

    private Token expect(String expected, String errorMessage) {
        if (!check(expected)) {
            Token errorToken = new Token(TokenType.UNKNOWN, expected, peek().getLine(), peek().getColumn());
            errorHandler.handleMissingToken(expected, errorToken.getLine(), errorToken.getColumn());
            return errorToken; // Returning a placeholder token to prevent crash
        }
        return advance();
    }

    private Token expectIdentifier(String errorMessage) {
        Token token = advance();
        if (!GrammarRule.isIdentifier(token)) {
            throw new SyntaxErrorHandler.SyntaxError(
                SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,  // Error Type
                errorMessage,                                   // Error Message
                token.getLine(),                                // Line Number
                token.getColumn(),                              // Column Number
                "Expected an identifier, but found: " + token.getLexeme(), // Suggestion
                "Parsing identifier"                           // Context
            );
        }
        return token;
    }

    private boolean match(String expected) {
        if (check(expected)) {
            advance();
            return true;
        }
        return false;
    }

    public boolean atEnd() {
        return current >= tokens.size();
    }
}
