package parser.core;

import parser.grammar.NonTerminal;
import parser.grammar.GrammarRule;
import lexer.Token;
import util.SyntaxErrorHandler;
import java.util.Stack;
import java.util.List;
import java.util.Map;

public class ParserAutomaton {
    private final Stack<Object> stateStack;
    private final Map<NonTerminal, List<List<Object>>> grammarRules;
    private final SyntaxErrorHandler syntaxErrorHandler;
    private NonTerminal currentState;

    public ParserAutomaton() {
        this.stateStack = new Stack<>();
        this.grammarRules = GrammarRule.loadRules();
        this.syntaxErrorHandler = new SyntaxErrorHandler();
        this.currentState = NonTerminal.SP_PROG; // Start at program
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

    public boolean processToken(Token token) {
        List<List<Object>> productions = grammarRules.get(currentState);
        if (productions == null) return false;

        for (List<Object> production : productions) {
            if (!production.isEmpty() && production.get(0).equals(token.getLexeme())) {
                return true;
            }
        }
        return false;
    }

    public void transition(Token token) {
        if (stateStack.isEmpty()) {
            syntaxErrorHandler.reportError("Unexpected token '" + token.getLexeme() + "' at end of input.");
            return;
        }

        Object top = stateStack.peek();

        if (top instanceof String) {
            if (top.equals(token.getLexeme())) {
                stateStack.pop(); // Consume terminal
            } else {
                syntaxErrorHandler.reportError("Syntax error: Expected '" + top + "', found '" + token.getLexeme() + "'.");
            }
            return;
        }

        if (top instanceof NonTerminal) {
            NonTerminal nonTerminal = (NonTerminal) top;
            List<List<Object>> productions = grammarRules.get(nonTerminal);

            for (List<Object> production : productions) {
                if (!production.isEmpty() && production.get(0).equals(token.getLexeme())) {
                    stateStack.pop(); 
                    pushProduction(production);
                    return;
                }
            }

            syntaxErrorHandler.reportError(
                "Unexpected token '" + token.getLexeme() + "' in state " + nonTerminal +
                ". Expected one of: " + getExpectedTokens(productions)
            );
        }
    }

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
