package parser.core;

import lexer.TokenType;
import parser.grammar.NonTerminal;

import java.util.ArrayList;
import java.util.List;

public class ParseTreeNode {
    private String value;
    private List<ParseTreeNode> children;
    private TokenType type;
    private ParseTreeNode parent;

    public ParseTreeNode(String value, TokenType type) {
        this.value = value;
        this.type = type;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public ParseTreeNode(NonTerminal nonTerminal) {
        this(nonTerminal.name(), null);
    }

    public ParseTreeNode addChild(NonTerminal type) {
        ParseTreeNode child = new ParseTreeNode(type);
        addChild(child);
        return child;
    }

    public ParseTreeNode addChild(String value) {
        ParseTreeNode child = new ParseTreeNode(value, null);
        addChild(child);
        return child;
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

    public ParseTreeNode getParent() {
        return parent;
    }

    public TokenType getType() {
        return type;
    }

    // Overridden method to support serialization to JSON
    public List<ParseTreeNode> getChildrenForJson() {
        return children;
    }

    @Override
    public String toString() {
        return value;
    }
}
/* 
public class ParseTreeNode {
    private String value;
    private List<ParseTreeNode> children;
    private TokenType type;
    private ParseTreeNode parent;

    public ParseTreeNode(String value, TokenType type) {
        this.value = value;
        this.type = type;
        this.children = new ArrayList<>();
        this.parent = null;
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

    // Overridden method to support serialization to JSON
    public List<ParseTreeNode> getChildrenForJson() {
        return children;
    }

    @Override
    public String toString() {
        return value;
    }
}


*/