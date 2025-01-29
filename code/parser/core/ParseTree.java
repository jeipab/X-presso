package parser.core;

import lexer.*;
import java.util.List;

public class ParseTree {
    private ParseTreeNode root;
    
    public ParseTree() {
        this.root = null;
    }

    public void setRoot(ParseTreeNode root) {
        this.root = root;
    }

    public ParseTreeNode getRoot() {
        return root;
    }

    // Build the parse tree (could be from a grammar parser or syntax analysis)
    public void build(ParseTreeNode rootNode) {
        this.root = rootNode;
    }

    // Visualize the tree (could be done with simple print statements or graphical representation)
    public void visualize() {
        if (root != null) {
            root.traversePreOrder();
        }
    }

    // Export the tree to a specific format (e.g., JSON or XML)
    public String export() {
        return exportNode(root);
    }

    private String exportNode(ParseTreeNode node) {
        if (node == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"value\": \"" + node.toString() + "\",\n");
        sb.append("  \"children\": [\n");
        for (ParseTreeNode child : node.children) {
            sb.append(exportNode(child) + ",\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    // Validate the parse tree (checking for syntax correctness, such as matching parenthesis or proper structure)
    public boolean validate() {
        return validateNode(root);
    }

    private boolean validateNode(ParseTreeNode node) {
        if (node == null) return true;
        
        // Example of validation: check if the node type is valid based on some conditions
        if (node.getParent() == null && node.type == TokenType.UNKNOWN) {
            return false; // root node can't have an unknown type
        }

        // Additional validations can be added for more complex grammar structures
        for (ParseTreeNode child : node.children) {
            if (!validateNode(child)) {
                return false;
            }
        }
        return true;
    }
}
