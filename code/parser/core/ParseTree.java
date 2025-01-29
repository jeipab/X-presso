package parser.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import parser.grammar.NonTerminal;

public class ParseTree {
    private final ParseTreeNode root;
    private List<ParseTreeNode> children = new ArrayList<>();


    public ParseTree(NonTerminal rootType) {
        this.root = new ParseTreeNode(rootType);
    }

    public ParseTreeNode getRoot() {
        return root;
    }

    public ParseTreeNode addChild(NonTerminal type) {
        ParseTreeNode child = new ParseTreeNode(type);
        this.children.add(child);
        return child;
    }

    public ParseTreeNode addChild(String value) {
        return root.addChild(value);
    }

    public static class Node extends ParseTreeNode {
        public Node(String value) {
            super(value, null);
        }

        public Node(NonTerminal expr) {
            super(expr.name(), null);
        }
    }

    public String toGraphviz() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ParseTree {\n");
        sb.append("    node [shape=ellipse, fontname=\"Arial\"];\n");

        AtomicInteger nodeId = new AtomicInteger(0); // Ensure unique IDs
        toGraphvizRecursive(root, sb, nodeId);

        sb.append("}");
        return sb.toString();
    }

    private void toGraphvizRecursive(ParseTreeNode node, StringBuilder sb, AtomicInteger nodeId) {
        if (node == null) return;
    
        int currentNodeId = nodeId.getAndIncrement();
        String nodeLabel = escapeGraphvizLabel(node.getValue());
    
        sb.append(String.format("    node_%d [label=\"%s\"];\n", currentNodeId, nodeLabel));
    
        for (ParseTreeNode child : node.getChildren()) {
            int childNodeId = nodeId.getAndIncrement();
            String childLabel = escapeGraphvizLabel(child.getValue());
    
            sb.append(String.format("    node_%d [label=\"%s\"];\n", childNodeId, childLabel));
            sb.append(String.format("    node_%d -> node_%d;\n", currentNodeId, childNodeId)); // ✅ Ensure correct connections
    
            toGraphvizRecursive(child, sb, nodeId);
        }
    }
    

    private String escapeGraphvizLabel(String label) {
        if (label == null) return "NULL";
        return label.replace("\"", "\\\"").replace("\\", "\\\\");
    }

    /**
     * Saves the DOT format output to a file in the specified directory.
     * @param outputPath The file path where the DOT file should be saved.
     */
    public void saveToDotFile(String outputPath) {
        String dotContent = toGraphviz();

        try {
            // Ensure directory exists
            File file = new File(outputPath);
            file.getParentFile().mkdirs(); // Create parent directories if not exist

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(dotContent);
                System.out.println("DOT file successfully saved at: " + outputPath);
            }
        } catch (IOException e) {
            System.err.println("Error saving DOT file: " + e.getMessage());
        }
    }
    
}