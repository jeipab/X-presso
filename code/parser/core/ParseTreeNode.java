package parser.core;

import java.util.ArrayList;
import java.util.List;

// Represents a node in the parse tree
class ParseTreeNode {
    private String value; // Can be terminal or non-terminal
    private boolean isTerminal; // Identifies if it's a terminal
    private List<ParseTreeNode> children;
    private ParseTreeNode parent;

    public ParseTreeNode(String value, boolean isTerminal) {
        this.value = value;
        this.isTerminal = isTerminal;
        this.children = new ArrayList<>();
    }

    public void addChild(ParseTreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    private void setParent(ParseTreeNode parent) {
        this.parent = parent;
    }

    public List<ParseTreeNode> getChildren() {
        return children;
    }

    public String getValue() {
        return value;
    }

    public boolean isTerminal() {
        return isTerminal;
    }
}
