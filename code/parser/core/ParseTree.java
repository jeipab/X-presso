package parser.core;

import java.util.*;


public class ParseTree {
    private ParseTreeNode root;

    public ParseTree(String startSymbol) {
        this.root = new ParseTreeNode(startSymbol, false);
    }

    public ParseTreeNode getRoot() {
        return root;
    }

    // Expands the first non-terminal (Leftmost)
    public void applyLeftmostDerivation(ParseTreeNode node, List<String> production) {
        if (node.isTerminal()) return;

        node.getChildren().clear(); // Clear previous children

        for (String symbol : production) {
            node.addChild(new ParseTreeNode(symbol, isTerminal(symbol)));
        }
    }

    // Expands the last non-terminal (Rightmost)
    public void applyRightmostDerivation(ParseTreeNode node, List<String> production) {
        if (node.isTerminal()) return;

        node.getChildren().clear();

        for (int i = production.size() - 1; i >= 0; i--) {
            node.addChild(new ParseTreeNode(production.get(i), isTerminal(production.get(i))));
        }
    }

    private boolean isTerminal(String symbol) {
        return symbol.matches("[a-z0-9]+"); // Lowercase letters and numbers are terminals
    }

    // Print Tree in Pyramid Format
    public void printTree() {
        List<List<String>> levels = new ArrayList<>();
        collectLevels(root, 0, levels);

        int maxWidth = levels.get(levels.size() - 1).size() * 4; // Adjust for spacing
        for (int i = 0; i < levels.size(); i++) {
            int padding = (maxWidth - levels.get(i).size() * 4) / 2;
            System.out.print(" ".repeat(padding));
            for (String value : levels.get(i)) {
                System.out.print(value + "   ");
            }
            System.out.println();
        }
    }

    private void collectLevels(ParseTreeNode node, int depth, List<List<String>> levels) {
        if (levels.size() <= depth) {
            levels.add(new ArrayList<>());
        }

        levels.get(depth).add(node.getValue());

        for (ParseTreeNode child : node.getChildren()) {
            collectLevels(child, depth + 1, levels);
        }
    }
}
