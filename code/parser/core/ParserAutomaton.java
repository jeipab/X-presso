package parser.core;

import lexer.Token;
import parser.grammar.NonTerminal;
import parser.grammar.GrammarRule;
import util.SyntaxErrorHandler;
import java.util.Stack;
import java.util.List;

public class ParserAutomaton {
    private final Stack<Object> stateStack;
    private final SyntaxErrorHandler syntaxErrorHandler;
    private NonTerminal currentState;

    public ParserAutomaton() {
        this.stateStack = new Stack<>();
        this.syntaxErrorHandler = new SyntaxErrorHandler();
        this.currentState = NonTerminal.SP_PROG; // Start at SP_PROG
    }

    public void pushState(NonTerminal nonTerminal) {
        stateStack.push(nonTerminal);
        currentState = nonTerminal;
    }

    public void popState() {
        if (!stateStack.isEmpty()) {
            stateStack.pop();
            currentState = stateStack.isEmpty() ? NonTerminal.SP_PROG : (NonTerminal) stateStack.peek();
        }
    }

    // This function processes the current token against the top of the stack
    public boolean processToken(Token token) {
        // Check if the token can start the production for the current state
        if (GrammarRule.isValidStart(currentState, token.getLexeme())) {
            return true; // Valid token for the current state
        }
        return false; // Invalid token for the current state
    }

    // This function handles the actual state transition based on the token
    public void transition(Token token) {
        if (stateStack.isEmpty()) {
            syntaxErrorHandler.reportError("Unexpected token '" + token.getLexeme() + "' at end of input.");
            return;
        }

        Object top = stateStack.peek();

        if (top instanceof String) {
            // Handle terminal tokens
            if (top.equals(token.getLexeme())) {
                stateStack.pop(); // Consume terminal
            } else {
                syntaxErrorHandler.reportError("Syntax error: Expected '" + top + "', found '" + token.getLexeme() + "'.");
            }
            return;
        }

        if (top instanceof NonTerminal) {
            NonTerminal nonTerminal = (NonTerminal) top;
            List<List<Object>> productions = GrammarRule.getProductions(nonTerminal);

            for (List<Object> production : productions) {
                // Check if the production can be applied based on the token
                if (!production.isEmpty() && production.get(0).equals(token.getLexeme())) {
                    stateStack.pop(); // Pop the current non-terminal from the stack
                    pushProduction(production); // Push the production onto the stack
                    return;
                }
            }

            syntaxErrorHandler.reportError(
                "Unexpected token '" + token.getLexeme() + "' in state " + nonTerminal +
                ". Expected one of: " + getExpectedTokens(productions)
            );
        }
    }

    // Push the production's elements onto the stack in reverse order
    private void pushProduction(List<Object> production) {
        for (int i = production.size() - 1; i >= 0; i--) {
            stateStack.push(production.get(i));
        }
    }

    private String getExpectedTokens(List<List<Object>> productions) {
        StringBuilder expected = new StringBuilder();
        for (List<Object> production : productions) {
            if (!production.isEmpty() && production.get(0) instanceof String) {
                expected.append("'").append(production.get(0)).append("', ");
            }
        }
        return expected.length() > 0 ? expected.substring(0, expected.length() - 2) : "unknown tokens";
    }

    public NonTerminal getCurrentState() {
        return currentState;
    }
}
