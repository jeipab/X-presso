package source.main;

import source.util.SourceReader;
import source.lexer.Lexer;
import source.lexer.Token;
import source.lexer.TokenType;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The entry point for testing the lexical analyzer.
 * Loads a source file, invokes the Lexer, and prints the resulting tokens
 * or error messages for debugging purposes.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to X-presso Lexer!");

        // Check if a file path is provided
        if (args.length < 1) {
            System.err.println("Usage: java source.main.Main <source-file-path>");
            return;
        }

        String filePath = args[0];

        try {
            // Initialize the SourceReader
            SourceReader reader = new SourceReader(filePath, StandardCharsets.UTF_8);

            // Initialize the Lexer
            Lexer lexer = new Lexer(reader);

            // Tokenize the input
            List<Token> tokens = lexer.tokenize();

            // Print the resulting tokens
            System.out.println("Tokens:");
            for (Token token : tokens) {
                System.out.println(token);
            }

            // Close the SourceReader
            reader.close();
        } catch (SourceReader.SourceReaderException e) {
            System.err.println("Error reading source file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
