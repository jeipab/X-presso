package parser.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import lexer.*;

public class TokenVisualizer {
    private StringBuilder dotBuilder;
    private int nodeCount;
    private Stack<String> scopeStack;

    public TokenVisualizer() {
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

            // === CLASS DECLARATIONS ===
            if (lexeme.equals("class")) {
                String classNode = createNode("ClassDecl");
                addEdge(parentNode, classNode);
                i++;

                if (i < tokens.size()) {
                    addEdge(classNode, createNode("Identifier\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
                    i++;
                }

                if (i < tokens.size() && tokens.get(i).getLexeme().equals(":>")) {
                    i++;
                    String inheritNode = createNode("Inheritance");
                    addEdge(classNode, inheritNode);
                    addEdge(inheritNode, createNode("ParentClass\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
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

                // Add return type if available
                if (i > 0 && isDataType(tokens.get(i - 1).getLexeme())) {
                    addEdge(functionNode, createNode("ReturnType\n'" + escapeString(tokens.get(i - 1).getLexeme()) + "'"));
                }

                addEdge(functionNode, createNode("Function\n'" + escapeString(lexeme) + "'"));

                i += 2;
                String paramNode = createNode("Parameters");
                addEdge(functionNode, paramNode);

                while (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                    if (isDataType(tokens.get(i).getLexeme())) {
                        String dataTypeNode = createNode("DataType\n'" + escapeString(tokens.get(i).getLexeme()) + "'");
                        addEdge(paramNode, dataTypeNode);
                        i++;
                        if (i < tokens.size()) {
                            addEdge(dataTypeNode, createNode("Param\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
                            i++;
                        }
                    } else {
                        addEdge(paramNode, createNode("Param\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
                        i++;
                    }
                }
                i++; // Move past `)`

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("{")) {
                    String bodyNode = createNode("FunctionBody");
                    addEdge(functionNode, bodyNode);
                    scopeStack.push(bodyNode);
                    i++;
                }
                continue;
            }

            // === DECLARATIONS ===
            if (i + 1 < tokens.size() && tokens.get(i + 1).getLexeme().matches("=|:=")) {
                String declNode = createNode("Declaration");
                addEdge(parentNode, declNode);

                if (i > 0 && isDataType(tokens.get(i - 1).getLexeme())) {
                    addEdge(declNode, createNode("DataType\n'" + escapeString(tokens.get(i - 1).getLexeme()) + "'"));
                }

                addEdge(declNode, createNode("Variable\n'" + escapeString(lexeme) + "'"));
                addEdge(declNode, createNode("Operator\n'" + escapeString(tokens.get(i + 1).getLexeme()) + "'"));

                i += 2;
                if (i < tokens.size()) {
                    addEdge(declNode, createNode("Value\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
                    i++;
                }
                continue;
            }

            // === CONDITIONALS ===
            if (lexeme.equals("if")) {
                String ifNode = createNode("IfStatement");
                addEdge(parentNode, ifNode);
                i++;

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("(")) {
                    String conditionNode = createNode("Condition");
                    addEdge(ifNode, conditionNode);
                    i++;
                    while (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                        addEdge(conditionNode, createNode("Expr\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
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

            // === ELSE CLAUSES ===
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

            // === LOOPS ===
            if (lexeme.equals("for") || lexeme.equals("while")) {
                String loopNode = createNode(lexeme.equals("for") ? "ForLoop" : "WhileLoop");
                addEdge(parentNode, loopNode);
                i++;

                if (i < tokens.size() && tokens.get(i).getLexeme().equals("(")) {
                    String conditionNode = createNode("LoopCondition");
                    addEdge(loopNode, conditionNode);
                    i++;
                    while (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                        addEdge(conditionNode, createNode("Expr\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
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

            // === INPUT ===
            if (lexeme.equals("Input::get")) {
                String inputNode = createNode("InputStatement");
                addEdge(parentNode, inputNode);

                // Process Input::get components
                if (i + 1 < tokens.size() && tokens.get(i + 1).getLexeme().equals("(")) {
                    i += 2; // Skip Input::get and (
                    String promptNode = createNode("PromptString");
                    addEdge(inputNode, promptNode);

                    if (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                        addEdge(promptNode, createNode("String\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
                        i++;
                    }
                    i++; // Skip )
                }
                continue;
            }

            // === OUTPUT ===
            if (lexeme.equals("Output::print")) {
                String outputNode = createNode("OutputStatement");
                addEdge(parentNode, outputNode);
                if (i + 2 < tokens.size() && tokens.get(i + 1).getLexeme().equals("(")) {
                    i += 2; // Skip Output::print and (
                    String contentNode = createNode("Content");
                    addEdge(outputNode, contentNode);

                    while (i < tokens.size() && !tokens.get(i).getLexeme().equals(")")) {
                        addEdge(contentNode, createNode("Expr\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
                        i++;
                    }
                    i++; // Skip )
                }
                continue;
            }

            // === EXPRESSIONS ===
            if (lexeme.matches("[a-zA-Z_][a-zA-Z0-9_]*") && i + 1 < tokens.size() && tokens.get(i + 1).getLexeme().matches("[=+\\-*/]") ) {
                String exprNode = createNode("Expression");
                addEdge(parentNode, exprNode);
                addEdge(exprNode, createNode("Variable\n'" + escapeString(lexeme) + "'"));
                addEdge(exprNode, createNode("Operator\n'" + escapeString(tokens.get(i + 1).getLexeme()) + "'"));

                i += 2;
                if (i < tokens.size()) {
                    addEdge(exprNode, createNode("Value\n'" + escapeString(tokens.get(i).getLexeme()) + "'"));
                    i++;
                }
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

    private boolean isDataType(String lexeme) {
        return lexeme.matches("int|char|bool|str|float|double|long|byte|Date|Frac|Complex");
    }
}