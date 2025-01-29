package parser.core;

import lexer.Token;
import parser.tree.ParseTree;
import parser.tree.NonTerminal;
import parser.grammar.GrammarRule;
import parser.symbol.SymbolTable;
import parser.automaton.ParserAutomaton;
import parser.error.SyntaxError;
import java.util.List;

/**
 * The Parser class serves as the central component of the parsing process.
 * It receives a stream of tokens from the lexer, validates them against grammar rules,
 * builds an abstract syntax tree (AST), and manages symbol tables and parsing states.
 */
public class Parser {
    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final ParserAutomaton automaton;
    private final ParseTree parseTree;
    private int current = 0;

    /**
     * Constructs a Parser instance with a list of tokens.
     * Initializes supporting components such as the SymbolTable and ParserAutomaton.
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.symbolTable = new SymbolTable();
        this.automaton = new ParserAutomaton();
        this.parseTree = new ParseTree(NonTerminal.SP_PROG);
        
        if (!tokens.isEmpty()) {
            System.out.println("\nParser Status:");
            System.out.println("==============");
            System.out.println("Number of tokens received: " + tokens.size());
            System.out.println("Symbol table initialized");
            printSymbolTable();
        }
    }

    /**
     * Prints the tokens received from the lexical analyzer for debugging purposes.
     */
    private void printSymbolTable() {
        System.out.println("\nReceived from Lexical Analyzer:");
        System.out.println("==============================");
    
        System.out.println("Tokens received: " + tokens.size());
        for (Token token : tokens) {
            String lexeme = token.getLexeme()
                                .replace("\n", "\\n")
                                .replace("\t", "\\t")
                                .replace("\r", "\\r");
    
            System.out.printf("%-20s %-15s Line: %-3d Column: %-3d%n",
                token.getType(), lexeme, token.getLine(), token.getColumn());
        }
    
        System.out.println("==============================\n");
    }

    /**
     * Initiates the parsing process and returns the constructed parse tree.
     */
    public ParseTree parse() {
        try {
            parseProgram();
        } catch (SyntaxError e) {
            System.err.println("Syntax Error: " + e.getMessage());
        }
        return parseTree;
    }

    /**
     * Parses the top-level program structure and initializes the parse tree.
     */
    private void parseProgram() {
        ParseTree.Node classNode = parseTree.addChild(NonTerminal.CLASS);
        parseClass(classNode);
    }

    /**
     * Parses a class definition.
     */
    private void parseClass(ParseTree.Node parent) {
        if (match("public")) {
            advance();
        }
        expect("class", "Expected 'class' keyword");
        Token className = expectIdentifier("Expected class name");
        symbolTable.enterScope(className.getLexeme());
        expect("{", "Expected '{' after class declaration");
        ParseTree.Node classBodyNode = parent.addChild(NonTerminal.CLASS_BODY);
        parseClassBody(classBodyNode);
        expect("}", "Expected '}' to close class definition");
    }

    /**
     * Parses the body of a class, including the main method.
     */
    private void parseClassBody(ParseTree.Node parent) {
        ParseTree.Node mainNode = parent.addChild(NonTerminal.SP_MAIN);
        parseMain(mainNode);
    }

    /**
     * Parses the main method.
     */
    private void parseMain(ParseTree.Node parent) {
        expect("main", "Expected 'main' method");
        expect("(", "Expected '(' after main");
        expectIdentifier("Expected parameter inside main");
        expect(")", "Expected ')' to close main parameters");
        expect("{", "Expected '{' to start main block");
        ParseTree.Node statementsNode = parent.addChild(NonTerminal.STATEMENTS);
        parseStatements(statementsNode);
        expect("}", "Expected '}' to close main block");
    }

    /**
     * Parses a sequence of statements inside a block.
     */
    private void parseStatements(ParseTree.Node parent) {
        while (!check("}")) {
            ParseTree.Node statementNode = parent.addChild(NonTerminal.STATEMENT);
            parseStatement(statementNode);
        }
    }

    /**
     * Parses a single statement.
     */
    private void parseStatement(ParseTree.Node parent) {
        Token currentToken = peek();
        if (GrammarRule.isDeclaration(currentToken)) {
            parseDeclaration(parent);
        } else if (GrammarRule.isOutput(currentToken)) {
            parseOutput(parent);
        } else {
            throw new SyntaxError(currentToken.getLine(), "Unexpected statement");
        }
    }

    /**
     * Parses a variable declaration.
     */
    private void parseDeclaration(ParseTree.Node parent) {
        Token varType = advance();
        Token varName = expectIdentifier("Expected variable name");
        symbolTable.insert(varName.getLexeme(), varType.getLexeme());
        expect("=", "Expected '=' in declaration");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPRESSION);
        parseExpression(exprNode);
        expect(";", "Expected ';' at the end of statement");
    }

    /**
     * Parses an output statement.
     */
    private void parseOutput(ParseTree.Node parent) {
        expect("Output", "Expected 'Output'");
        expect("::", "Expected '::' after Output");
        expect("print", "Expected 'print' function");
        expect("(", "Expected '(' before output content");
        ParseTree.Node exprNode = parent.addChild(NonTerminal.EXPRESSION);
        parseExpression(exprNode);
        expect(")", "Expected ')' to close output");
        expect(";", "Expected ';' after output statement");
    }

    /**
     * Parses an expression.
     */
    private void parseExpression(ParseTree.Node parent) {
        advance();
    }
}
