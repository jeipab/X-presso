package parser.core;

import parser.grammar.NonTerminal;
import parser.grammar.GrammarRule;
import lexer.Token;
import lexer.TokenType;
import java.util.Stack;
import java.util.List;

public class ParserAutomaton {
    private Stack<State> stateStack;

    public ParserAutomaton() {
        this.stateStack = new Stack<>();
    }

    // Push a new state onto the stack
    public void pushState(NonTerminal nonTerminal) {
        stateStack.push(new State(nonTerminal));
    }

    // Pop the top state off the stack
    public void popState() {
        if (!stateStack.isEmpty()) {
            stateStack.pop();
        }
    }

    // Drive the parsing process
    public boolean parse(List<Token> tokens) {
        try {
            for (Token token : tokens) {
                while (true) {
                    if (stateStack.isEmpty()) {
                        throw new RuntimeException("Unexpected end of input.");
                    }

                    State currentState = stateStack.peek();
                    NonTerminal currentNonTerminal = currentState.getSymbol();

                    // Get the production rules for the current non-terminal
                    List<GrammarRule> rules = NonTerminal.getProductions(currentNonTerminal);

                    boolean matched = false;
                    for (GrammarRule rule : rules) {
                        if (ruleMatchesToken(rule, token)) {
                            applyRule(rule);
                            matched = true;
                            break;
                        }
                    }

                    if (matched) {
                        break; // Proceed to the next token
                    } else {
                        handleError(token); // Handle syntax errors
                        return false;
                    }
                }
            }

            // Final validation
            if (!stateStack.isEmpty()) {
                throw new RuntimeException("Parsing incomplete: stack not empty.");
            }

            return true; // Parsing succeeded

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            return false; // Parsing failed
        }
    }

    // Apply a grammar rule (reduce action)
    private void applyRule(GrammarRule rule) {
        // Pop states for the rule's right-hand side
        for (int i = 0; i < rule.getRightSide().size(); i++) {
            popState();
        }

        // Push a new state for the rule's left-hand side
        pushState(rule.getLeftSide());
    }

    // Check if a grammar rule matches a token
    private boolean ruleMatchesToken(GrammarRule rule, Token token) {
        // The first symbol on the right-hand side of the rule should match the token type
        if (rule.getRightSide().isEmpty()) {
            return false; // Invalid rule
        }

        Object firstSymbol = rule.getRightSide().get(0);
        if (firstSymbol instanceof TokenType) {
            return token.getType() == firstSymbol;
        } else if (firstSymbol instanceof NonTerminal) {
            // For non-terminals, check if the token is in their FIRST set
            return NonTerminal.getFirst((NonTerminal) firstSymbol).contains(token.getType());
        }

        return false;
    }

    // Handle syntax errors
    private void handleError(Token token) {
        System.err.println("Syntax error at token: " + token);
    }

    // Inner State class
    private class State {
        private NonTerminal symbol;

        State(NonTerminal symbol) {
            this.symbol = symbol;
        }

        public NonTerminal getSymbol() {
            return symbol;
        }
    }
}
