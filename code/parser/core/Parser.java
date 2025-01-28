package parser.core;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.core.ParseTree;
import parser.core.ParserAutomaton;
import parser.grammar.NonTerminal;
import parser.grammar.GrammarRule;
import parser.symbol.SymbolTable;
import util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Initial implementation of a recursive descent parser for the S-presso language.
 * Processes tokens from lexer to build an abstract syntax tree following the language grammar.
 */
public class Parser {
    private final Lexer lexer;
    private final ParseTree parseTree;
    private final SymbolTable symbolTable;
    private final ErrorHandler errorHandler;
    private Token currentToken;
    private boolean hasErrors;

    /**
     * Constructs a new Parser with required components.
     * @param lexer The lexical analyzer providing the token stream
     */
    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.parseTree = new ParseTree();
        this.symbolTable = new SymbolTable();
        this.errorHandler = new ErrorHandler();
        this.hasErrors = false;
        advance(); // Get first token
    }

    /**
     * Advances to next token in the stream
     */
    private void advance() {
        try {
            currentToken = lexer.nextToken();
        } catch (Exception e) {
            handleError("Lexical error: " + e.getMessage());
        }
    }

    /**
     * Main parsing method - entry point
     */
    public ParseTree parse() {
        try {
            ParseTreeNode root = parseProgram();
            parseTree.setRoot(root);
            return parseTree;
        } catch (Exception e) {
            handleError("Fatal parsing error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses a complete program according to grammar
     */
    private ParseTreeNode parseProgram() {
        ParseTreeNode programNode = new ParseTreeNode(NonTerminal.PROGRAM);

        while (isStartOfClass()) {
            programNode.addChild(parseClass());
        }

        if (!hasMainMethod(programNode)) {
            handleError("Program must contain a main method");
        }

        return programNode;
    }

    /**
     * Parses a class declaration
     */
    private ParseTreeNode parseClass() {
        ParseTreeNode classNode = new ParseTreeNode(NonTerminal.CLASS);

        // Parse modifiers
        if (isModifier()) {
            classNode.addChild(parseModifiers());
        }

        // Parse class keyword
        if (!match(TokenType.RESERVED, "class")) {
            handleError("Expected 'class' keyword");
            return classNode;
        }

        // Parse class name
        ParseTreeNode identifier = parseIdentifier();
        if (identifier != null) {
            classNode.addChild(identifier);
            symbolTable.enterScope(identifier.getValue());
        }

        // Parse inheritance
        if (currentToken.getType() == TokenType.INHERIT_OP) {
            classNode.addChild(parseInheritance());
        }

        // Parse class body
        if (match(TokenType.DELIM, "{")) {
            classNode.addChild(parseClassBody());
            if (!match(TokenType.DELIM, "}")) {
                handleError("Expected '}' at end of class");
            }
        } else {
            handleError("Expected '{' after class header");
        }

        symbolTable.exitScope();
        return classNode;
    }

    /**
     * Parses class body including methods and fields
     */
    private ParseTreeNode parseClassBody() {
        ParseTreeNode bodyNode = new ParseTreeNode(NonTerminal.CLASS_BODY);

        while (!check(TokenType.DELIM, "}") && !isAtEnd()) {
            if (isMethodStart()) {
                bodyNode.addChild(parseMethod());
            } else if (isFieldStart()) {
                bodyNode.addChild(parseField());
            } else {
                handleError("Invalid class member");
                advance();
            }
        }

        return bodyNode;
    }

    /**
     * Parses method declaration
     */
    private ParseTreeNode parseMethod() {
        ParseTreeNode methodNode = new ParseTreeNode(NonTerminal.METHOD);

        // Parse modifiers
        if (isModifier()) {
            methodNode.addChild(parseModifiers());
        }

        // Parse return type
        methodNode.addChild(parseType());

        // Parse method name
        ParseTreeNode identifier = parseIdentifier();
        if (identifier != null) {
            methodNode.addChild(identifier);
            symbolTable.enterScope(identifier.getValue());
        }

        // Parse parameters
        if (match(TokenType.DELIM, "(")) {
            methodNode.addChild(parseParameters());
            if (!match(TokenType.DELIM, ")")) {
                handleError("Expected ')' after parameters");
            }
        } else {
            handleError("Expected '(' after method name");
        }

        // Parse method body
        if (match(TokenType.DELIM, "{")) {
            methodNode.addChild(parseStatements());
            if (!match(TokenType.DELIM, "}")) {
                handleError("Expected '}' at end of method");
            }
        }

        symbolTable.exitScope();
        return methodNode;
    }

    /**
     * Parses parameters in method declaration
     */
    private ParseTreeNode parseParameters() {
        ParseTreeNode paramsNode = new ParseTreeNode(NonTerminal.PARAMETERS);

        if (!check(TokenType.DELIM, ")")) {
            do {
                paramsNode.addChild(parseParameter());
            } while (match(TokenType.DELIM, ","));
        }

        return paramsNode;
    }

    /**
     * Parses individual parameter
     */
    private ParseTreeNode parseParameter() {
        ParseTreeNode paramNode = new ParseTreeNode(NonTerminal.PARAMETER);
        
        ParseTreeNode type = parseType();
        ParseTreeNode identifier = parseIdentifier();
        
        if (type != null && identifier != null) {
            paramNode.addChild(type);
            paramNode.addChild(identifier);
            symbolTable.addSymbol(identifier.getValue(), type.getValue());
        }

        return paramNode;
    }

    /**
     * Parses statements in method body
     */
    private ParseTreeNode parseStatements() {
        ParseTreeNode stmtsNode = new ParseTreeNode(NonTerminal.STATEMENTS);

        while (!check(TokenType.DELIM, "}") && !isAtEnd()) {
            ParseTreeNode stmt = parseStatement();
            if (stmt != null) {
                stmtsNode.addChild(stmt);
            }
        }

        return stmtsNode;
    }

    /**
     * Parses individual statement
     */
    private ParseTreeNode parseStatement() {
        if (isDeclarationStatement()) {
            return parseDeclaration();
        } else if (isAssignmentStatement()) {
            return parseAssignment();
        } else if (isControlStatement()) {
            return parseControlStatement();
        } else if (isIOStatement()) {
            return parseIOStatement();
        } else {
            handleError("Invalid statement");
            advance();
            return null;
        }
    }

    // Helper methods for checking token types and grammar rules

    private boolean isStartOfClass() {
        return isModifier() || check(TokenType.RESERVED, "class");
    }

    private boolean isMethodStart() {
        return isModifier() || isType();
    }

    private boolean isFieldStart() {
        return isModifier() || isType();
    }

    private boolean isModifier() {
        if (currentToken.getType() != TokenType.RESERVED) return false;
        String lexeme = currentToken.getLexeme();
        return lexeme.equals("public") || lexeme.equals("private") || 
               lexeme.equals("protected") || lexeme.equals("static") || 
               lexeme.equals("final");
    }

    private boolean isType() {
        if (currentToken.getType() != TokenType.RESERVED) return false;
        String lexeme = currentToken.getLexeme();
        return lexeme.equals("int") || lexeme.equals("bool") || 
               lexeme.equals("str") || lexeme.equals("float") || 
               lexeme.equals("char") || lexeme.equals("void");
    }

    private boolean check(TokenType type, String lexeme) {
        return currentToken.getType() == type && 
               currentToken.getLexeme().equals(lexeme);
    }

    private boolean match(TokenType type, String lexeme) {
        if (check(type, lexeme)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean isAtEnd() {
        return currentToken.getType() == TokenType.EOF;
    }

    // Error handling methods

    private void handleError(String message) {
        hasErrors = true;
        errorHandler.reportError(message);
    }

    // Getter methods

    public boolean hasErrors() {
        return hasErrors;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
}