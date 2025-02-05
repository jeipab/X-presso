package parser.grammar;

public enum NonTerminal {
    // Core Program Structures
    SP_PROG,
    CLASS,
    CLASS_MODS,
    ACCESS_MOD,
    NON_ACCESS_MOD,
    INHERIT,
    CLASS_BODY,
    SP_MAIN,
    SP_METHOD,

    // Declarations
    FIELD,
    FIELD_INIT,
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
    CONDITION,

    // Identifiers & Operators
    IDENTIFIER,
    OPERATOR,
    ASSIGN_OP,
    ARITHMETIC_OP,
    LOG_OP,
    REL_OP,

    // Literals
    LITERAL,
    INT_LIT,
    FLOAT_LIT,
    STR_LIT,
    CHAR_LIT,
    BOOL_LIT,
    DATE_LIT,
    FRAC_LIT,
    COMP_LIT,

    // Control Structures
    IF_STMT,
    ELSE_STMT,
    SWITCH_STMT,
    CASES,

    // Loops
    FOR_LOOP,
    WHILE_LOOP,
    DO_WHILE_LOOP
}
