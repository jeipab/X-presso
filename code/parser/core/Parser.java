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
        this.errorHandler = new SyntaxErrorHandler(this);
    }

    public ParseTree parse() {
        try {
            parseProgram();
        } catch (Exception e) {
            Token current = peek(); // Get the current token
            int line = current != null ? current.getLine() : -1;
            int column = current != null ? current.getColumn() : -1;

            errorHandler.reportError(
                SyntaxErrorHandler.ErrorType.INVALID_SYNTAX_STRUCTURE,
                "Critical Parsing Error: " + e.getMessage(),
                line, // Use actual line
                column, // Use actual column
                "Check the source code structure and syntax."
            );
        }
        return parseTree;
    }

    private void parseProgram() {
        automaton.pushState(NonTerminal.SP_PROG);
        ParseTreeNode classNode = parseTree.addChild(NonTerminal.CLASS); 
        parseClass(classNode);
    }

    private void parseClass(ParseTreeNode parent) {
        // Parse modifiers if they exist using the grammar rules
        if (isPartOf(NonTerminal.CLASS_MODS, peek())) {
            parseClassModifiers();
        }
    
        // Parse the rest of the class declaration
        automaton.transition(peek()); // Sync automaton state
        parseClassDefinition(parent);
    }

    /**
     * Parses class modifiers according to the grammar rules
     */
    private void parseClassModifiers() {
        while (peek() != null && isPartOf(NonTerminal.CLASS_MODS, peek())) {
            Token modifier = peek();
            
            if (isPartOf(NonTerminal.ACCESS_MOD, modifier)) {
                automaton.transition(modifier);
                advance();
            } else if (isPartOf(NonTerminal.NON_ACCESS_MOD, modifier)) {
                automaton.transition(modifier);
                advance();
            } else {
                break;
            }
        }
    }

    /**
     * Parses the core class definition after modifiers
     */
    private void parseClassDefinition(ParseTreeNode parent) {
        // Expect class keyword
        expect("class", "Expected 'class' keyword");

        // Parse class name
        Token className = expectIdentifier("Expected class name");
        symbolTable.enterScope(className.getLexeme());

        // Parse inheritance if present
        if (isPartOf(NonTerminal.CLASS_INHERIT, peek())) {
            parseInheritance();
        }

        // Parse class body
        expect("{", "Expected '{' after class declaration");
        ParseTreeNode classBodyNode = parent.addChild(NonTerminal.CLASS_BODY);
        parseClassBody(classBodyNode);
        expect("}", "Expected '}' to close class definition");

        symbolTable.exitScope();
    }

    /**
     * Parses class inheritance declarations
     */
    private void parseInheritance() {
        while (peek() != null && peek().getLexeme().equals(":>")) {
            advance(); // consume :>
            Token parentClass = expectIdentifier("Expected parent class name");
            // Could add symbol table validation here for the parent class
        }
    }

    private void parseClassBody(ParseTreeNode parent) {
        ParseTreeNode mainNode = parent.addChild(NonTerminal.SP_MAIN);
        parseMain(mainNode);
    }

    private void parseMain(ParseTreeNode parent) {
        automaton.transition(peek());
        expect("(", "Expected '(' after main");
        expectIdentifier("Expected parameter inside main");
        expect(")", "Expected ')' to close main parameters");
        expect("{", "Expected '{' to start main block");
        ParseTreeNode statementsNode = parent.addChild(NonTerminal.STATEMENTS);
        parseStatements(statementsNode);
        expect("}", "Expected '}' to close main block");
    }

    private void parseStatements(ParseTreeNode parent) {
        while (!check("}")) {
            ParseTreeNode statementNode = parent.addChild(NonTerminal.STATEMENT);
            parseStatement(statementNode);
        }
    }

    private void parseStatement(ParseTreeNode parent) {
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


    private void parseDeclaration(ParseTreeNode parent) {
        automaton.transition(peek());
        Token varType = advance();
        Token varName = expectIdentifier("Expected variable name");
        symbolTable.insert(varName.getLexeme(), varType.getLexeme());
        expect("=", "Expected '=' in declaration");
        ParseTreeNode exprNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(exprNode);
        expect(";", "Expected ';' at the end of statement");
    }

    private void parseAssignment(ParseTreeNode parent) {
        automaton.transition(peek());
        Token varName = expectIdentifier("Expected variable name");
        expect("=", "Expected '=' in assignment");
        ParseTreeNode exprNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(exprNode);
        expect(";", "Expected ';' at the end of statement");
    }

    private void parseInput(ParseTreeNode parent) {
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

    private void parseConditional(ParseTreeNode parent) {
        expect("if", "Expected 'if' keyword");
        expect("(", "Expected '(' before condition");
        ParseTreeNode conditionNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(conditionNode);
        expect(")", "Expected ')' after condition");
        expect("{", "Expected '{' before if block");
        parseStatements(parent.addChild(NonTerminal.STATEMENTS));
        expect("}", "Expected '}' after if block");
    }

    private void parseLoop(ParseTreeNode parent) {
        expect("while", "Expected 'while' keyword");
        expect("(", "Expected '(' before loop condition");
        ParseTreeNode conditionNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(conditionNode);
        expect(")", "Expected ')' after loop condition");
        expect("{", "Expected '{' before loop block");
        parseStatements(parent.addChild(NonTerminal.STATEMENTS));
        expect("}", "Expected '}' after loop block");
    }

    private void parseFunction(ParseTreeNode parent) {
        Token functionName = expectIdentifier("Expected function name");
        expect("(", "Expected '(' before function parameters");
        parseParameters(parent.addChild(NonTerminal.PARAMETERS));
        expect(")", "Expected ')' after function parameters");
        expect("{", "Expected '{' before function body");
        parseStatements(parent.addChild(NonTerminal.STATEMENTS));
        expect("}", "Expected '}' after function body");
    }

    private void parseOutput(ParseTreeNode parent) {
        automaton.transition(peek());
        expect("Output", "Expected 'Output'");
        expect("::", "Expected '::' after Output");
        expect("print", "Expected 'print' function");
        expect("(", "Expected '(' before output content");
        ParseTreeNode exprNode = parent.addChild(NonTerminal.EXPR);
        parseExpression(exprNode);
        expect(")", "Expected ')' to close output");
        expect(";", "Expected ';' after output statement");
    }

    private void parseExpression(ParseTreeNode parent) {
        parsePrecedence(parent, 1); // Start with the lowest precedence level
    }

    private void parsePrecedence(ParseTreeNode  parent, int precedence) {
        if (precedence > 17) {
            parsePrimary(parent); // Parse primary expressions at the deepest level
            return;
        }

        ParseTreeNode left = new ParseTreeNode(NonTerminal.EXPR);
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

    private void parseParameters(ParseTreeNode parent) {
        if (check(")")) {
            return; // No parameters (empty parentheses)
        }
    
        do {
            ParseTreeNode parameterNode = parent.addChild(NonTerminal.PARAMETER);
            parseParameter(parameterNode);
        } while (match(",")); // Continue parsing if there's a comma
    
        expect(")", "Expected ')' after parameters");
    }
    
    /**
     * Parses a single parameter in a function declaration.
     */
    private void parseParameter(ParseTreeNode parent) {
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
        Token current = peek();
        if (!current.getLexeme().equals(expected)) {
            NonTerminal context = automaton.getCurrentState();
            String contextMsg = String.format("While parsing %s", context);
            
            errorHandler.reportError(
                SyntaxErrorHandler.ErrorType.MISSING_TOKEN,
                errorMessage,
                current.getLine(),
                current.getColumn(),
                String.format("Expected '%s' but found '%s'. %s", 
                    expected, current.getLexeme(), contextMsg)
            );
            return current;
        }
        return advance();
    }

    private Token expectIdentifier(String errorMessage) {
        Token token = advance();
        if (!GrammarRule.isIdentifier(token)) {
            NonTerminal context = automaton.getCurrentState();
            errorHandler.reportError(
                SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                errorMessage,
                token.getLine(),
                token.getColumn(),
                String.format("Found '%s' while parsing %s", 
                    token.getLexeme(), context)
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

    /**
 * Checks if a token can be derived from a given non-terminal using the grammar rules
 */
    private boolean isPartOf(NonTerminal nonTerminal, Token token) {
        List<List<Object>> productions = GrammarRule.getProductions(nonTerminal);
        if (productions == null) return false;

        for (List<Object> production : productions) {
            if (production.isEmpty()) continue;
            Object first = production.get(0);
            
            if (first instanceof String) {
                if (first.equals(token.getLexeme())) return true;
            } else if (first instanceof NonTerminal) {
                if (isPartOf((NonTerminal)first, token)) return true;
            }
        }
        return false;
    }
}
