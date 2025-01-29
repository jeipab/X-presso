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
            List.of(NonTerminal.CLASS, NonTerminal.SP_PROG) // Allow multiple classes
        ));
        
        // Comprehensive class declaration rules
        rules.put(NonTerminal.CLASS, List.of(
            // Full structure with optional components
            
            List.of(NonTerminal.CLASS_MODS, "class", NonTerminal.IDENTIFIER, NonTerminal.CLASS_INHERIT, NonTerminal.INTERFACE_INHERIT, "{", NonTerminal.CLASS_BODY, "}"),

            // Without interface inheritance
            List.of(NonTerminal.CLASS_MODS, "class", NonTerminal.IDENTIFIER, NonTerminal.CLASS_INHERIT, "{", NonTerminal.CLASS_BODY, "}"),

            // Without class inheritance
            List.of(NonTerminal.CLASS_MODS, "class", NonTerminal.IDENTIFIER, NonTerminal.INTERFACE_INHERIT, "{", NonTerminal.CLASS_BODY, "}"),

            // Without any inheritance
            List.of(NonTerminal.CLASS_MODS, "class", NonTerminal.IDENTIFIER, "{", NonTerminal.CLASS_BODY, "}"),

            // Without modifiers but with both inheritances
            List.of("class", NonTerminal.IDENTIFIER, NonTerminal.CLASS_INHERIT, NonTerminal.INTERFACE_INHERIT, "{", NonTerminal.CLASS_BODY, "}"),

            // Without modifiers, with class inheritance only
            List.of("class", NonTerminal.IDENTIFIER, NonTerminal.CLASS_INHERIT, "{", NonTerminal.CLASS_BODY, "}"),

            // Without modifiers, with interface inheritance only
            List.of("class", NonTerminal.IDENTIFIER, NonTerminal.INTERFACE_INHERIT, "{", NonTerminal.CLASS_BODY, "}"),

            // Minimal class declaration
            List.of("class", NonTerminal.IDENTIFIER, "{", NonTerminal.CLASS_BODY, "}"),

            // Empty class declaration
            List.of("class", NonTerminal.IDENTIFIER, "{", "}"),

            // With modifiers, empty class
            List.of(NonTerminal.CLASS_MODS, "class", NonTerminal.IDENTIFIER, "{", "}"),

            // Nested class declarations (for future extensibility)
            List.of(NonTerminal.CLASS, NonTerminal.CLASS)
        ));

        // Class Modifiers
        rules.put(NonTerminal.CLASS_MODS, List.of(
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.CLASS_MODS), // Multiple access modifiers
            List.of(NonTerminal.NON_ACCESS_MOD, NonTerminal.CLASS_MODS), // Multiple non-access modifiers
            List.of(NonTerminal.ACCESS_MOD), // Single access modifier
            List.of(NonTerminal.NON_ACCESS_MOD), // Single non-access modifier
            List.of() // Empty case (no modifiers)
        ));

        rules.put(NonTerminal.ACCESS_MOD, List.of(
            List.of("public"),
            List.of("private"),
            List.of("protected"),
            List.of("general")
        ));

        rules.put(NonTerminal.NON_ACCESS_MOD, List.of(
            List.of("static"),
            List.of("final"),
            List.of("abstract"),
            List.of("native"),
            List.of("strictfp"),
            List.of("transient"),
            List.of("volatile")
        ));

        rules.put(NonTerminal.CLASS_INHERIT, List.of(
            List.of(":>", NonTerminal.IDENTIFIER, NonTerminal.CLASS_INHERIT), // Recursive multiple inheritance
            List.of(":>", NonTerminal.IDENTIFIER), // Single inheritance
            List.of() // No inheritance
        ));

        rules.put(NonTerminal.INTERFACE_INHERIT, List.of(
            List.of(":>>", NonTerminal.IDENTIFIER, ",", NonTerminal.INTERFACE_INHERIT), //multiple interface inheritance
            List.of(":>>", NonTerminal.IDENTIFIER), //single interface inheritance
            List.of() //Empty case (no interface to implement)
        ));

        rules.put(NonTerminal.CLASS_BODY, List.of(
            List.of(NonTerminal.FIELD, NonTerminal.CLASS_BODY),  // Multiple fields
            List.of(NonTerminal.SP_MAIN, NonTerminal.CLASS_BODY), // Main method
            List.of(NonTerminal.SP_METHOD, NonTerminal.CLASS_BODY), // Custom methods
            List.of(NonTerminal.FIELD),  // Single field declaration
            List.of(NonTerminal.SP_MAIN), // Main method only
            List.of(NonTerminal.SP_METHOD), // Custom method only
            List.of()  // Empty class body
        ));

        rules.put(NonTerminal.SP_MAIN, List.of(
            List.of("public", "static", "void", "main", "(", "str", "[", "]", "args", ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of("main", "(", "args", ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of(NonTerminal.DEC_STATE, ";")

        ));

        // Method Declarations
        rules.put(NonTerminal.SP_METHOD, List.of(
            // Method with access and non-access modifiers
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(",  NonTerminal.PARAMETERS, ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(", ")", "{", NonTerminal.STATEMENTS, "}"),

            // Method with only access modifier
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(",  NonTerminal.PARAMETERS, ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(", ")", "{", NonTerminal.STATEMENTS, "}"),

            // Method with only non-access modifier
            List.of(NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(",  NonTerminal.PARAMETERS, ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of(NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(", ")", "{", NonTerminal.STATEMENTS, "}"),

            // Method with no modifiers
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(",  NonTerminal.PARAMETERS, ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, "(", ")", "{", NonTerminal.STATEMENTS, "}")
        ));

        // Field Declarations
        rules.put(NonTerminal.FIELD, List.of(
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, NonTerminal.FIELD_INIT, ";"), // With access & non-access modifiers
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, NonTerminal.FIELD_INIT, ";"), // With access modifier
            List.of(NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, NonTerminal.FIELD_INIT, ";"), // With non-access modifier
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, NonTerminal.FIELD_INIT, ";"), // Just data type and assignment

            // Same patterns without initialization
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, ";"),
            List.of(NonTerminal.ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, ";"),
            List.of(NonTerminal.NON_ACCESS_MOD, NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, ";"),
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER, ";")
        ));

        // Field Initialization Rules
        rules.put(NonTerminal.FIELD_INIT, List.of(
            List.of("=", NonTerminal.EXPR), // Field with assignment
            List.of() // Field without assignment
        ));

        rules.put(NonTerminal.DATA_TYPE, List.of(
            List.of("int"),
            List.of("char"),
            List.of("bool"),
            List.of("str"),
            List.of("float"),
            List.of("double"),
            List.of("long"),
            List.of("byte"),
            List.of("Date"),
            List.of("Frac"),
            List.of("Complex")
        ));

        rules.put(NonTerminal.PARAMETERS, List.of(
            List.of(NonTerminal.PARAMETER),
            List.of(NonTerminal.PARAMETER, ",", NonTerminal.PARAMETERS)
        ));

        rules.put(NonTerminal.PARAMETER, List.of(
            List.of(NonTerminal.DATA_TYPE, NonTerminal.IDENTIFIER)
        ));

        rules.put(NonTerminal.STATEMENTS, List.of(
            List.of(NonTerminal.DEC_STATE),
            List.of(NonTerminal.IN_STATE),
            List.of(NonTerminal.OUT_STATE),
            List.of(NonTerminal.ASS_STATE),
            List.of(NonTerminal.CON_STATE),
            List.of(NonTerminal.ITER_STATE)
        ));

        rules.put(NonTerminal.IDENTIFIER, List.of(
            List.of(NonTerminal.START_CHAR, NonTerminal.REST_CHARS)
        ));

        rules.put(NonTerminal.START_CHAR, List.of(
            List.of(NonTerminal.LETTER),
            List.of("_")
        ));

        rules.put(NonTerminal.REST_CHARS, List.of(
            List.of(""),
            List.of(NonTerminal.VALID_CHAR, NonTerminal.REST_CHARS)
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
            List.of("A"), List.of("B"), List.of("C"), List.of("D"), List.of("E"), List.of("F"), List.of("G"),
            List.of("H"), List.of("I"), List.of("J"), List.of("K"), List.of("L"), List.of("M"), List.of("N"),
            List.of("O"), List.of("P"), List.of("Q"), List.of("R"), List.of("S"), List.of("T"), List.of("U"),
            List.of("V"), List.of("W"), List.of("X"), List.of("Y"), List.of("Z")
        ));
        rules.put(NonTerminal.LOWERCASE, List.of(
            List.of("a"), List.of("b"), List.of("c"), List.of("d"), List.of("e"), List.of("f"), List.of("g"),
            List.of("h"), List.of("i"), List.of("j"), List.of("k"), List.of("l"), List.of("m"), List.of("n"),
            List.of("o"), List.of("p"), List.of("q"), List.of("r"), List.of("s"), List.of("t"), List.of("u"),
            List.of("v"), List.of("w"), List.of("x"), List.of("y"), List.of("z")
        ));

        rules.put(NonTerminal.DIGIT, List.of(
            List.of("0"),
            List.of(NonTerminal.NON_ZERO)
        ));

        rules.put(NonTerminal.NON_ZERO, List.of(
            List.of("1"), List.of("2"), List.of("3"), List.of("4"), List.of("5"), List.of("6"), List.of("7"),
            List.of("8"), List.of("9")
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
            List.of("="),
            List.of("+="),
            List.of("-="),
            List.of("*="),
            List.of("/="),
            List.of("%="),
            List.of("?=")
        ));

        rules.put(NonTerminal.ARITHMETIC_OP, List.of(
            List.of("+"),
            List.of("-"),
            List.of("*"),
            List.of("/"),
            List.of("%"),
            List.of("^")
        ));

        rules.put(NonTerminal.LOG_OP, List.of(
            List.of("||"),
            List.of("&&"),
            List.of("!")
        ));

        rules.put(NonTerminal.REL_OP, List.of(
            List.of("=="),
            List.of("!="),
            List.of(">"),
            List.of("<"),
            List.of(">="),
            List.of("<=")
        ));

        rules.put(NonTerminal.BIT_OP, List.of(
            List.of("&"),
            List.of("|"),
            List.of("^"),
            List.of("~"),
            List.of("<<"),
            List.of(">>"),
            List.of(">>>")
        ));

        rules.put(NonTerminal.UNARY_OP, List.of(
            List.of(NonTerminal.PREFIX_OP, NonTerminal.OPERAND),
            List.of(NonTerminal.OPERAND, NonTerminal.POSTFIX_OP)
        ));

        rules.put(NonTerminal.TERNARY_OP, List.of(
            List.of(NonTerminal.CONDITION, "?", NonTerminal.EXPR, ":", NonTerminal.EXPR)
        ));

        rules.put(NonTerminal.METHOD_OP, List.of(
            List.of("."),
            List.of("::"),
            List.of("->")
        ));

        // Reusing previous rules for EXPR, OPERAND, etc.
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
            List.of("+"),
            List.of("-"),
            List.of("++"),
            List.of("--"),
            List.of("**"),
            List.of("!")
        ));

        rules.put(NonTerminal.POSTFIX_OP, List.of(
            List.of("++"),
            List.of("--"),
            List.of("**")
        ));

        rules.put(NonTerminal.CONDITION, List.of(
            List.of(NonTerminal.EXPR)
        ));


        // Spec_Word Production Rules
        rules.put(NonTerminal.SPEC_WORD, List.of(
            List.of(NonTerminal.RESERVED),
            List.of(NonTerminal.KEYWORD)
        ));

        // Keyword Production Rule
        rules.put(NonTerminal.KEYWORD, List.of(
            List.of("break"),
            List.of("case"),
            List.of("day"),
            List.of("default"),
            List.of("do"),
            List.of("else"),
            List.of("exit"),
            List.of("exit when"),
            List.of("for"),
            List.of("get"),
            List.of("if"),
            List.of("in"),
            List.of("Input"),
            List.of("month"),
            List.of("Output"),
            List.of("print"),
            List.of("switch"),
            List.of("switch-fall"),
            List.of("while"),
            List.of("where type"),
            List.of("year")
        ));

        // Reserved Words Production Rule
        rules.put(NonTerminal.RESERVED, List.of(
            List.of("abstract"),
            List.of("after"),
            List.of("ALIAS"),
            List.of("before"),
            List.of("bool"),
            List.of("byte"),
            List.of("char"),
            List.of("class"),
            List.of("Complex"),
            List.of("Date"),
            List.of("double"),
            List.of("exclude"),
            List.of("export_as"),
            List.of("Frac"),
            List.of("filter_by"),
            List.of("final"),
            List.of("float"),
            List.of("inline_query"),
            List.of("inspect"),
            List.of("int"),
            List.of("long"),
            List.of("main"),
            List.of("modify"),
            List.of("native"),
            List.of("private"),
            List.of("protected"),
            List.of("public"),
            List.of("short"),
            List.of("static"),
            List.of("STRICT"),
            List.of("strictfp"),
            List.of("str"),
            List.of("today"),
            List.of("toMixed"),
            List.of("transient"),
            List.of("validate"),
            List.of("volatile")
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
            List.of("Output", "::", "print", "(", "\"", NonTerminal.STR, "\"", "+", NonTerminal.DATA_TYPE, ")", ";")
        ));

        rules.put(NonTerminal.STR, List.of(
            List.of(NonTerminal.CHARACTER, NonTerminal.STR),
            List.of() 
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
            List.of("if", "(", NonTerminal.EXPR, ")", NonTerminal.STATEMENTS),
            List.of("if", "(", NonTerminal.EXPR, ")", "{", NonTerminal.STATEMENTS, "}"),
            List.of("if", "(", NonTerminal.EXPR, ")", "{", NonTerminal.STATEMENTS, "}", NonTerminal.ELSE_STMT)
        ));

        rules.put(NonTerminal.ELSE_STMT, List.of(
            List.of("else", "{", NonTerminal.STATEMENTS, "}"),
            List.of("else", "if", "(", NonTerminal.EXPR, ")", "{", NonTerminal.STATEMENTS, "}", NonTerminal.ELSE_STMT)
        ));

        rules.put(NonTerminal.SWITCH_STMT, List.of(
            List.of("switch", "(", NonTerminal.IDENTIFIER, ")", "{", NonTerminal.CASES, "}"),
            List.of("switch-fall", "(", NonTerminal.IDENTIFIER, ")", "{", NonTerminal.CASES, "}")
        ));

        rules.put(NonTerminal.CASES, List.of(
            List.of("case", NonTerminal.STR_LIT, ":", "{", NonTerminal.STATEMENTS, "}", NonTerminal.CASES),
            List.of("case", NonTerminal.INT_LIT, ":", "{", NonTerminal.STATEMENTS, "}", NonTerminal.CASES),
            List.of("case", NonTerminal.CHAR_LIT, ":", "{", NonTerminal.STATEMENTS, "}", NonTerminal.CASES),
            List.of("case", "[", NonTerminal.FLOAT_LIT, "]", ":", "{", NonTerminal.STATEMENTS, "}", NonTerminal.CASES),
            List.of("default", ":", "{", NonTerminal.STATEMENTS, "}")
        ));

        rules.put(NonTerminal.ASS_STATE, List.of(
            List.of(NonTerminal.IDENTIFIER, NonTerminal.ASSIGN_OP, NonTerminal.LITERAL),
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
            List.of(NonTerminal.NON_ZERO, NonTerminal.DIGIT)
        ));

        // Floating Point Literal (Float_Lit)
        rules.put(NonTerminal.FLOAT_LIT, List.of(
            List.of(NonTerminal.INT_LIT, ".", NonTerminal.DIGIT, NonTerminal.DIGIT)
        ));

        // String Literal (Str_Lit)
        rules.put(NonTerminal.STR_LIT, List.of(
            List.of("\"", NonTerminal.CHARACTER, "\"")
        ));

        // Char Literal (Char_Lit)
        rules.put(NonTerminal.CHAR_LIT, List.of(
            List.of("'", NonTerminal.CHARACTER, "'")
        ));

        // Boolean Literal (Bool_Lit)
        rules.put(NonTerminal.BOOL_LIT, List.of(
            List.of("true"),
            List.of("false")
        ));

        // Date Literal (Date_Lit)
        rules.put(NonTerminal.DATE_LIT, List.of(
            List.of("[", NonTerminal.YEAR, "|", NonTerminal.MONTH, "|", NonTerminal.DAY, "]")
        ));

        // Fraction Literal (Frac_Lit)
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
            // Standard while-loop: while (condition) { statements }
            List.of("while", "(", NonTerminal.CONDITION, ")", "{", NonTerminal.STATEMENTS, "}"),

            // While-loop with `exit-when`: while (condition) exit-when (condition) { statements }
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
            List.of("inspect", "{", NonTerminal.STATEMENTS, "*", "}")
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
            List.of("inline_query", "{", NonTerminal.QUERY_STATEMENT, "*", "}")
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
            List.of(NonTerminal.IDENTIFIER, "->", "{", NonTerminal.STATEMENTS, "*", "}")
        ));

        // Year, Month, and Day Production Rules
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
}