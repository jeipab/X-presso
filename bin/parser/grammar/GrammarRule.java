package parser.grammar;

import java.util.*;

import lexer.Token;
import lexer.TokenType;

public class GrammarRule {
    private static final Map<NonTerminal, List<List<Object>>> rules = new HashMap<>();

    static {
        // Base Source Code Production Rules
        rules.put(NonTerminal.SP_PROG, List.of(
            List.of(NonTerminal.CLASS), 
            List.of(NonTerminal.CLASS, NonTerminal.SP_PROG), // Allow multiple classes
            List.of(NonTerminal.STATEMENTS, NonTerminal.SP_PROG), // Allow statements without classes
            List.of(NonTerminal.EXPR, NonTerminal.SP_PROG), // Allow expressions without classes
            List.of() // Empty case
        ));
        
        // Class Declarations with Optional Elements
        rules.put(NonTerminal.CLASS, List.of(
            List.of(Optional.of(NonTerminal.CLASS_MODS), "class", NonTerminal.IDENTIFIER, Optional.of(NonTerminal.CLASS_INHERIT), Optional.of(NonTerminal.INTERFACE_INHERIT), "{", Optional.of(NonTerminal.CLASS_BODY), "}"),
            List.of("class", NonTerminal.IDENTIFIER, "{", Optional.of(NonTerminal.CLASS_BODY), "}")
        ));

        // Class Modifiers
        rules.put(NonTerminal.CLASS_MODS, List.of(
            List.of(NonTerminal.ACCESS_MOD),
            List.of(NonTerminal.NON_ACCESS_MOD),
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.CLASS_MODS),
            List.of(NonTerminal.NON_ACCESS_MOD, NonTerminal.CLASS_MODS),
            List.of()  // Empty production
        ));


        rules.put(NonTerminal.ACCESS_MOD, List.of(List.of("public"), List.of("private"), List.of("protected"), List.of("general")));
        rules.put(NonTerminal.NON_ACCESS_MOD, List.of(List.of("static"), List.of("final"), List.of("abstract"), List.of("native"), List.of("strictfp"), List.of("transient"), List.of("volatile")));

        rules.put(NonTerminal.CLASS_INHERIT, List.of(
            List.of(":>", NonTerminal.IDENTIFIER, Optional.of(NonTerminal.CLASS_INHERIT)),
            List.of()
        ));

        rules.put(NonTerminal.INTERFACE_INHERIT, List.of(
            List.of(":>>", NonTerminal.IDENTIFIER, Optional.of(List.of(",", NonTerminal.INTERFACE_INHERIT))),
            List.of()
        ));

        rules.put(NonTerminal.CLASS_BODY, List.of(
            List.of(Optional.of(NonTerminal.FIELD), Optional.of(NonTerminal.CLASS_BODY)),
            List.of(Optional.of(NonTerminal.SP_MAIN), Optional.of(NonTerminal.CLASS_BODY)),
            List.of(Optional.of(NonTerminal.SP_METHOD), Optional.of(NonTerminal.CLASS_BODY)),
            List.of()
        ));

        rules.put(NonTerminal.SP_MAIN, List.of(
            List.of("public", "static", "void", "main", "(", "str", "[", "]", "args", ")", "{", Optional.of(NonTerminal.STATEMENTS), "}"),
            List.of("main", "(", "args", ")", "{", Optional.of(NonTerminal.STATEMENTS), "}")
        ));

        // Method Declarations
        rules.put(NonTerminal.SP_METHOD, List.of(
            List.of(Optional.of(NonTerminal.ACCESS_MOD), Optional.of(NonTerminal.NON_ACCESS_MOD), NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(", Optional.of(NonTerminal.PARAMETERS), ")", "{", Optional.of(NonTerminal.STATEMENTS), "}")
        ));

        // Field Declarations
        rules.put(NonTerminal.FIELD, List.of(
            List.of(Optional.of(NonTerminal.ACCESS_MOD), Optional.of(NonTerminal.NON_ACCESS_MOD), NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, Optional.of(NonTerminal.FIELD_INIT), ";")
        ));

        // Field Initialization Rules
        rules.put(NonTerminal.FIELD_INIT, List.of(
            List.of("=", NonTerminal.EXPR),
            List.of()
        ));

        rules.put(NonTerminal.DATA_TYPE, List.of(
            List.of("int"), List.of("char"), List.of("bool"), List.of("str"), List.of("float"), List.of("double"),
            List.of("long"), List.of("byte"), List.of("Date"), List.of("Frac"), List.of("Complex")
        ));

        // Paramers Production Rules
        rules.put(NonTerminal.PARAMETERS, List.of(
            List.of(NonTerminal.PARAMETER),
            List.of(NonTerminal.PARAMETER, ",", NonTerminal.PARAMETERS)
        ));

        rules.put(NonTerminal.PARAMETER, List.of(
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER)
        ));

        // Statements Production Rules
        rules.put(NonTerminal.STATEMENTS, List.of(
            List.of(Optional.of(NonTerminal.DEC_STATE)),
            List.of(Optional.of(NonTerminal.IN_STATE)),
            List.of(Optional.of(NonTerminal.OUT_STATE)),
            List.of(Optional.of(NonTerminal.ASS_STATE)),
            List.of(Optional.of(NonTerminal.CON_STATE)),
            List.of(Optional.of(NonTerminal.ITER_STATE))
        ));

        // Identifier Production Rules
        rules.put(NonTerminal.IDENTIFIER, List.of(
            List.of(NonTerminal.START_CHAR, Optional.of(NonTerminal.REST_CHARS))
        ));

        rules.put(NonTerminal.START_CHAR, List.of(
            List.of(NonTerminal.LETTER),
            List.of("_")
        ));

        rules.put(NonTerminal.REST_CHARS, List.of(
            List.of(),
            List.of(NonTerminal.VALID_CHAR, Optional.of(NonTerminal.REST_CHARS))
        ));

        rules.put(NonTerminal.VALID_CHAR, List.of(
            List.of(NonTerminal.LETTER),
            List.of(NonTerminal.DIGIT),
            List.of("_")
        ));

        rules.put(NonTerminal.LETTER, List.of(
            List.of(NonTerminal.UPPERCASE),
            List.of(NonTerminal.LOWERCASE)
        ));


        rules.put(NonTerminal.UPPERCASE, List.of(
            List.of((Object[]) "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""))
        ));
        rules.put(NonTerminal.LOWERCASE, List.of(
            List.of((Object[]) "abcdefghijklmnopqrstuvwxyz".split(""))
        ));

        // Number Production Rules
        rules.put(NonTerminal.DIGIT, List.of(
            List.of("0"),
            List.of(NonTerminal.NON_ZERO)
        ));

        rules.put(NonTerminal.NON_ZERO, List.of(
            List.of((Object[]) "123456789".split(""))
        ));

        //Operators
        rules.put(NonTerminal.OPERATOR, List.of(
            List.of(NonTerminal.ASSIGN_OP),
            List.of(NonTerminal.ARITHMETIC_OP),
            List.of(NonTerminal.LOG_OP),
            List.of(NonTerminal.REL_OP),
            List.of(NonTerminal.BIT_OP),
            List.of(NonTerminal.UNARY_OP),
            List.of(NonTerminal.TERNARY_OP),
            List.of(NonTerminal.METHOD_OP),
            List.of(NonTerminal.BINARY_OP)
        ));

        rules.put(NonTerminal.BINARY_OP, List.of(
            List.of(NonTerminal.ARITHMETIC_OP),
            List.of(NonTerminal.LOG_OP),
            List.of(NonTerminal.REL_OP),
            List.of(NonTerminal.BIT_OP),
            List.of(NonTerminal.METHOD_OP)
        ));

        rules.put(NonTerminal.ASSIGN_OP, List.of(
            List.of("=", "+=", "-=", "*=", "/=", "%=", "?=")
        ));

        rules.put(NonTerminal.ARITHMETIC_OP, List.of(
            List.of("+", "-", "*", "/", "%", "^")
        ));

        rules.put(NonTerminal.LOG_OP, List.of(
            List.of("||", "&&", "!")
        ));

        rules.put(NonTerminal.REL_OP, List.of(
            List.of("==", "!=", ">", "<", ">=", "<=")
        ));

        rules.put(NonTerminal.BIT_OP, List.of(
            List.of("&", "^|", "^", "~", "<<", ">>", ">>>")
        ));

        rules.put(NonTerminal.UNARY_OP, List.of(
            List.of(NonTerminal.PREFIX_OP, NonTerminal.OPERAND),
            List.of(NonTerminal.OPERAND, NonTerminal.POSTFIX_OP)
        ));

        rules.put(NonTerminal.TERNARY_OP, List.of(
            List.of(NonTerminal.CONDITION, "?", NonTerminal.EXPR, ":", NonTerminal.EXPR)
        ));

        rules.put(NonTerminal.METHOD_OP, List.of(
            List.of(".", "::", "->")
        ));

        // Expression Production Rules
        rules.put(NonTerminal.EXPR, List.of(
            List.of(NonTerminal.OPERAND),
            List.of(NonTerminal.UNARY_EXPR),
            List.of(NonTerminal.BINARY_EXPR),
            List.of(NonTerminal.TERNARY_EXPR),
            List.of("(", NonTerminal.EXPR, ")")
        ));

        rules.put(NonTerminal.OPERAND, List.of(
            List.of(NonTerminal.IDENTIFIER),
            List.of(NonTerminal.LITERAL)
        ));

        rules.put(NonTerminal.PREFIX_OP, List.of(
            List.of("+", "-", "++", "--", "**", "!")
        ));

        rules.put(NonTerminal.POSTFIX_OP, List.of(
            List.of("++", "--", "**")
        ));

        rules.put(NonTerminal.CONDITION, List.of(
            List.of(NonTerminal.EXPR)
        ));

        // Special Word Production Rules
        rules.put(NonTerminal.SPEC_WORD, List.of(
            List.of(NonTerminal.RESERVED),
            List.of(NonTerminal.KEYWORD)
        ));

        // Keyword Production Rule
        rules.put(NonTerminal.KEYWORD, List.of(
            List.of("break", "case", "day", "default", "do", "else", "exit", "exit when", "for", "get", "if", "in", "Input", "month", "Output", "print", "switch", "switch-fall", "while", "where type", "year")
        ));

        // Reserved Words Production Rule
        rules.put(NonTerminal.RESERVED, List.of(
            List.of("abstract", "after", "ALIAS", "before", "bool", "byte", "char", "class", "Complex", "Date", "double", "exclude", "export_as", "Frac", "filter_by", "final", "float", "inline_query", "inspect", "int", "long", "main", "modify", "native", "private", "protected", "public", "short", "static", "STRICT", "strictfp", "str", "today", "toMixed", "transient", "validate", "volatile")
        ));

        // Declaration Statement Production Rules
        rules.put(NonTerminal.DEC_STATE, List.of(
            List.of(NonTerminal.SINGLE_DEC),
            List.of(NonTerminal.MULTI_DEC)
        ));

        rules.put(NonTerminal.SINGLE_DEC, List.of(
            List.of(NonTerminal.PARAMETER, ";"),
            List.of(NonTerminal.DATA_TYPE, NonTerminal.ASS_STATE, ";"),
            List.of(NonTerminal.DATA_TYPE, NonTerminal.ASS_STATE, NonTerminal.DEC_STATE)
        ));

        rules.put(NonTerminal.MULTI_DEC, List.of(
            List.of(NonTerminal.DATA_TYPE, NonTerminal.MULTI_IDENTIFIER)
        ));

        rules.put(NonTerminal.MULTI_IDENTIFIER, List.of(
            List.of(NonTerminal.IDENTIFIER),
            List.of(NonTerminal.ASS_STATE),
            List.of(NonTerminal.IDENTIFIER, ",", NonTerminal.MULTI_IDENTIFIER),
            List.of(NonTerminal.ASS_STATE, ",", NonTerminal.MULTI_IDENTIFIER)
        ));


        // Input Statement Production Rules
        rules.put(NonTerminal.IN_STATE, List.of(
            List.of(NonTerminal.IDENTIFIER, "=", "STRICT", "Input", "::", "get", "(", NonTerminal.PROMPT_STRING, ")", ";"),
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "=", "STRICT", "Input", "::", "get", "(", NonTerminal.PROMPT_STRING, ")", ";")
        ));

        rules.put(NonTerminal.PROMPT_STRING, List.of(
            List.of(NonTerminal.STR_LIT),
            List.of()  
        ));

        // Output Statement Production Rules
        rules.put(NonTerminal.OUT_STATE, List.of(
            List.of("Output", "::", "print", "(", NonTerminal.IDENTIFIER, ")", ";"),
            List.of("Output", "::", "print", "(", "\"", NonTerminal.STR_LIT, "\"", "+", NonTerminal.DATA_TYPE, ")", ";")
        ));

        rules.put(NonTerminal.CHARACTER, List.of(
            List.of(NonTerminal.LETTER),
            List.of(NonTerminal.DIGIT),
            List.of(NonTerminal.SPECIAL_CHAR),
            List.of()
        ));
        
        // Conditional Statement Production Rules
        rules.put(NonTerminal.CON_STATE, List.of(
            List.of(NonTerminal.IF_STMT),
            List.of(NonTerminal.SWITCH_STMT)
        ));

        rules.put(NonTerminal.IF_STMT, List.of(
            List.of("if", "(", NonTerminal.EXPR, ")", Optional.of("{"), NonTerminal.STATEMENTS, Optional.of("}"), Optional.of(NonTerminal.ELSE_STMT))
        ));

        rules.put(NonTerminal.ELSE_STMT, List.of(
            List.of("else", Optional.of("{"), NonTerminal.STATEMENTS, Optional.of("}")),
            List.of("else", "if", "(", NonTerminal.EXPR, ")", Optional.of("{"), NonTerminal.STATEMENTS, Optional.of("}"), Optional.of(NonTerminal.ELSE_STMT))
        ));

        rules.put(NonTerminal.SWITCH_STMT, List.of(
            List.of("switch", "(", NonTerminal.IDENTIFIER, ")", "{", NonTerminal.CASES, "}"),
            List.of("switch-fall", "(", NonTerminal.IDENTIFIER, ")", "{", NonTerminal.CASES, "}")
        ));

        rules.put(NonTerminal.CASES, List.of(
            List.of("case", NonTerminal.LITERAL, ":", "{", NonTerminal.STATEMENTS, "}", Optional.of(NonTerminal.CASES)),
            List.of("default", ":", "{", NonTerminal.STATEMENTS, "}")
        ));

        rules.put(NonTerminal.ASS_STATE, List.of(
            List.of(NonTerminal.IDENTIFIER, NonTerminal.ASSIGN_OP, NonTerminal.EXPR)
        ));

        rules.put(NonTerminal.LITERAL, List.of(
            List.of(NonTerminal.INT_LIT),
            List.of(NonTerminal.FLOAT_LIT),
            List.of(NonTerminal.STR_LIT),
            List.of(NonTerminal.CHAR_LIT),
            List.of(NonTerminal.BOOL_LIT),
            List.of(NonTerminal.DATE_LIT),
            List.of(NonTerminal.FRAC_LIT),
            List.of(NonTerminal.COMP_LIT)
        ));

        rules.put(NonTerminal.NUMBER, List.of(
            List.of(NonTerminal.INT_LIT),
            List.of(NonTerminal.FLOAT_LIT)
        ));

        rules.put(NonTerminal.INT_LIT, List.of(
            List.of("0"),
            List.of(NonTerminal.NON_ZERO, Optional.of(NonTerminal.DIGIT))
        ));

        rules.put(NonTerminal.FLOAT_LIT, List.of(
            List.of(NonTerminal.INT_LIT, ".", NonTerminal.DIGIT, Optional.of(NonTerminal.DIGIT))
        ));

        rules.put(NonTerminal.STR_LIT, List.of(
            List.of("\"", NonTerminal.CHARACTER, "\"")
        ));

        rules.put(NonTerminal.CHAR_LIT, List.of(
            List.of("'", NonTerminal.CHARACTER, "'")
        ));

        rules.put(NonTerminal.BOOL_LIT, List.of(
            List.of("true"),
            List.of("false")
        ));

        rules.put(NonTerminal.DATE_LIT, List.of(
            List.of("[", NonTerminal.YEAR, "|", NonTerminal.MONTH, "|", NonTerminal.DAY, "]")
        ));

        rules.put(NonTerminal.FRAC_LIT, List.of(
            List.of("[", NonTerminal.INT_LIT, "|", NonTerminal.INT_LIT, "]")
        ));

        // Iterative Statement Production Rules
        rules.put(NonTerminal.ITER_STATE, List.of(
            List.of(NonTerminal.FOR_LOOP),
            List.of(NonTerminal.WHILE_LOOP),
            List.of(NonTerminal.DO_WHILE_LOOP),
            List.of(NonTerminal.ENHANCED_FOR)
        ));

        // For Loop Production Rule
        rules.put(NonTerminal.FOR_LOOP, List.of(
            List.of("for", "(", NonTerminal.ASS_STATE, ";", NonTerminal.CONDITION, ";", NonTerminal.EXPR, ")", "{", NonTerminal.STATEMENTS, "}")
        ));

        rules.put(NonTerminal.WHILE_LOOP, List.of(
            List.of("while", "(", NonTerminal.CONDITION, ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of("while", "(", NonTerminal.CONDITION, ")", "exit-when", "(", NonTerminal.CONDITION, ")", "{", NonTerminal.STATEMENTS, "}")
        ));

        // Do-While Loop Production Rule
        rules.put(NonTerminal.DO_WHILE_LOOP, List.of(
            List.of("do", "{", NonTerminal.STATEMENTS, "}", "while", "(", NonTerminal.CONDITION, ")")
        ));

        // Enhanced For Loop Production Rule
        rules.put(NonTerminal.ENHANCED_FOR, List.of(
            List.of("do", "for", "(", NonTerminal.PARAMETER, ":", NonTerminal.OPERAND, ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of("do", "for", "(", NonTerminal.IDENTIFIER, "in", NonTerminal.IDENTIFIER, ")", "{", NonTerminal.STATEMENTS, "}", "while", NonTerminal.EXPR, "exit-when", NonTerminal.EXPR)
        ));

        rules.put(NonTerminal.FILTER_EXPR, List.of(
            List.of(NonTerminal.IDENTIFIER, ".", "filter_by", "(", NonTerminal.LAMBDA_EXPR, ")")
        ));

        rules.put(NonTerminal.VALIDATE_EXPR, List.of(
            List.of(NonTerminal.IDENTIFIER, ".", "validate", "(", NonTerminal.LAMBDA_EXPR, ")")
        ));

        rules.put(NonTerminal.INSPECT_BLOCK, List.of(
            List.of("inspect", "{", NonTerminal.STATEMENTS, "}")
        ));

        rules.put(NonTerminal.DATE_FUNC, List.of(
            List.of(NonTerminal.DATE_OBJ, ".", NonTerminal.DATE_OP, "(", NonTerminal.DATE_LIT, ")")
        ));

        rules.put(NonTerminal.DATE_OBJ, List.of(
            List.of(NonTerminal.IDENTIFIER),
            List.of("System")
        ));

        rules.put(NonTerminal.DATE_OP, List.of(
            List.of("before"),
            List.of("after"),
            List.of("year"),
            List.of("month"),
            List.of("day"),
            List.of("today")
        ));


        rules.put(NonTerminal.MODIFY_BLOCK, List.of(
            List.of(NonTerminal.IDENTIFIER, ".", "modify", "(", NonTerminal.LAMBDA_BLOCK, ")")
        ));

        rules.put(NonTerminal.QUERY_BLOCK, List.of(
            List.of("inline_query", "{", NonTerminal.QUERY_STATEMENT, "}")
        ));

        rules.put(NonTerminal.QUERY_STATEMENT, List.of(
            List.of(NonTerminal.FROM_CLAUSE),
            List.of(NonTerminal.FILTER_CLAUSE),
            List.of(NonTerminal.SELECT_CLAUSE)
        ));

        rules.put(NonTerminal.FROM_CLAUSE, List.of(
            List.of("from", NonTerminal.IDENTIFIER, ";")
        ));

        rules.put(NonTerminal.FILTER_CLAUSE, List.of(
            List.of("filter_by", "(", NonTerminal.LAMBDA_EXPR, ")", ";")
        ));

        rules.put(NonTerminal.SELECT_CLAUSE, List.of(
            List.of("select", "(", NonTerminal.LAMBDA_EXPR, ")", ";")
        ));

        rules.put(NonTerminal.EXPORT_EXPR, List.of(
            List.of(NonTerminal.IDENTIFIER, ".", "export_as", "(", NonTerminal.STR_LIT, ",", NonTerminal.STR_LIT, ")")
        ));

        rules.put(NonTerminal.TOMIXED_EXPR, List.of(
            List.of(NonTerminal.IDENTIFIER, ".", "toMixed", "(", ")")
        ));

        rules.put(NonTerminal.ALIAS_DEC, List.of(
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "=", "ALIAS", NonTerminal.IDENTIFIER)
        ));

        rules.put(NonTerminal.LAMBDA_EXPR, List.of(
            List.of(NonTerminal.IDENTIFIER, "->", NonTerminal.EXPR)
        ));

        rules.put(NonTerminal.LAMBDA_BLOCK, List.of(
            List.of(NonTerminal.IDENTIFIER, "->", "{", NonTerminal.STATEMENTS, "}")
        ));

        rules.put(NonTerminal.YEAR, List.of(
            List.of(NonTerminal.NON_ZERO, NonTerminal.DIGIT, NonTerminal.DIGIT, NonTerminal.DIGIT)
        ));

        rules.put(NonTerminal.MONTH, List.of(
            List.of("0", NonTerminal.NON_ZERO),
            List.of("1", "0"),
            List.of("1", "1"),
            List.of("1", "2")
        ));

        rules.put(NonTerminal.DAY, List.of(
            List.of("0", NonTerminal.NON_ZERO),
            List.of("1", NonTerminal.DIGIT),
            List.of("2", NonTerminal.DIGIT),
            List.of("3", "0"),
            List.of("3", "1")
        ));
    }


    public static List<List<Object>> getProductions(NonTerminal nonTerminal) {
        return rules.getOrDefault(nonTerminal, Collections.emptyList());
    }

    public static boolean isValidStart(NonTerminal nonTerminal, String token) {
        List<List<Object>> productions = rules.get(nonTerminal);
        if (productions == null) return false;
    
        for (List<Object> production : productions) {
            if (!production.isEmpty() && production.get(0).equals(token)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIdentifier(Token token) {
        return token.getType() == TokenType.IDENTIFIER;
    }

    public static boolean isLiteral(Token token) {
        return token.getType() == TokenType.INT_LIT ||
                token.getType() == TokenType.FLOAT_LIT ||
                token.getType() == TokenType.STR_LIT ||
                token.getType() == TokenType.CHAR_LIT ||
                token.getType() == TokenType.BOOL_LIT ||
                token.getType() == TokenType.DATE_LIT ||
                token.getType() == TokenType.FRAC_LIT ||
                token.getType() == TokenType.COMP_LIT;
    }

    public static boolean isOperatorAtPrecedence(Token token, int precedence) {
        if (token == null) return false;
        
        String lexeme = token.getLexeme();
        
        return switch (precedence) {
            case 1 -> lexeme.equals("(") || lexeme.equals(")") || lexeme.equals("[") || lexeme.equals("]") || lexeme.equals("{") || lexeme.equals("}");
            case 2 -> lexeme.equals(".") || lexeme.equals("::") || lexeme.equals("->");
            case 3 -> lexeme.equals("++") || lexeme.equals("--") || lexeme.equals("**");
            case 4 -> lexeme.equals("+") || lexeme.equals("-") || lexeme.equals("!") || lexeme.equals("~") || lexeme.equals("++") || lexeme.equals("--") || lexeme.equals("**");
            case 5 -> lexeme.equals("^");
            case 6 -> lexeme.equals("*") || lexeme.equals("/") || lexeme.equals("%");
            case 7 -> lexeme.equals("+") || lexeme.equals("-");
            case 8 -> lexeme.equals("<<") || lexeme.equals(">>") || lexeme.equals(">>>");
            case 9 -> lexeme.equals("&");
            case 10 -> lexeme.equals("^");
            case 11 -> lexeme.equals("|");
            case 12 -> lexeme.equals("<") || lexeme.equals("<=") || lexeme.equals(">") || lexeme.equals(">=");
            case 13 -> lexeme.equals("==") || lexeme.equals("!=");
            case 14 -> lexeme.equals("&&");
            case 15 -> lexeme.equals("||");
            case 16 -> lexeme.equals("?");
            case 17 -> lexeme.equals("=") || lexeme.equals("+=") || lexeme.equals("-=") || lexeme.equals("*=") || lexeme.equals("/=") || lexeme.equals("%=") || lexeme.equals("?=");
            default -> false;
        };
    }

    public static boolean couldGenerateToken(NonTerminal nonTerminal, Token token) {
        // Check if this non-terminal could eventually generate the given token
        if (nonTerminal == NonTerminal.CLASS_MODS) {
            // Check if token could be generated by ACCESS_MOD
            return couldGenerateToken(NonTerminal.ACCESS_MOD, token) ||
                   couldGenerateToken(NonTerminal.NON_ACCESS_MOD, token);
        }
        
        if (nonTerminal == NonTerminal.ACCESS_MOD) {
            return token.getLexeme().equals("public") || 
                    token.getLexeme().equals("private") || 
                    token.getLexeme().equals("protected") || 
                    token.getLexeme().equals("general");
        }
        
        if (nonTerminal == NonTerminal.NON_ACCESS_MOD) {
            return token.getLexeme().equals("static") || 
                    token.getLexeme().equals("final") || 
                    token.getLexeme().equals("abstract") ||
                    token.getLexeme().equals("native") ||
                    token.getLexeme().equals("strictfp") ||
                    token.getLexeme().equals("transient") ||
                    token.getLexeme().equals("volatile");
        }
        
        return false;
    }
}