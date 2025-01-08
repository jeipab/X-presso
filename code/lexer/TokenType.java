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
     ARITHMETIC_OPERATOR,
     ASSIGNMENT_OPERATOR,
     RELATIONAL_OPERATOR,
     LOGICAL_OPERATOR,
     UNARY_OPERATOR,
     TERNARY_OPERATOR,
     BITWISE_OPERATOR,

     // Literals
     INTEGER_LITERAL,
     FLOAT_LITERAL,
     STRING_LITERAL,
     BOOLEAN_LITERAL,
     CHARACTER_LITERAL,
     NULL_LITERAL,

     // Delimiters
     DELIMITER,
     PUNCTUATION_DELIMITER,
     STRING_DELIMITER,
     OBJECT_DELIMITER,

     // Comments
     COMMENT,

     // Whitespace
     WHITESPACE,

     // Escape Characters
     ESCAPE_CHARACTER,

     // End of File
     EOF
}
