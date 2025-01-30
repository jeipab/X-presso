package parser.core;

import lexer.Token;
import parser.grammar.GrammarRule;
import parser.grammar.NonTerminal;
import java.util.List;
import java.util.Stack;

public class ParserAutomaton {
    private final Stack<NonTerminal> stateStack;

    public ParserAutomaton() {
        this.stateStack = new Stack<>();
    }

    public void reset() {
        stateStack.clear();
    }

    public void pushState(NonTerminal state) {
        stateStack.push(state);
        System.out.println("Pushed state: " + state);
    }

    public void popState() {
        if (!stateStack.isEmpty()) {
            NonTerminal top = stateStack.pop();
            System.out.println("Popped state: " + top);
        }
    }

    public boolean processToken(Token token) {
        if (isStackEmpty()) {
            return false;
        }

        NonTerminal currentState = getCurrentState();
        System.out.println("Processing token: " + token.getLexeme() + " in state: " + currentState);

        if (currentState == null) {
            return false;
        }

        List<List<Object>> productions = GrammarRule.getProductions(currentState);

        for (List<Object> production : productions) {
            if (!production.isEmpty()) {
                Object firstElement = production.get(0);

                if (firstElement instanceof NonTerminal) {
                    if (!stateStack.contains(firstElement)) { // Prevent redundant expansion
                        expandNonTerminal(currentState, production);
                        return true;
                    }
                } else if (firstElement instanceof String) {
                    if (((String) firstElement).equals(token.getLexeme())) {
                        return transition(token);
                    }
                }
            } else {
                System.out.println("Using empty production for state: " + currentState);
                popState();
                return true;
            }
        }

        // If no valid transition is found, perform error recovery
        handleError(token);
        return false;
    }

    public boolean transition(Token token) {
        if (isStackEmpty()) return false;

        NonTerminal currentState = getCurrentState();
        List<List<Object>> productions = GrammarRule.getProductions(currentState);

        for (List<Object> production : productions) {
            if (!production.isEmpty()) {
                Object firstElement = production.get(0);

                if (firstElement instanceof String) {
                    String terminal = (String) firstElement;
                    if (terminal.equals(token.getLexeme())) {
                        System.out.println("Matched terminal: " + terminal + " with token: " + token.getLexeme());

                        System.out.println("Popping state: " + currentState);
                        popState();

                        if (isStackEmpty()) {
                            pushState(NonTerminal.SP_PROG);
                        }

                        return true;
                    }
                } else if (firstElement instanceof NonTerminal) {
                    if (!stateStack.contains(firstElement)) {
                        expandNonTerminal(currentState, production);
                        return transition(token);
                    }
                }
            }
        }

        return false;
    }

    private void expandNonTerminal(NonTerminal nonTerminal, List<Object> production) {
        System.out.println("\nExpanding " + nonTerminal + " with production: " + production);

        for (int i = production.size() - 1; i >= 0; i--) {
            Object element = production.get(i);

            if (element instanceof NonTerminal) {
                if (!stateStack.contains(element)) { // Prevent redundant expansion
                    pushState((NonTerminal) element);
                }
            } else if (element instanceof String) {
                System.out.println("Encountered terminal: " + element + ", will match in transition()");
            } else {
                throw new RuntimeException("Unexpected production rule element type: " + element.getClass());
            }
        }
    }

    private void handleError(Token token) {
        System.err.println("Syntax Error: Unexpected token '" + token.getLexeme() + "' at line " + token.getLine() + ", column " + token.getColumn());

        // Pop states aggressively to attempt recovery
        while (!isStackEmpty() && !GrammarRule.isValidStart(getCurrentState(), token.getLexeme())) {
            popState();
        }

        System.err.println("Attempting recovery, new state: " + getCurrentState());
    }

    public NonTerminal getCurrentState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }

    public boolean isStackEmpty() {
        return stateStack.isEmpty();
    }
}
