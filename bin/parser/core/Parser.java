package parser.core;

import lexer.Token;
import parser.grammar.NonTerminal;
import util.SyntaxErrorHandler;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private final ParserAutomaton automaton;
    private final SyntaxErrorHandler errorHandler;
    private ParseTree parseTree;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.automaton = new ParserAutomaton();
        this.errorHandler = new SyntaxErrorHandler(this);
    }

    /**
     * Parses the given list of tokens into a parse tree.
     *
     * @return The parsed tree structure.
     */
    public ParseTree parse() {
        parseTree = new ParseTree(NonTerminal.SP_PROG);
    
        try {
            // Initialize the automaton with tokens and start symbol
            automaton.setTokens(tokens);
            automaton.reset();
            automaton.pushState(NonTerminal.SP_PROG);
    
            // Start parsing
            boolean parseSuccess = automaton.processTokens();
    
            if (!parseSuccess) {
                // Handle parsing failure
                Token currentToken = peek();
                String expectedTokens = getExpectedTokens();
                
                errorHandler.reportError(
                    SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                    String.format("Unexpected token '%s'. Expected one of: %s", 
                        currentToken != null ? currentToken.getLexeme() : "EOF",
                        expectedTokens),
                    currentToken != null ? currentToken.getLine() : lastToken().getLine(),
                    currentToken != null ? currentToken.getColumn() : lastToken().getColumn(),
                    "Check syntax at this position"
                );
            } else if (!atEnd()) {
                // Extra tokens at the end
                Token extraToken = peek();
                errorHandler.reportError(
                    SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                    "Extra tokens after complete expression",
                    extraToken.getLine(),
                    extraToken.getColumn(),
                    "Remove extra tokens after the complete expression"
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

    /**
     * Gets a string representation of the expected tokens at the current position.
     * This helps provide more meaningful error messages.
     */
    private String getExpectedTokens() {
        NonTerminal currentState = automaton.getCurrentState();
        if (currentState == null) {
            return "end of input";
        }

        StringBuilder expected = new StringBuilder();
        List<List<Object>> productions = parser.grammar.GrammarRule.getProductions(currentState);
        
        for (List<Object> production : productions) {
            if (!production.isEmpty()) {
                Object firstElement = production.get(0);
                if (firstElement instanceof String) {
                    if (expected.length() > 0) {
                        expected.append(", ");
                    }
                    expected.append("'").append(firstElement).append("'");
                } else if (firstElement instanceof NonTerminal) {
                    if (expected.length() > 0) {
                        expected.append(", ");
                    }
                    expected.append(((NonTerminal) firstElement).name());
                }
            }
        }
        
        return expected.length() > 0 ? expected.toString() : "unknown";
    }

    /**
     * Updates the parse tree based on the current state of parsing.
     * This method should be called whenever a production is successfully matched.
     */
    public void updateParseTree(NonTerminal nonTerminal, List<Object> production) {
        ParseTreeNode currentNode = parseTree.getRoot();
        ParseTreeNode newNode = new ParseTreeNode(nonTerminal);
        
        // Add production elements as children
        for (Object element : production) {
            if (element instanceof String) {
                newNode.addChild((String) element);
            } else if (element instanceof NonTerminal) {
                newNode.addChild(new ParseTreeNode((NonTerminal) element));
            }
        }
        
        currentNode.addChild(newNode);
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

    private Token lastToken() {
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    /**
     * Gets the error handler associated with this parser.
     */
    public SyntaxErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Gets the current parse tree.
     */
    public ParseTree getParseTree() {
        return parseTree;
    }

    /**
     * Saves the parse tree visualization to a DOT file.
     */
    public void saveParseTree(String outputPath) {
        if (parseTree != null) {
            parseTree.saveToDotFile(outputPath);
        }
    }
}