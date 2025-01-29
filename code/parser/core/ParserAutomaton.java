package parser.core;

import lexer.Token;
import parser.grammar.NonTerminal;
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
    }

    public void popState() {
        if (!stateStack.isEmpty()) {
            stateStack.pop();
        }
    }

    public NonTerminal getCurrentState() {
        return stateStack.isEmpty() ? null : stateStack.peek();
    }

    public boolean isStackEmpty() {
        return stateStack.isEmpty();
    }

    public void transition(Token token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transition'");
    }

    public boolean processToken(Token token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processToken'");
    }
}