package source.lexer;

/**
 * Defines the possible categories of tokens that the lexical analyzer can recognize. 
 * Examples include IDENTIFIER, KEYWORD, OPERATOR, LITERAL, and DELIMITER.
 * This enumeration provides structure for classifying tokens.
 */

public class TokenType {
    
public enum TokenType {

     // IDENTIFIERS
     IDENTIFIER, 

     // KEYWORDS
     KEYWORD, 
     RESERVED_WORD,


     // OPERATORS
     PLUS_OPERATOR,
     MINUS_OPERATOR,
     MULTIPLY_OPERATOR,
     DIVIDE_OPERATOR,
     MODULUS_OPERATOR,
     ASSIGNMENT_OPERATOR,
     EQUAL_OPERATOR,
     GREATER_THAN,
     LESS_THAN,
     GREATER_THAN_OR_EQUAL,
     LESS_THAN_OR_EQUAL,
     NOT_EQUAL,
     AND_OPERATOR,
     OR_OPERATOR,
     NOT_OPERATOR,
     INCREMENT,
     DECREMENT,
     SELF_EXPONENTIATION,
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
     NULL, // Not sure for " "

     // DELIMITERS
     LEFT_PARENTHESIS,
     RIGHT_PARENTHESIS,
     LEFT_BRACE,
     RIGHT_BRACE,
     LEFT_BRACKET,
     RIGHT_BRACKET,

     // COMMENTS
     SINGLE_COMMENT,
     MULTILINE_START_COMMENT, 
     MULTILINE_END_COMMENT,

     // WHITE SPACE
     TAB, 
     NEWLINE,
     
     // SEMICOLON?? SPECIAL CHARACTERS???
     SEMICOLON,
     COLON,
     COMMA,
     
}
