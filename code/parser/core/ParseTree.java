package parser.core;

import parser.grammar.NonTerminal;

public class ParseTree {
    private final ParseTreeNode root;

    public ParseTree(NonTerminal rootType) {
        this.root = new ParseTreeNode(rootType);
    }

    public ParseTreeNode getRoot() {
        return root;
    }

    public ParseTreeNode addChild(NonTerminal type) {
        return root.addChild(type);
    }

    public ParseTreeNode addChild(String value) {
        return root.addChild(value);
    }

    public static class Node extends ParseTreeNode {
        public Node(String value) {
            super(value, null);
        }

        @Override
        public Node addChild(NonTerminal type) {
            ParseTreeNode child = super.addChild(type);
            return new Node(child.getValue());
        }
    }
}

/*
import parser.grammar.NonTerminal;
public class ParseTree {
     private final Node root;
 
     public ParseTree(NonTerminal spProg) {
         this.root = new Node(NonTerminal.SP_PROG.name()); // Root is always "SP_PROG"
     }
 
     public Node getRoot() {
         return root;
     }
 
     public Node addChild(NonTerminal type) {
         Node child = new Node(type.name());
         root.addChild(child);  // Always adds children to the root, but doesn't replace it
         return child;
     }
 
     // Define Node as an inner class to match Parser.java expectations
     public static class Node extends ParseTreeNode {
         public Node(String value) {
             super(value, null); // Pass null for TokenType since it's not explicitly used
         }
 
         public Node addChild(NonTerminal type) {
             Node child = new Node(type.name());
             super.addChild(child);
             return child;
         }
     }
 }
 

 */