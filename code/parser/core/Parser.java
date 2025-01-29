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
        automaton.transition(peek());
        advance(); // Placeholder for operator precedence parsing
    }
}
