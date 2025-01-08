package source.lexer;


/**
 * Defines the possible categories of tokens that the lexical analyzer can recognize. 
 * Examples include IDENTIFIER, KEYWORD, OPERATOR, LITERAL, and DELIMITER.
 * This enumeration provides structure for classifying tokens.
 */

public enum Token {

     // IDENTIFIERS
     IDENTIFIER, 


     //KEYWORD, 
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

     //RESERVED_WORD,

          ABSTRACT_RESERVED_WORD,  
          AFTER_RESERVED_WORD,  
          ALIAS_RESERVED_WORD,  
          BEFORE_RESERVED_WORD,  
          BOOL_DATA_TYPE,  
          BYTE_DATA_TYPE,  
          CHAR_DATA_TYPE,  
          CLASS_RESERVED_WORD,  
          COMPLEX_DATA_TYPE,  
          DATE_DATA_TYPE,  
          DOUBLE_DATA_TYPE,  
          EXCLUDE_RESERVED_WORD,  
          EXPORT_AS_RESERVED_WORD,  
          FRAC_DATA_TYPE,  
          FILTER_BY_RESERVED_WORD,  
          FINAL_RESERVED_WORD,  
          FLOAT_DATA_TYPE,  
          INLINE_QUERY_RESERVED_WORD,  
          INSPECT_RESERVED_WORD,  
          INT_DATA_TYPE,  
          LONG_DATA_TYPE,  
          MAIN_RESERVED_WORD,  
          MODIFY_RESERVED_WORD,  
          NATIVE_RESERVED_WORD,  
          PRIVATE_RESERVED_WORD,  
          PROTECTED_RESERVED_WORD,  
          PUBLIC_RESERVED_WORD,  
          RATIONAL_DATA_TYPE,  
          SHORT_DATA_TYPE,  
          STATIC_RESERVED_WORD,  
          STRICT_RESERVED_WORD,  
          STRICTFP_RESERVED_WORD,  
          STR_DATA_TYPE,  
          TODAY_RESERVED_WORD,  
          TOMIXED_RESERVED_WORD,  
          TRANSIENT_RESERVED_WORD,  
          VALIDATE_RESERVED_WORD,  
          VOLATILE_RESERVED_WORD,  

     // ARITHMETIC_OPERATOR,

          PLUS_OPERATOR,
          MINUS_OPERATOR,
          MULTIPLY_OPERATOR,
          DIVIDE_OPERATOR,
          MODULUS_OPERATOR,


     // ASSIGNMENT_OPERATOR, 

          EQUAL_ASSIGN_OPERATOR, // ( = )  
          ADDITION_ASSIGN_OPERATOR, // ( += )  
          SUBTRACTION_ASSIGN_OPERATOR, // ( -= )  
          MULTIPLICATION_ASSIGN_OPERATOR, // ( *= )  
          DIVISION_ASSIGN_OPERATOR, // ( /= )  
          MODULUS_ASSIGN_OPERATOR, // ( %= )  
          NULL_ASSIGN_OPERATOR, // ( ?= )  


     //RELATIONAL_OPERATOR,
     
          EQUAL_OPERATOR,
          GREATER_THAN,
          LESS_THAN,
          GREATER_THAN_OR_EQUAL,
          LESS_THAN_OR_EQUAL,
          NOT_EQUAL,


     //LOGICAL_OPERATOR,
     
          LOGICAL_AND_OPERATOR,
          LOGICAL_OR_OPERATOR,
          LOGICAL_NOT_OPERATOR, 

     //UNARY_OPERATOR,
     
          INCREMENT,
          DECREMENT,
          SELF_EXPONENTIATION,


     //TERNARY_OPERATOR,
          QUESTION_MARK_TERNARY_OPERATOR, 
          COLON__OPERATOR, 
     

     //BITWISE_OPERATORS,
     
          RIGHT_SIGNED_SHIFT,
          LEFT_SIGNED_SHIFT,
          RIGHT_UNSIGNED_SHIFT,
          LEFT_UNSIGNED_SHIFT,

     //LITERALS,
     
          STRING_LITERAL,
          INTEGER_LITERAL,
          FLOAT_LITERAL,
          DOUBLE_LITERAL,
          FRACTION_LITERAL,
          BOOLEAN_LITERAL,
          CHARACTER_LITERAL,
          MIXED_LITERAL,
          NULL,

     // DELIMITERS,
          LEFT_PARENTHESIS,
          RIGHT_PARENTHESIS,
          LEFT_BRACE,
          RIGHT_BRACE,
          LEFT_BRACKET,
          RIGHT_BRACKET,

     //PUNCTUATION_DELIMTER,
          SEMICOLON,
          COLON,
          COMMA,
          ANNOTATION, // "@"  */


     // STRING_DELIMETER,  
     
          DOUBLE_QUOTATION, 
          SINGLE_QUOTATION, 
     
     //OBJECT_DELIMETER,  // "< >", "{ }"
     
          LEFT_ANGLE_BRACKET,
          RIGHT_ANGLE_BRACKET,
          LEFT_CURLY_BRACE,
          RIGHT_CURLY_BRACE,


     // COMMENT,
     
          SINGLE_COMMENT,
          MULTILINE_START_COMMENT, 
          MULTILINE_END_COMMENT,   

     //ESCAPE_CHARACTER,
          BACKSLASH_ESCAPE_CHARACTER, 
          SINGLE_QUOTE_ESCAPE_CHARACTER, 
          DOUBLE_QUOTE_ESCAPE_CHARACTER, 
          NEWLINE_ESCAPE_CHARACTER, 
          TAB_ESCAPE_CHARACTER, 
          CARRIAGE_RETURN_ESCAPE_CHARACTER, 
          BACKSPACE_ESCAPE_CHARACTER, 
          FORM_FEED_ESCAPE_CHARACTER, 
          UNICODE_ESCAPE_CHARACTER,
}
