package lexer;

/**
 * Implements regular expressions and pattern matching without external libraries
 */
public class Patterns {
    // Character Set Definitions
    private static final char[] UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWERCASE = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] DIGITS = "0123456789".toCharArray();
    private static final char[] OPERATORS = "+-*/%=<>!&|^~.:?".toCharArray();
    private static final char[] DELIMITERS = "(){}[];,".toCharArray();

    /**
     * RegularExpression class for pattern matching
     */
    private static class RegularExpression {
        private final String pattern;
        
        public RegularExpression(String pattern) {
            this.pattern = pattern;
        }
        
        public boolean matches(String input) {
            return match(input, pattern, 0, 0);
        }
        
        private boolean match(String input, String pattern, int inputIndex, int patternIndex) {
            if (patternIndex == pattern.length()) {
                return inputIndex == input.length();
            }
            
            if (patternIndex < pattern.length() && pattern.charAt(patternIndex) == '*') {
                char prevChar = pattern.charAt(patternIndex - 1);
                // Match zero or more occurrences
                for (int i = inputIndex; i <= input.length(); i++) {
                    if (match(input, pattern, i, patternIndex + 1)) {
                        return true;
                    }
                    if (i < input.length() && !matchChar(input.charAt(i), prevChar)) {
                        break;
                    }
                }
                return false;
            }
            
            if (patternIndex < pattern.length() && pattern.charAt(patternIndex) == '+') {
                char prevChar = pattern.charAt(patternIndex - 1);
                // Match one or more occurrences
                if (inputIndex >= input.length() || !matchChar(input.charAt(inputIndex), prevChar)) {
                    return false;
                }
                for (int i = inputIndex + 1; i <= input.length(); i++) {
                    if (match(input, pattern, i, patternIndex + 1)) {
                        return true;
                    }
                    if (i < input.length() && !matchChar(input.charAt(i), prevChar)) {
                        break;
                    }
                }
                return false;
            }
            
            if (inputIndex >= input.length()) {
                return false;
            }
            
            if (patternIndex < pattern.length() && matchChar(input.charAt(inputIndex), pattern.charAt(patternIndex))) {
                return match(input, pattern, inputIndex + 1, patternIndex + 1);
            }
            
            return false;
        }
        
        private boolean matchChar(char input, char pattern) {
            switch (pattern) {
                case 'L': return isLetter(input);
                case 'D': return isDigit(input);
                case 'O': return isOperator(input);
                case 'M': return isDelimiter(input);
                case '_': return input == '_';
                default: return input == pattern;
            }
        }
    }

    // Regular Expression Patterns from Documentation
    private static final RegularExpression IDENTIFIER_PATTERN = 
        new RegularExpression("L(L+D)*(_)*(L+D)*");
    
    private static final RegularExpression ASSIGN_OP_PATTERN = 
        new RegularExpression("=(+|-|/|*|%|?)?=");
        
    private static final RegularExpression ARITHMETIC_OP_PATTERN = 
        new RegularExpression("(+|-|*|/|%|^)");
        
    private static final RegularExpression LOGICAL_OP_PATTERN = 
        new RegularExpression("(&(&)|!|(||))");
        
    private static final RegularExpression RELATIONAL_OP_PATTERN = 
        new RegularExpression("(<|>)(=)?|(!|=)=");
        
    private static final RegularExpression BITWISE_OP_PATTERN = 
        new RegularExpression("(&|^|~|<<|>>|>>>)");
        
    private static final RegularExpression UNARY_OP_PATTERN = 
        new RegularExpression("(+|-|*)+(+|-|*)");
        
    private static final RegularExpression METHOD_OP_PATTERN = 
        new RegularExpression("(.|(::))");

    // Helper Methods for Character Classification
    public static boolean isLetter(char c) {
        return isUppercase(c) || isLowercase(c);
    }

    public static boolean isUppercase(char c) {
        for (char upper : UPPERCASE) {
            if (c == upper) return true;
        }
        return false;
    }

    public static boolean isLowercase(char c) {
        for (char lower : LOWERCASE) {
            if (c == lower) return true;
        }
        return false;
    }

    public static boolean isDigit(char c) {
        for (char digit : DIGITS) {
            if (c == digit) return true;
        }
        return false;
    }

    public static boolean isOperator(char c) {
        for (char op : OPERATORS) {
            if (c == op) return true;
        }
        return false;
    }

    public static boolean isDelimiter(char c) {
        for (char delim : DELIMITERS) {
            if (c == delim) return true;
        }
        return false;
    }

    // Pattern Matching Methods
    public static boolean matchIdentifier(String input) {
        return IDENTIFIER_PATTERN.matches(input);
    }

    public static boolean matchAssignOp(String input) {
        return ASSIGN_OP_PATTERN.matches(input);
    }

    public static boolean matchArithmeticOp(String input) {
        return ARITHMETIC_OP_PATTERN.matches(input);
    }

    public static boolean matchLogicalOp(String input) {
        return LOGICAL_OP_PATTERN.matches(input);
    }

    public static boolean matchRelationalOp(String input) {
        return RELATIONAL_OP_PATTERN.matches(input);
    }

    public static boolean matchBitwiseOp(String input) {
        return BITWISE_OP_PATTERN.matches(input);
    }

    public static boolean matchUnaryOp(String input) {
        return UNARY_OP_PATTERN.matches(input);
    }

    public static boolean matchMethodOp(String input) {
        return METHOD_OP_PATTERN.matches(input);
    }

    // Complex Pattern Matching Methods
    public static boolean matchDateLiteral(String input) {
        if (input == null || input.length() != 12) return false;
        if (input.charAt(0) != '[' || input.charAt(11) != ']') return false;
        
        // [YYYY|MM|DD] format
        String content = input.substring(1, 11);
        String[] parts = content.split("\\|");
        if (parts.length != 3) return false;
        
        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            
            return year >= 0 && 
                month >= 1 && month <= 12 &&
                day >= 1 && day <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean matchFractionLiteral(String input) {
        if (input == null || input.length() < 5) return false;
        if (input.charAt(0) != '[' || input.charAt(input.length()-1) != ']') return false;
        
        String content = input.substring(1, input.length()-1);
        String[] parts = content.split("\\|");
        if (parts.length != 2) return false;
        
        try {
            int numerator = Integer.parseInt(parts[0]);
            int denominator = Integer.parseInt(parts[1]);
            return denominator != 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean matchComplexLiteral(String input) {
        if (input == null || !input.startsWith("$(") || !input.endsWith(")")) return false;
        
        String content = input.substring(2, input.length()-1);
        String[] parts = content.split(",");
        if (parts.length != 2) return false;
        
        return matchNumber(parts[0].trim()) && matchNumber(parts[1].trim());
    }

    private static boolean matchNumber(String input) {
        if (input == null || input.isEmpty()) return false;
        
        boolean hasDecimal = false;
        int start = 0;
        
        if (input.charAt(0) == '+' || input.charAt(0) == '-') {
            if (input.length() == 1) return false;
            start = 1;
        }
        
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '.') {
                if (hasDecimal) return false;
                hasDecimal = true;
            } else if (!isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}