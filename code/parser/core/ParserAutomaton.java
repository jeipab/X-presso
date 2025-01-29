package parser.core;

import lexer.Token;
import parser.grammar.NonTerminal;
import parser.grammar.GrammarRule;
import util.SyntaxErrorHandler;
import java.util.Stack;
import java.util.HashSet;
import java.util.List;

public class ParserAutomaton {
    private final Stack<Object> stateStack;
    private final SyntaxErrorHandler syntaxErrorHandler;
    private NonTerminal currentState;

    public ParserAutomaton() {
        this.stateStack = new Stack<>();
        this.syntaxErrorHandler = new SyntaxErrorHandler();
        this.currentState = NonTerminal.SP_PROG;
    }

    public boolean processToken(Token token) {
        return isValidTokenForState(currentState, token, new HashSet<>());
    }

    /**
     * Recursively checks if a token is valid for a given non-terminal state
     * by exploring all possible productions.
     */
    private boolean isValidTokenForState(NonTerminal state, Token token, HashSet<NonTerminal> visited) {
        // Prevent infinite recursion
        if (visited.contains(state)) {
            return false;
        }
        visited.add(state);

        List<List<Object>> productions = GrammarRule.getProductions(state);
        if (productions == null) return false;

        // Check each production for the current state
        for (List<Object> production : productions) {
            if (production.isEmpty()) continue;

            Object first = production.get(0);
            if (first instanceof String) {
                // Direct terminal match
                if (first.equals(token.getLexeme())) {
                    return true;
                }
            } else if (first instanceof NonTerminal) {
                // For non-terminals, recursively check their productions
                NonTerminal nonTerm = (NonTerminal) first;
                if (isValidTokenForState(nonTerm, token, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void transition(Token token) {
        if (stateStack.isEmpty()) {
            syntaxErrorHandler.reportError(
                SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                "Unexpected token '" + token.getLexeme() + "' at end of input.",
                token.getLine(),
                token.getColumn(),
                "Check for missing tokens or incorrect syntax."
            );
            return;
        }

        Object top = stateStack.peek();

        if (top instanceof String) {
            // Handle terminal tokens
            if (top.equals(token.getLexeme())) {
                stateStack.pop(); // Consume terminal
            } else {
                syntaxErrorHandler.reportError(
                    SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                    "Expected '" + top + "', found '" + token.getLexeme() + "'.",
                    token.getLine(),
                    token.getColumn(),
                    "Check for missing or incorrect syntax structure."
                );
            }
            return;
        }

        if (top instanceof NonTerminal) {
            NonTerminal nonTerminal = (NonTerminal) top;
            if (handleNonTerminalTransition(nonTerminal, token)) {
                stateStack.pop(); // Remove current non-terminal after successful transition
            } else {
                syntaxErrorHandler.reportError(
                    SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                    "Unexpected token '" + token.getLexeme() + "' in state " + nonTerminal,
                    token.getLine(),
                    token.getColumn(),
                    "Check syntax structure and valid tokens for this context."
                );
            }
        }
    }

    /**
     * Handles transitions for non-terminal states by recursively checking productions
     * and pushing appropriate production rules onto the stack.
     */
    private boolean handleNonTerminalTransition(NonTerminal nonTerminal, Token token) {
        List<List<Object>> productions = GrammarRule.getProductions(nonTerminal);
        if (productions == null) return false;

        // Try each production
        for (List<Object> production : productions) {
            if (production.isEmpty()) {
                // Handle epsilon productions
                continue;
            }

            if (isValidProduction(production, token, new HashSet<>())) {
                pushProduction(production);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a production is valid for the given token by recursively
     * examining its components.
     */
    private boolean isValidProduction(List<Object> production, Token token, HashSet<NonTerminal> visited) {
        if (production.isEmpty()) return false;

        Object first = production.get(0);
        if (first instanceof String) {
            return first.equals(token.getLexeme());
        } else if (first instanceof NonTerminal) {
            NonTerminal nonTerm = (NonTerminal) first;
            return isValidTokenForState(nonTerm, token, visited);
        }

        return false;
    }

    private void pushProduction(List<Object> production) {
        // Push in reverse order to maintain correct parsing order
        for (int i = production.size() - 1; i >= 0; i--) {
            stateStack.push(production.get(i));
        }
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

    public NonTerminal getCurrentState() {
        return currentState;
    }
}