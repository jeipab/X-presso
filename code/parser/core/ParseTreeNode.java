package parser.core;
import lexer.*;
import parser.grammar.*;
import java.util.ArrayList;
import java.util.List;

public class ParseTreeNode {
    private String value;
    List<ParseTreeNode> children;
    TokenType type; // TokenType could be an enum or class representing the token type
    private NonTerminal nonTerminal; // NonTerminal type representing non-terminal symbols in the grammar
    private ParseTreeNode parent;
    
    public ParseTreeNode(String value, TokenType type) {
        this.value = value;
        this.type = type;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    // Add a child node to the current node
    public void addChild(ParseTreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    // Remove a child node from the current node
    public void removeChild(ParseTreeNode child) {
        children.remove(child);
        child.setParent(null);
    }

    // Set the parent node of this node
    private void setParent(ParseTreeNode parent) {
        this.parent = parent;
    }

    // Get the parent of the current node
    public ParseTreeNode getParent() {
        return parent;
    }

    // Traverse the tree in pre-order (root -> left -> right)
    public void traversePreOrder() {
        System.out.println(this);
        for (ParseTreeNode child : children) {
            child.traversePreOrder();
        }
    }

    // Traverse the tree in post-order (left -> right -> root)
    public void traversePostOrder() {
        for (ParseTreeNode child : children) {
            child.traversePostOrder();
        }
        System.out.println(this);
    }

    // Convert node to string representation (for visualization)
    @Override
    public String toString() {
        return "Node[value=" + value + ", type=" + type + "]";
    }
}
