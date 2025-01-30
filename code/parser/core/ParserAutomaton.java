package parser.core;

import lexer.Token;
import parser.grammar.GrammarRule;
import parser.grammar.NonTerminal;
import util.SyntaxErrorHandler.SyntaxError;
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
    
                if (firstElement instanceof String) { 
                    // ✅ Only expand if the terminal matches the token
                    if (((String) firstElement).equals(token.getLexeme())) {
                        return transition(token); // Ensure token is consumed
                    }
                } else if (firstElement instanceof NonTerminal) {
                    // ✅ Only expand if the token belongs to this nonterminal
                    if (canExpand((NonTerminal) firstElement, token)) {
                        expandNonTerminal(currentState, production);
                        return true; // Expansion happens, but no token consumed yet
                    }
                }
            } else {
                // Handle empty production (used to exit recursive states like CLASS_MODS)
                System.out.println("Using empty production for state: " + currentState);
                popState();
                return true;
            }
        }
    
        // If no valid transition is found, return false
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
    
                        // Pop the current state after matching the terminal
                        System.out.println("Popping state: " + currentState);
                        popState(); // No need to pass parent here
    
                        // Reset state only if stack is empty
                        if (isStackEmpty()) {
                            pushState(NonTerminal.SP_PROG);
                        }
    
                        return true;
                    }
                } else if (firstElement instanceof NonTerminal) {
                    expandNonTerminal(currentState, production);
                    return transition(token);
                }
            }
        }
    
        return false;
    }

    private void expandNonTerminal(NonTerminal nonTerminal, List<Object> production) {
        System.out.println("Expanding " + nonTerminal + " with production: " + production);

        // Push the production elements in reverse order (right to left)
        for (int i = production.size() - 1; i >= 0; i--) {
            Object element = production.get(i);
    
            if (element instanceof NonTerminal) {
                // Avoid pushing duplicates
                if (!stateStack.contains(element)) {
                    pushState((NonTerminal) element);
                }
            } else if (element instanceof String) {
                // Match terminals in transition(), not in stack
                System.out.println("Encountered terminal: " + element + ", will match in transition()");
            } else {
                throw new RuntimeException("Unexpected production rule element type: " + element.getClass());
            }
        }
    }

    private boolean canExpand(NonTerminal nonTerminal, Token token) {
        List<List<Object>> productions = GrammarRule.getProductions(nonTerminal);
    
        for (List<Object> production : productions) {
            if (!production.isEmpty()) {
                Object firstElement = production.get(0);
    
                if (firstElement instanceof String) {
                    // ✅ If token matches a terminal in the first position, allow expansion
                    if (((String) firstElement).equals(token.getLexeme())) {
                        return true;
                    }
                } else if (firstElement instanceof NonTerminal) {
                    // ✅ If it's a non-terminal, recursively check if it can expand to match the token
                    if (canExpand((NonTerminal) firstElement, token)) {
                        return true;
                    }
                }
            }
        }
        
        return false; // ❌ Avoid expansion if no match is found
    }

    public NonTerminal getCurrentState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }

    public boolean isStackEmpty() {
        return stateStack.isEmpty();
    }
}