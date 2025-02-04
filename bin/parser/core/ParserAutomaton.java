package parser.core;

import lexer.Token;
import parser.grammar.GrammarRule;
import parser.grammar.NonTerminal;
import java.util.*;

public class ParserAutomaton {
    private final Stack<NonTerminal> stateStack;
    private final Stack<Integer> productionIndexStack;
    private final Stack<Integer> elementIndexStack;
    private final Set<String> expandedStates;
    private final Set<String> attemptedProductions;
    private List<Token> tokens;
    private int currentTokenIndex;
    private static final int MAX_STACK_DEPTH = 50;
    private boolean debug = true;

    public ParserAutomaton() {
        this.stateStack = new Stack<>();
        this.productionIndexStack = new Stack<>();
        this.elementIndexStack = new Stack<>();
        this.expandedStates = new HashSet<>();
        this.attemptedProductions = new HashSet<>();
        this.currentTokenIndex = 0;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public void reset() {
        stateStack.clear();
        productionIndexStack.clear();
        elementIndexStack.clear();
        expandedStates.clear();
        attemptedProductions.clear();
        currentTokenIndex = 0;
    }

    private void debugPrint(String message) {
        if (debug) {
            System.out.println("DEBUG: " + message);
            System.out.println("Current Token: " + (getCurrentToken() != null ? getCurrentToken().getLexeme() : "null"));
            System.out.println("Stack: " + stateStack);
            System.out.println("Productions: " + productionIndexStack);
            System.out.println("Elements: " + elementIndexStack);
            System.out.println("---");
        }
    }

    public void pushState(NonTerminal state) {
        if (stateStack.size() >= MAX_STACK_DEPTH) {
            throw new RuntimeException("Maximum parser stack depth exceeded");
        }

        String stateKey = getStateKey(state);
        debugPrint("Attempting to push state: " + state + ", key: " + stateKey);
        
        if (expandedStates.contains(stateKey)) {
            debugPrint("State already expanded: " + state);
            return;
        }

        expandedStates.add(stateKey);
        stateStack.push(state);
        productionIndexStack.push(0);
        elementIndexStack.push(0);
        System.out.println("Pushed state: " + state);
    }

    private String getStateKey(NonTerminal state) {
        return state.name() + "@" + currentTokenIndex + "@" + getStackContext();
    }

    private String getStackContext() {
        return String.join("->", stateStack.stream()
            .map(NonTerminal::name)
            .toList());
    }

    private Token getCurrentToken() {
        return currentTokenIndex < tokens.size() ? tokens.get(currentTokenIndex) : null;
    }

    public boolean processTokens() {
        while (!stateStack.isEmpty() && currentTokenIndex < tokens.size()) {
            debugPrint("Processing tokens, current index: " + currentTokenIndex);
            Token currentToken = getCurrentToken();
            if (currentToken != null) {
                debugPrint("Current token: " + currentToken.getLexeme());
            }

            if (!processCurrentState()) {
                debugPrint("Failed to process current state");
                if (!backtrack()) {
                    debugPrint("Backtracking failed");
                    return false;
                }
            }
        }
        return stateStack.isEmpty() && currentTokenIndex == tokens.size();
    }

    private boolean processCurrentState() {
        NonTerminal currentState = stateStack.peek();
        int productionIndex = productionIndexStack.peek();
        int elementIndex = elementIndexStack.peek();

        debugPrint("Processing state: " + currentState + 
                  ", production: " + productionIndex + 
                  ", element: " + elementIndex);

        List<List<Object>> productions = GrammarRule.getProductions(currentState);
        Token currentToken = getCurrentToken();

        // Try to match current token first
        if (currentToken != null && productionIndex == 0) {
            int matchingProduction = findProductionMatchingToken(productions, currentToken);
            if (matchingProduction != -1) {
                debugPrint("Found matching production: " + matchingProduction);
                productionIndexStack.pop();
                productionIndexStack.push(matchingProduction);
                productionIndex = matchingProduction;
            }
        }

        if (productionIndex >= productions.size()) {
            debugPrint("No more productions to try");
            return false;
        }

        List<Object> currentProduction = productions.get(productionIndex);
        if (elementIndex >= currentProduction.size()) {
            debugPrint("Completed production");
            popState();
            return true;
        }

        Object currentElement = currentProduction.get(elementIndex);
        debugPrint("Current element: " + currentElement);

        if (currentElement instanceof NonTerminal) {
            return handleNonTerminal((NonTerminal) currentElement);
        } else if (currentElement instanceof String) {
            return handleTerminal((String) currentElement);
        } else if (currentElement instanceof Optional<?>) {
            return handleOptional((Optional<?>) currentElement);
        }

        return false;
    }

    private int findProductionMatchingToken(List<List<Object>> productions, Token token) {
        for (int i = 0; i < productions.size(); i++) {
            if (productionCouldMatchToken(productions.get(i), token)) {
                return i;
            }
        }
        return -1;
    }

    private boolean productionCouldMatchToken(List<Object> production, Token token) {
        if (production.isEmpty()) {
            return false;
        }
        
        Object firstElement = production.get(0);
        if (firstElement instanceof String) {
            return firstElement.equals(token.getLexeme());
        } else if (firstElement instanceof NonTerminal) {
            return GrammarRule.couldGenerateToken((NonTerminal) firstElement, token);
        }
        return false;
    }

    private boolean handleNonTerminal(NonTerminal nonTerm) {
        debugPrint("Handling NonTerminal: " + nonTerm);
        String stateKey = getStateKey(nonTerm);
        
        if (!expandedStates.contains(stateKey)) {
            pushState(nonTerm);
            return true;
        }
        return false;
    }

    private boolean handleTerminal(String terminal) {
        Token currentToken = getCurrentToken();
        debugPrint("Handling Terminal: " + terminal + ", Current Token: " + 
                  (currentToken != null ? currentToken.getLexeme() : "null"));
        
        if (currentToken != null && currentToken.getLexeme().equals(terminal)) {
            debugPrint("Terminal matched");
            currentTokenIndex++;
            elementIndexStack.set(elementIndexStack.size() - 1, 
                                elementIndexStack.peek() + 1);
            return true;
        }
        return false;
    }

    private boolean handleOptional(Optional<?> optional) {
        debugPrint("Handling Optional element");
        if (optional.isPresent()) {
            Object opt = optional.get();
            if (opt instanceof NonTerminal) {
                NonTerminal nonTerm = (NonTerminal) opt;
                String stateKey = getStateKey(nonTerm);
                
                if (!expandedStates.contains(stateKey)) {
                    pushState(nonTerm);
                }
            }
        }
        elementIndexStack.set(elementIndexStack.size() - 1, 
                            elementIndexStack.peek() + 1);
        return true;
    }

    private boolean backtrack() {
        while (!stateStack.isEmpty()) {
            int currentProdIndex = productionIndexStack.peek();
            List<List<Object>> productions = GrammarRule.getProductions(stateStack.peek());
            
            if (currentProdIndex + 1 < productions.size()) {
                productionIndexStack.set(productionIndexStack.size() - 1, currentProdIndex + 1);
                elementIndexStack.set(elementIndexStack.size() - 1, 0);
                return true;
            }
            
            popState();
        }
        return false;
    }

    public void popState() {
        if (!stateStack.isEmpty()) {
            NonTerminal top = stateStack.pop();
            productionIndexStack.pop();
            elementIndexStack.pop();
            String stateKey = getStateKey(top);
            expandedStates.remove(stateKey);
            System.out.println("Popped state: " + top);
        }
    }

    public NonTerminal getCurrentState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }

    public boolean isStackEmpty() {
        return stateStack.isEmpty();
    }
}