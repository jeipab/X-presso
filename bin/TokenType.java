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
     /*
          BREAK_KEYWORD,
          CASE_KEYWORD,  
          DAY_KEYWORD,  
          DEFAULT_KEYWORD,  
          DO_KEYWORD,  
          ELSE_KEYWORD,  
          EXIT_KEYWORD, 
          EXIT_WHEN_KEYWORD,  
          FOR_KEYWORD,  
          GET_KEYWORD,  
          IF_KEYWORD,  
          IN_KEYWORD,  
          INPUT_KEYWORD,  
          MONTH_KEYWORD,  
          OUTPUT_KEYWORD, 
          PRINT_KEYWORD,
          SWITCH_KEYWORD,
          SWITCH_FALL_KEYWORD,
          WHILE_KEYWORD,  
          WHERE_TYPE_KEYWORD,  
          YEAR_KEYWORD,  

     */
     RESERVED_WORD,
     /*
          ABSTRACT_RESERVED_WORD,
          AFTER_RESERVED_WORD,  
          ALIAS_RESERVED_WORD,  
          BEFORE_RESERVED_WORD,  
          BOOL_RESERVED_WORD,  
          BYTE_RESERVED_WORD,  
          CHAR_RESERVED_WORD,  
          CLASS_RESERVED_WORD,  
          COMPLEX_RESERVED_WORD,  
          DATE_RESERVED_WORD,  
          DOUBLE_RESERVED_WORD,  
          EXCLUDE_RESERVED_WORD, 
          EXPORT_AS_RESERVED_WORD,  
          FRAC_RESERVED_WORD,  
          FILTER_BY_RESERVED_WORD,  
          FINAL_RESERVED_WORD,  
          FLOAT_RESERVED_WORD,  
          INLINE_QUERY_RESERVED_WORD,  
          INSPECT_RESERVED_WORD,  
          INT_RESERVED_WORD,  
          LONG_RESERVED_WORD,  
          MAIN_RESERVED_WORD,  
          MODIFY_RESERVED_WORD,  
          NATIVE_RESERVED_WORD,  
          PRIVATE_RESERVED_WORD, 
          PROTECTED_RESERVED_WORD,  
          PUBLIC_RESERVED_WORD,  
          RATIONAL_RESERVED_WORD, 
          SHORT_RESERVED_WORD,  
          STATIC_RESERVED_WORD,  
          STRICT_RESERVED_WORD,  
          STRICTFP_RESERVED_WORD,  
          STR_RESERVED_WORD,  
          TODAY_RESERVED_WORD,  
          TOMIXED_RESERVED_WORD, 
          TRANSIENT_RESERVED_WORD,  
          VALIDATE_RESERVED_WORD,  
          VOLATILE_RESERVED_WORD,  

      */


     ARITHMETIC_OPERATOR,
     /* 
          PLUS_OPERATOR,
          MINUS_OPERATOR,
          MULTIPLY_OPERATOR,
          DIVIDE_OPERATOR,
          MODULUS_OPERATOR,*/


     ASSIGNMENT_OPERATOR, // "=", "+=", "-=", "*=", "/=", "%=", "?="

     /*   ASSIGN_OP_KEYWORD // ( = )  
          PLUS_ASSIGN_OP_KEYWORD // ( += )  
          MINUS_ASSIGN_OP_KEYWORD // ( -= )  
          MULT_ASSIGN_OP_KEYWORD // ( *= )  
          DIV_ASSIGN_OP_KEYWORD // ( /= )  
          MOD_ASSIGN_OP_KEYWORD // ( %= )  
          NULL_ASSIGN_OP_KEYWORD // ( ?= )  */


     RELATIONAL_OPERATOR,
     /* 
          EQUAL_OPERATOR,
          GREATER_THAN,
          LESS_THAN,
          GREATER_THAN_OR_EQUAL,
          LESS_THAN_OR_EQUAL,
          NOT_EQUAL,*/


     LOGICAL_OPERATOR,
     /*
          AND_OPERATOR,
          OR_OPERATOR,
          NOT_OPERATOR, */

     UNARY_OPERATOR,
     /*   
          INCREMENT,
          DECREMENT,
          SELF_EXPONENTIATION, */

     TERNARY_OPERATOR,
     /*
          QUESTION_MARK_TERNARY_OPERATOR, 
          COLON__OPERATOR, */
     

     BITWISE_OPERATORS,
     /* 
          RIGHT_SIGNED_SHIFT,
          LEFT_SIGNED_SHIFT,
          RIGHT_UNSIGNED_SHIFT,
          LEFT_UNSIGNED_SHIFT,*/

     LITERALS,
     /* 
          STRING_LITERAL,
          INTEGER_LITERAL,
          FLOAT_LITERAL,
          DOUBLE_LITERAL,
          FRACTION_LITERAL,
          BOOLEAN_LITERAL,
          CHARACTER_LITERAL,
          MIXED_LITERAL,
          NULL,*/

     DELIMITERS,
     /*
          LEFT_PARENTHESIS,
          RIGHT_PARENTHESIS,
          LEFT_BRACE,
          RIGHT_BRACE,
          LEFT_BRACKET,
          RIGHT_BRACKET, 
          BACK_SLASH_DELIMITER*/

     PUNCTUATION_DELIMTER,
     /* 
          SEMICOLON,
          COLON,
          COMMA,
          ANNOTATION, // "@"  */

     STRING_DELIMETER,  
     /* 
          DOUBLE_QUOTATION, 
          SINGLE_QUOTATION */
     
     OBJECT_DELIMETER,  // "< >", "{ }"
     /*
          LEFT_ANGLE_BRACKET,
          RIGHT_ANGLE_BRACKET,
          LEFT_CURLY_BRACE,
          RIGHT_CURLY_BRACE,*/
      

     COMMENT,
     /* 
          SINGLE_COMMENT,
          MULTILINE_START_COMMENT, 
          MULTILINE_END_COMMENT,   */

     ESCAPE_CHARACTER,
     /*      
          BACKSLASH_ESCAPE_CHARACTER, 
          SINGLE_QUOTE_ESCAPE_CHARACTER, 
          DOUBLE_QUOTE_ESCAPE_CHARACTER, 
          NEWLINE_ESCAPE_CHARACTER, 
          TAB_ESCAPE_CHARACTER, 
          CARRIAGE_RETURN_ESCAPE_CHARACTER, 
          BACKSPACE_ESCAPE_CHARACTER, 
          FORM_FEED_ESCAPE_CHARACTER, 
          UNICODE_ESCAPE_CHARACTER, */
     
}
