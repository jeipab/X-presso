package parser.core;

import lexer.Token;
import parser.grammar.NonTerminal;
import parser.symbol.SymbolTable;
import util.SyntaxErrorHandler;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final ParserAutomaton automaton;
    private final SyntaxErrorHandler errorHandler;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.symbolTable = new SymbolTable();
        this.automaton = new ParserAutomaton();
        this.errorHandler = new SyntaxErrorHandler(this);
    }

    /**
     * Parses the given list of tokens into a parse tree.
     *
     * @return The parsed tree structure.
     */
    public ParseTree parse() {
        ParseTree parseTree = new ParseTree(NonTerminal.SP_PROG);
        ParseTreeNode rootNode = parseTree.getRoot();
    
        try {
            // Initialize parsing by pushing the start symbol
            automaton.pushState(NonTerminal.SP_PROG);
    
            while (!automaton.isStackEmpty() && !atEnd()) {
                Token currentToken = peek();
    
                // Process the current token through the automaton
                if (automaton.processToken(currentToken)) {
                    // If token is valid for current state, perform transition
                    if (automaton.transition(currentToken)) {
                        advance(); // Only advance if a terminal was consumed
                        if (atEnd()) break;
                    }
                } else {
                    // Handle syntax error
                    errorHandler.reportError(
                        SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                        "Unexpected token '" + currentToken.getLexeme() + "'",
                        currentToken.getLine(),
                        currentToken.getColumn(),
                        "Check syntax at this position"
                    );
    
                    automaton.popState();
                }
            }
    
            // Check if we've reached the end of input appropriately
            if (!automaton.isStackEmpty()) {
                Token lastToken = tokens.get(tokens.size() - 1);
                errorHandler.reportError(
                    SyntaxErrorHandler.ErrorType.UNEXPECTED_EOF,
                    "Unexpected end of input - incomplete syntax",
                    lastToken.getLine(),
                    lastToken.getColumn(),
                    "Check for missing closing tokens or statements"
                );
            }
    
        } catch (Exception e) {
            Token current = peek();
            errorHandler.reportError(
                SyntaxErrorHandler.ErrorType.INVALID_SYNTAX_STRUCTURE,
                "Critical parsing error: " + e.getMessage(),
                current != null ? current.getLine() : 0,
                current != null ? current.getColumn() : 0,
                "Check the overall syntax structure"
            );
        }
    
        return parseTree;
    }

    private Token peek() {
        return current < tokens.size() ? tokens.get(current) : null;
    }

    public Token advance() {
        return current < tokens.size() ? tokens.get(current++) : null;
    }

    public boolean atEnd() {
        return current >= tokens.size();
    }
}