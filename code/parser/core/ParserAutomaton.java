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

    public void popState(ParseTreeNode parent) {
        if (!stateStack.isEmpty()) {
            NonTerminal top = stateStack.peek();
            System.out.println("Popped state: " + top);
    
            // ✅ Ensure that only fully processed rules are popped
            if (isProductionComplete(parent)) {
                stateStack.pop();
                if (parent != null && parent.getParent() != null) {
                    parent = parent.getParent();
                }
            }
        }
    }

    public boolean processToken(Token token) {
        while (!isStackEmpty()) {
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
                        expandNonTerminal(currentState, production);
                        return true;
                    } else if (firstElement instanceof String) {
                        // If it's a terminal, it must match the token lexeme
                        return transition(token, null);
                    }
                }
            }
        }
        return false;
    }

    public boolean transition(Token token, ParseTreeNode parent) {
        if (isStackEmpty()) {
            return false;
        }
    
        NonTerminal currentState = getCurrentState();
        List<List<Object>> productions = GrammarRule.getProductions(currentState);
    
        // Try each production for the current state
        for (List<Object> production : productions) {
            if (!production.isEmpty()) {
                Object firstElement = production.get(0);
    
                if (firstElement instanceof String) { 
                    // If it's a terminal, check if it matches the current token
                    String terminal = (String) firstElement;
                    if (terminal.equals(token.getLexeme())) {
                        System.out.println("Matched terminal: " + terminal + " with token: " + token.getLexeme());
                        
                        // Pop the current state after matching the terminal
                        popState(parent);
                        if (parent != null) {
                            parent.addChild(token.getLexeme());
                        }
    
                        // Move to the next state in the production
                        if (production.size() > 1) {
                            Object nextElement = production.get(1);
                            if (nextElement instanceof NonTerminal) {
                                pushState((NonTerminal) nextElement);
                            } else if (nextElement instanceof String) {
                                System.out.println("Next terminal in production: " + nextElement + ", will match in next transition");
                            }
                        } else {
                            // If this is the last element in the production, transition back to the parent state
                            if (!stateStack.isEmpty()) {
                                NonTerminal parentState = stateStack.peek();
                                System.out.println("Transitioning back to parent state: " + parentState);
                            }
                        }
    
                        return true;
                    } else {
                        System.out.println("Terminal " + terminal + " did not match token: " + token.getLexeme() + ", trying next production");
                    }
                } else if (firstElement instanceof NonTerminal) {
                    // If it's a non-terminal, expand it and try again
                    System.out.println("Expanding non-terminal: " + firstElement);
                    expandNonTerminal(currentState, production);
                    return transition(token, parent);
                }
            }
        }
    
        // If no production matches, report an error
        System.out.println("No matching production for token: " + token.getLexeme() + " in state: " + currentState);
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

    private boolean isProductionComplete(ParseTreeNode node) {
        if (node == null) return false;
    
        // Ensure the node represents a non-terminal
        NonTerminal nodeType;
        try {
            nodeType = NonTerminal.valueOf(node.getValue());  // Convert node value back to NonTerminal
        } catch (IllegalArgumentException e) {
            return true;  // If it's not a NonTerminal, assume it's a terminal and is complete
        }
    
        // ✅ Get expected children from the grammar
        List<List<Object>> productions = GrammarRule.getProductions(nodeType);
        int expectedChildren = productions.size(); // Expected number of productions
    
        return node.getChildren().size() >= expectedChildren;
    }

    private boolean hasRemainingChildren(ParseTreeNode node) {
        if (node == null) return false;
        return !node.getChildren().isEmpty();
    }

    public NonTerminal getCurrentState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }

    public boolean isStackEmpty() {
        return stateStack.isEmpty();
    }
}