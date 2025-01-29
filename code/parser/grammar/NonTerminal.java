package parser.grammar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lexer.TokenType;

public enum NonTerminal {
    // Core Program Structures
    SP_PROG,
    CLASS,
    ACCESS_MOD,
    NON_ACCESS_MOD,
    INHERIT,
    CLASS_INHERIT,
    INTERFACE_INHERIT,
    CLASS_BODY,
    SP_MAIN,
    SP_METHOD,

    // Declarations
    FIELD,
    DATA_TYPE,
    PARAMETERS,
    PARAMETER,

    // Statements & Expressions
    STATEMENTS,
    STATEMENT,
    FUNCTION,
    EXPR,
    UNARY_EXPR,
    BINARY_EXPR,
    TERNARY_EXPR,
    CONDITION,

    // Identifiers & Characters
    IDENTIFIER,
    START_CHAR,
    REST_CHARS,
    VALID_CHAR,
    LETTER,
    UPPERCASE,
    LOWERCASE,
    DIGIT,
    NON_ZERO,

    // Operators
    OPERATOR,
    ASSIGN_OP,
    ARITHMETIC_OP,
    LOG_OP,
    REL_OP,
    BIT_OP,
    PREFIX_OP,
    POSTFIX_OP,
    UNARY_OP,
    BINARY_OP,
    TERNARY_OP,
    METHOD_OP,

    // Operands & Literals
    OPERAND,
    LITERAL,
    NUMBER,
    INT_LIT,
    FLOAT_LIT,
    STR_LIT,
    CHAR_LIT,
    BOOL_LIT,
    DATE_LIT,
    FRAC_LIT,
    COMP_LIT,

    // Date Components
    YEAR,
    MONTH,
    DAY,

    // Character Handling
    CHARACTER,
    SPECIAL_CHAR,

    // Special Words & Keywords
    SPEC_WORD,
    KEYWORD,
    RESERVED,

    // FUNCTION
    FILTER_EXPR,
    VALIDATE_EXPR,
    INSPECT_BLOCK,
    DATE_FUNC,
    DATE_OBJ,
    DATE_OP,
    MODIFY_BLOCK,
    QUERY_BLOCK,
    QUERY_STATEMENT,
    FROM_CLAUSE,
    FILTER_CLAUSE,
    SELECT_CLAUSE,
    EXPORT_EXPR,
    TOMIXED_EXPR,
    ALIAS_DEC,
    LAMBDA_EXPR,
    LAMBDA_BLOCK,

    // Declarations
    DEC_STATE,
    SINGLE_DEC,
    MULTI_DEC,
    MULTI_IDENTIFIER,

    // Input & Output
    IN_STATE,
    PROMPT_STRING,
    OUT_STATE,
    STR,

    // Control Statements
    CON_STATE,
    IF_STMT,
    ELSE_STMT,
    SWITCH_STMT,
    CASES,

    // Assignments & Iteration
    ASS_STATE,
    ITER_STATE,
    FOR_LOOP,
    WHILE_LOOP,
    DO_WHILE_LOOP,
    ENHANCED_FOR;




     private final Set<TokenType> firstSet = new HashSet<>();
     private final Set<TokenType> followSet = new HashSet<>();

     // **Method to define First Set**
     public void addFirst(TokenType... tokens) {
          firstSet.addAll(Arrays.asList(tokens));
     }

     // **Method to define Follow Set**
     public void addFollow(TokenType... tokens) {
          followSet.addAll(Arrays.asList(tokens));
     }

     // **Get FIRST Set**
     public Set<TokenType> getFirst() {
          return firstSet;
     }

     // **Get FOLLOW Set**
     public Set<TokenType> getFollow() {
          return followSet;
     }
}
