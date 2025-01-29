package parser.core;

import lexer.Token;
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
            errorHandler.reportError("Critical Parsing Error: " + e.getMessage());
        }
        return parseTree;
    }

    private void parseProgram() {
        automaton.pushState(NonTerminal.SP_PROG);
        ParseTree.Node classNode = parseTree.addChild(NonTerminal.CLASS);
        parseClass(classNode);
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

        if (!automaton.processToken(currentToken)) {
            errorHandler.reportError("Unexpected token: " + currentToken.getLexeme());
            return;
        }

        switch (automaton.getCurrentState()) {
            case "DECLARATION":
                parseDeclaration(parent);
                break;
            case "OUTPUT":
                parseOutput(parent);
                break;
            case "ASSIGNMENT":
                parseAssignment(parent);
                break;
            case "CONDITIONAL":
                parseConditional(parent);
                break;
            case "LOOP":
                parseLoop(parent);
                break;
            case "FUNCTION":
                parseFunction(parent);
                break;
            case "EXPRESSION":
                parseExpression(parent);
                break;
            case "INPUT":
                parseInput(parent);
                break;
            default:
                errorHandler.reportError("Unknown statement type.");
        }
    }

    private void parseDeclaration(ParseTree.Node parent) {
        automaton.transition(peek());
        Token varType = advance();
        Token varName = expectIdentifier("Expected variable name");
        symbolTable.insert(varName.getLexeme(), varType.getLexeme());
        expect("=", "Expected '=' in declaration");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPRESSION);
        parseExpression(exprNode);
        expect(";", "Expected ';' at the end of statement");
    }

    private void parseAssignment(ParseTree.Node parent) {
        automaton.transition(peek());
        Token varName = expectIdentifier("Expected variable name");
        expect("=", "Expected '=' in assignment");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPRESSION);
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

    private void parseOutput(ParseTree.Node parent) {
        automaton.transition(peek());
        expect("Output", "Expected 'Output'");
        expect("::", "Expected '::' after Output");
        expect("print", "Expected 'print' function");
        expect("(", "Expected '(' before output content");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPRESSION);
        parseExpression(exprNode);
        expect(")", "Expected ')' to close output");
        expect(";", "Expected ';' after output statement");
    }

    private void parseExpression(ParseTree.Node parent) {
        parsePrecedence(parent, 1); // Start with the lowest precedence level
    }

    private void parsePrecedence(ParseTree.Node parent, int precedence) {
        if (precedence > 17) {
            parsePrimary(parent); // Parse primary expressions at the deepest level
            return;
        }

        ParseTree.Node left = new ParseTree.Node(NonTerminal.EXPRESSION);
        parsePrecedence(left, precedence + 1); // Parse higher precedence expressions first

        while (true) {
            Token currentToken = peek();

            // Check if the current token matches an operator for this precedence level
            if (!GrammarRule.isOperatorAtPrecedence(currentToken, precedence)) {
                break; // Stop parsing if the operator does not match this precedence level
            }

            Token operator = advance(); // Consume the operator
            ParseTree.Node operatorNode = parent.addChild(operator.getLexeme());

            if (isRightAssociative(precedence)) {
                parsePrecedence(operatorNode, precedence); // Right-to-left associativity
            } else {
                parsePrecedence(operatorNode, precedence + 1); // Left-to-right associativity
            }

            parent.addChild(left); // Add the parsed left-hand expression
        }
    }

    private void parsePrimary(ParseTree.Node parent) {
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
            throw new SyntaxError(currentToken.getLine(), "Unexpected token in expression");
        }
    }

    private boolean isRightAssociative(int precedence) {
        return precedence == 4 || precedence == 5 || precedence == 16 || precedence == 17; // Based on provided table
    }

    private Token advance() {
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
            throw new SyntaxError(peek().getLine(), errorMessage);
        }
        return advance();
    }

    private Token expectIdentifier(String errorMessage) {
        Token token = advance();
        if (!GrammarRule.isIdentifier(token)) {
            throw new SyntaxError(token.getLine(), errorMessage);
        }
        return token;
    }
}
