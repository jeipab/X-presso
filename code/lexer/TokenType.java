package lexer;

/**
 * Enum representing the various types of tokens recognized by the Lexer.
 */
public enum TokenType {
     // Identifiers
     IDENTIFIER,

     // Keywords and Reserved Words
     KEYWORD,
     RESERVED_WORD,

     // Operators
     ARITHMETIC_OP,
     ASSIGNMENT_OP,
     RELATIONAL_OP,
     LOGICAL_OP,
     UNARY_OP,
     TERNARY_OP,
     BITWISE_OP,
     METHOD_OP,
     LOOP_OP,

     // Literals
     INT_LIT,
     FLOAT_LIT,
     STR_LIT,
     BOOL_LIT,
     CHAR_LIT,
     NULL_LIT,
     FRAC_LIT,
     DATE_LIT,
     COMP_LIT,

     // Delimiters
     DELIMITER,
     PUNC_DELIM,
     STRING_DELIM,
     OBJECT_DELIM,

     // Comments
     COMMENT,

     // Whitespace
     WHITESPACE,

     // Escape Characters
     ESCAPE_CHAR,

     // End of File
     EOF
}
