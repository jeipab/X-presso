package parser.core;

import lexer.Token;
import lexer.TokenType;
import parser.grammar.NonTerminal;
import parser.grammar.GrammarRule;
import parser.symbol.SymbolTable;
import util.SyntaxErrorHandler;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private final ParserAutomaton automaton; // ✅ Add the automaton
    private final SyntaxErrorHandler errorHandler;
    private final Stack<Object> parsingStack;
    private final Map<NonTerminal, Map<TokenType, List<Object>>> parsingTable;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.symbolTable = new SymbolTable();
        this.automaton = new ParserAutomaton(); // ✅ Initialize automaton
        this.errorHandler = new SyntaxErrorHandler(this);
        this.parsingStack = new Stack<>();
        this.parsingTable = buildParsingTable();

        // Push starting symbol
        parsingStack.push(NonTerminal.SP_PROG);
    }

    public ParseTree parse() {
        ParseTree parseTree = new ParseTree(NonTerminal.SP_PROG);
        ParseTreeNode rootNode = parseTree.getRoot();

        while (!parsingStack.isEmpty()) {
            Object top = parsingStack.pop();
            Token token = peek();

            if (top instanceof String) {
                if (token.getLexeme().equals(top)) {
                    automaton.transition(token); // ✅ Transition state in the automaton
                    advance();
                } else {
                    errorHandler.reportError(
                        SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                        "Expected '" + top + "' but found '" + token.getLexeme() + "'",
                        token.getLine(),
                        token.getColumn(),
                        "Check the syntax structure."
                    );
                }
            } else if (top instanceof NonTerminal) {
                NonTerminal nonTerminal = (NonTerminal) top;

                // ✅ Ensure token is valid for this state using the automaton
                if (!automaton.processToken(token)) {
                    errorHandler.reportError(
                        SyntaxErrorHandler.ErrorType.UNEXPECTED_TOKEN,
                        "Unexpected token '" + token.getLexeme() + "' in " + nonTerminal,
                        token.getLine(),
                        token.getColumn(),
                        "Check syntax structure."
                    );
                    continue;
                }

                List<Object> production = getProduction(nonTerminal, token);

                if (production != null) {
                    pushProduction(production);
                } else {
                    errorHandler.reportError(
                        SyntaxErrorHandler.ErrorType.INVALID_SYNTAX_STRUCTURE,
                        "Unexpected token '" + token.getLexeme() + "' in " + nonTerminal,
                        token.getLine(),
                        token.getColumn(),
                        "Check for missing or misplaced tokens."
                    );
                }
            }
        }

        return parseTree;
    }

    private Map<NonTerminal, Map<TokenType, List<Object>>> buildParsingTable() {
        Map<NonTerminal, Map<TokenType, List<Object>>> table = new HashMap<>();
        
        for (NonTerminal nonTerminal : NonTerminal.values()) {
            List<List<Object>> productions = GrammarRule.getProductions(nonTerminal);
            Map<TokenType, List<Object>> tokenRules = new HashMap<>();
            
            for (List<Object> production : productions) {
                if (!production.isEmpty()) {
                    Object first = production.get(0);
                    if (first instanceof String) {
                        tokenRules.put(TokenType.IDENTIFIER, production); 
                    } else if (first instanceof NonTerminal) {
                        Set<TokenType> firstSet = ((NonTerminal) first).getFirst();
                        for (TokenType tokenType : firstSet) {
                            tokenRules.put(tokenType, production);
                        }
                    }
                }
            }
            table.put(nonTerminal, tokenRules);
        }
        return table;
    }

    private List<Object> getProduction(NonTerminal nonTerminal, Token token) {
        Map<TokenType, List<Object>> rules = parsingTable.get(nonTerminal);
        return rules != null ? rules.get(token.getType()) : null;
    }

    private void pushProduction(List<Object> production) {
        for (int i = production.size() - 1; i >= 0; i--) {
            parsingStack.push(production.get(i));
        }
    }

    private Token peek() {
        return current < tokens.size() ? tokens.get(current) : null;
    }

    private Token advance() {
        return current < tokens.size() ? tokens.get(current++) : null;
    }
}
