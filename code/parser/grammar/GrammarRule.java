package parser.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lexer.TokenType;

// Represents a production rule in the grammar
public class GrammarRule {
    private NonTerminal leftSide; // Left-hand side of the production
    private List<Symbol> rightSide; // Right-hand side (can contain Terminals or NonTerminals)

    public GrammarRule(NonTerminal leftSide, List<Symbol> rightSide) {
        this.leftSide = leftSide;
        this.rightSide = new ArrayList<>(rightSide);
    }

    // Getter for left-hand side
    public NonTerminal getLeftSide() {
        return leftSide;
    }

    // Getter for right-hand side (returns an unmodifiable list for safety)
    public List<Symbol> getRightSide() {
        return new ArrayList<>(rightSide);
    }

    // Checks if the rule can produce an empty string (nullable)
    public boolean isNullable() {
        if (rightSide.isEmpty()) return true; // Directly nullable if RHS is empty
        for (Symbol symbol : rightSide) {
            if (!(symbol instanceof NonTerminal) || !((NonTerminal) symbol).isNullable()) {
                return false;
            }
        }
        return true;
    }

    // Computes the FIRST set for this rule
    public Set<TokenType> getFirst() {
        Set<TokenType> firstSet = new HashSet<>();

        for (Symbol symbol : rightSide) {
            if (symbol instanceof Terminal) {
                firstSet.add(((Terminal) symbol).getTokenType());
                break; // Stop after adding the first terminal
            } else if (symbol instanceof NonTerminal) {
                firstSet.addAll(((NonTerminal) symbol).getFirst());
                if (!((NonTerminal) symbol).isNullable()) {
                    break; // Stop if the non-terminal is not nullable
                }
            }
        }
        return firstSet;
    }

    // Computes the FOLLOW set for this rule
    public Set<TokenType> getFollow() {
        Set<TokenType> followSet = new HashSet<>();

        for (int i = 0; i < rightSide.size(); i++) {
            Symbol current = rightSide.get(i);
            if (current instanceof NonTerminal) {
                NonTerminal nonTerminal = (NonTerminal) current;
                // Add FIRST of the next symbol to FOLLOW of current
                if (i + 1 < rightSide.size()) {
                    Symbol next = rightSide.get(i + 1);
                    if (next instanceof Terminal) {
                        followSet.add(((Terminal) next).getTokenType());
                    } else if (next instanceof NonTerminal) {
                        followSet.addAll(((NonTerminal) next).getFirst());
                        if (((NonTerminal) next).isNullable()) {
                            followSet.addAll(nonTerminal.getFollow());
                        }
                    }
                } else {
                    // If current is the last symbol, add FOLLOW of leftSide
                    followSet.addAll(leftSide.getFollow());
                }
            }
        }
        return followSet;
    }

    // Validates if a production is valid
    public boolean validate() {
        if (leftSide == null || rightSide == null) {
            return false; // Invalid if either side is null
        }
        for (Symbol symbol : rightSide) {
            if (!(symbol instanceof Terminal || symbol instanceof NonTerminal)) {
                return false; // RHS must only contain valid Terminals or NonTerminals
            }
        }
        return true;
    }

    // Converts the rule to a readable string format
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(leftSide).append(" -> ");
        for (Symbol symbol : rightSide) {
            sb.append(symbol).append(" ");
        }
        return sb.toString().trim();
    }
}

// Common interface for Terminal and NonTerminal
interface Symbol {
    String getName();
}
