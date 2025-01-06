package source.lexer;


/**
 * Defines the possible categories of tokens that the lexical analyzer can recognize. 
 * Examples include IDENTIFIER, KEYWORD, OPERATOR, LITERAL, and DELIMITER.
 * This enumeration provides structure for classifying tokens.
 */

public enum TokenType {

     // IDENTIFIERS
     IDENTIFIER, 

     // KEYWORDS
     KEYWORD, 
     RESERVED_WORD,


     // ARITHMETIC OPERATORS
     PLUS_OPERATOR,
     MINUS_OPERATOR,
     MULTIPLY_OPERATOR,
     DIVIDE_OPERATOR,
     MODULUS_OPERATOR,


     ASSIGNMENT_OPERATOR, // "=", "+=", "-=", "*=", "/=", "%=", "?="

     // RELATIONAL OPERATORS
     EQUAL_OPERATOR,
     GREATER_THAN,
     LESS_THAN,
     GREATER_THAN_OR_EQUAL,
     LESS_THAN_OR_EQUAL,
     NOT_EQUAL,


     // LOGICAL OPERATORS
     AND_OPERATOR,
     OR_OPERATOR,
     NOT_OPERATOR,

     // UNARY OPERATOR
     INCREMENT,
     DECREMENT,
     SELF_EXPONENTIATION,

     // BITWISE OPERATORS
     RIGHT_SIGNED_SHIFT,
     LEFT_SIGNED_SHIFT,
     RIGHT_UNSIGNED_SHIFT,
     LEFT_UNSIGNED_SHIFT,

     // LITERALS
     STRING_LITERAL, 
     INTEGER,
     FLOAT,
     DOUBLE,
     FRACTION,
     BOOLEAN,
     CHARACTER,
     MIXED,
     NULL, // Not sure for " "

     // DELIMITERS
     LEFT_PARENTHESIS,
     RIGHT_PARENTHESIS,
     LEFT_BRACE,
     RIGHT_BRACE,
     LEFT_BRACKET,
     RIGHT_BRACKET,

     // PUNCTUATION_DELIMTER?? or etong mga nasa baba?
     SEMICOLON,
     COLON,
     COMMA,
     TERNARY_OPERATOR, // "?"
     ANNOTATION, // "@"

     STRING_DELIMETER, // " \ "
     
     OBJECT_DELIMETER,  // "< >", "{ }"

     // COMMENTS
     SINGLE_COMMENT,
     MULTILINE_START_COMMENT, 
     MULTILINE_END_COMMENT,

     // WHITE SPACE
     TAB, 
     NEWLINE,

     //FOR NEW PRINCIPLES (dunno san to lalagay)
     METHOD_CALL,
     ACCESS_PROPERTY,

     

     
}
