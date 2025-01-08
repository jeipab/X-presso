package code.language;
/**
 * Manages the keywords and reserved words of the language. 
 * Provides utilities to check if a string is a keyword or a reserved word 
 * to differentiate identifiers from language constructs.
 */

/*
 * Changed the class name to SpecialWords since we have different tokens for reserved words and keywords
 */
public class SpecialWords {
    /*
     * List of reserved, key, and noise words
     */
    private final String[] reservedWords = {"abstract", "after", "ALIAS", "before", "bool", "byte", "char", "class", "Complex", "Date", "double", "exclude", "export_as", "Frac", "filter_by", "final", "float", "inline_query", "inspect", "int", "long", "main", "modify", "native", "private", "protected", "public", "Rational", "short", "static", "STRICT", "strictfp", "str", "today", "toMixed", "transient", "validate", "volatile"};
    private final String[] keywords = {"break", "case", "day", "default", "do", "else", "exit", "exit when", "for", "get", "if", "in", "Input", "month", "Output", "print", "switch", "switch-fall", "while", "where type", "year"};
    private static final String noiseWord = "general";

    /*
     * Determines whether a lexeme is a keyword
     */
    public boolean isKeyword(String lexeme) {
        boolean match = false;
        for (String keyword : keywords) {
            if (lexeme.equals(keyword)) {
                match = true;
                break;
            }
        }
        return match;
    }
    
    /*
     * Determines whether a lexeme is a reserved word
     */
    public boolean isReservedWord(String lexeme) {
        boolean match = false;
        for (String reservedWord : reservedWords) {
            if (lexeme.equals(reservedWord)) {
                match = true;
                break;
            }
        }
        return match;
    }

    /*
     * Determines whether a lexeme is a noise word
     */
    public boolean isNoiseWord(String lexeme) {
        return lexeme.equals(noiseWord);
    }
}
