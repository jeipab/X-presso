package parser.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import lexer.*;

public class TokenVisualizer {
    private Map<String, Integer> tokenCounter;
    private StringBuilder dotBuilder;
    private int nodeCount;
    private Stack<String> scopeStack;

    public TokenVisualizer() {
        this.tokenCounter = new HashMap<>();
        this.dotBuilder = new StringBuilder();
        this.nodeCount = 0;
        this.scopeStack = new Stack<>();
    }

    public String convertTokensToVizFormat(List<Token> tokens) {
        dotBuilder.append("digraph AbstractSyntaxTree {\n");
        dotBuilder.append("    node [shape=ellipse];\n\n");

        // Root node
        String rootNode = createNode("Program");
        scopeStack.push(rootNode);

        // Build AST using lexeme-based comparison
        buildAST(rootNode, tokens);

        dotBuilder.append("}\n");
        return dotBuilder.toString();
    }

    private void buildAST(String parentNode, List<Token> tokens) {
        int i = 0;
        while (i < tokens.size()) {
            Token token = tokens.get(i);
            String lexeme = token.getLexeme();

            // Skip whitespace and comments
            if (lexeme.matches("\\s+") || lexeme.startsWith("//") || lexeme.startsWith("/*")) {
                i++;
                continue;
            }

            // === BASE STRUCTURE (Class, Function) ===
            if (lexeme.equals("class")) {
                String classNode = createNode("ClassDecl");
                addEdge(parentNode, classNode);
                i++;

                if (i < tokens.size()) {
                    addEdge(classNode, createNode("Identifier\n'" + tokens.get(i).getLexeme() + "'"));
                    i++;
                }

                if (i < tokens.size() && tokens.get(i).getLexeme().equals(":>")) {
                    i++;
                    String inheritNode = createNode("Inheritance");
                    addEdge(classNode, inheritNode);
                    addEdge(inheritNode, createNode("ParentClass\n'" + tokens.get(i).getLexeme() + "'"));
                    i++;
                }

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("{")) {
                    String bodyNode = createNode("ClassBody");
                    addEdge(classNode, bodyNode);
                    scopeStack.push(bodyNode);
                    i++;
                }
                continue;
            }

            // === FUNCTION DECLARATIONS ===
            if (i + 2 < tokens.size() && tokens.get(i + 1).getLexeme().equals("(")) {
                String functionNode = createNode("FunctionDecl");
                addEdge(parentNode, functionNode);
                addEdge(functionNode, createNode("Function\n'" + lexeme + "'"));

                i += 2;
                String paramNode = createNode("Parameters");
                addEdge(functionNode, paramNode);

                while (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                    addEdge(paramNode, createNode("Param\n'" + tokens.get(i).getLexeme() + "'"));
                    i++;
                }
                i++; // Move past `)`
                continue;
            }

            // === EXPRESSIONS & OPERATIONS ===
            if (i + 1 < tokens.size() && tokens.get(i + 1).getLexeme().matches("[=+\\-*/]")) {
                String exprNode = createNode("Expression");
                addEdge(parentNode, exprNode);
                addEdge(exprNode, createNode("Variable\n'" + lexeme + "'"));
                addEdge(exprNode, createNode("Operator\n'" + tokens.get(i + 1).getLexeme() + "'"));

                i += 2;
                if (i < tokens.size()) {
                    addEdge(exprNode, createNode("Value\n'" + tokens.get(i).getLexeme() + "'"));
                    i++;
                }
                continue;
            }

            // === CONDITIONALS (IF-ELSE, SWITCH) ===
            if (lexeme.equals("if")) {
                String ifNode = createNode("IfStatement");
                addEdge(parentNode, ifNode);
                i++;

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("(")) {
                    String conditionNode = createNode("Condition");
                    addEdge(ifNode, conditionNode);
                    i++;
                    while (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                        addEdge(conditionNode, createNode("Expr\n'" + tokens.get(i).getLexeme() + "'"));
                        i++;
                    }
                    i++;
                }

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("{")) {
                    String blockNode = createNode("IfBlock");
                    addEdge(ifNode, blockNode);
                    scopeStack.push(blockNode);
                    i++;
                }
                continue;
            }

            if (lexeme.equals("else")) {
                String elseNode = createNode("ElseStatement");
                addEdge(parentNode, elseNode);
                i++;
                if (i < tokens.size() && tokens.get(i).getLexeme().equals("{")) {
                    String blockNode = createNode("ElseBlock");
                    addEdge(elseNode, blockNode);
                    scopeStack.push(blockNode);
                    i++;
                }
                continue;
            }

            // === LOOPS (FOR, WHILE) ===
            if (lexeme.equals("for") || lexeme.equals("while")) {
                String loopNode = createNode(lexeme.equals("for") ? "ForLoop" : "WhileLoop");
                addEdge(parentNode, loopNode);
                i++;

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("(")) {
                    String conditionNode = createNode("LoopCondition");
                    addEdge(loopNode, conditionNode);
                    i++;
                    while (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                        addEdge(conditionNode, createNode("Expr\n'" + tokens.get(i).getLexeme() + "'"));
                        i++;
                    }
                    i++;
                }

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("{")) {
                    String bodyNode = createNode("LoopBody");
                    addEdge(loopNode, bodyNode);
                    scopeStack.push(bodyNode);
                    i++;
                }
                continue;
            }

            // === INPUT & OUTPUT ===
            if (lexeme.equals("Input::get")) {
                String inputNode = createNode("InputStatement");
                addEdge(parentNode, inputNode);
                i++;
                continue;
            }

            if (lexeme.equals("Output::print")) {
                String outputNode = createNode("OutputStatement");
                addEdge(parentNode, outputNode);
                i++;
                continue;
            }

            // === SCOPE CLOSURE ===
            if (lexeme.equals("}")) {
                if (!scopeStack.isEmpty()) {
                    scopeStack.pop();
                }
                i++;
                continue;
            }

            i++;
        }
    }

    private String createNode(String label) {
        String nodeId = "node" + nodeCount++;
        dotBuilder.append(String.format("    %s [label=\"%s\"];\n", nodeId, label));
        return nodeId;
    }

    private void addEdge(String from, String to) {
        dotBuilder.append(String.format("    %s -> %s;\n", from, to));
    }

    private String escapeString(String str) {
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
