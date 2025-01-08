package main;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import util.SourceReader;

/**
 * Entry point for testing the lexical analyzer.
 * Loads a source file, invokes the Lexer, and outputs tokens or error messages.
 */

// java code.main.Main <filePath> [--verbose] [--output=text|json]
// java code.main.Main source.xpresso --verbose --output=json

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to X-presso Lexer!");

        Scanner scanner = new Scanner(System.in);

        String filePath;
        boolean verbose;
        String outputFormat;

        // If no arguments, switch to interactive mode
        if (args.length < 1) {
            System.out.println("No arguments provided. Enter details interactively:");

            // Prompt for file path
            System.out.print("Enter source file path: ");
            filePath = scanner.nextLine();

            // Prompt for verbosity
            System.out.print("Enable verbose mode? (yes/no): ");
            verbose = scanner.nextLine().trim().equalsIgnoreCase("yes");

            // Prompt for output format
            System.out.print("Select output format (text/json): ");
            outputFormat = scanner.nextLine().trim().toLowerCase();
            if (!outputFormat.equals("text") && !outputFormat.equals("json")) {
                System.out.println("Invalid output format, defaulting to 'text'.");
                outputFormat = "text";
            }
        } else {
            filePath = args[0];
            verbose = args.length > 1 && "--verbose".equalsIgnoreCase(args[1]);
            outputFormat = args.length > 2 && args[2].startsWith("--output=") 
                            ? args[2].substring("--output=".length()) 
                            : "text";
        }

        try {
            // Initialize SourceReader
            SourceReader reader = new SourceReader(filePath, StandardCharsets.UTF_8);

            // Initialize Lexer
            Lexer lexer = new Lexer(reader);

            // Tokenize input
            List<Token> tokens = lexer.tokenize();

            // Output tokens
            if ("json".equalsIgnoreCase(outputFormat)) {
                printTokensAsJson(tokens, verbose);
            } else {
                printTokensAsText(tokens, verbose);
            }

            // Print token summary
            printTokenSummary(tokens);

            // Close SourceReader
            reader.close();
        } catch (SourceReader.SourceReaderException e) {
            System.err.println("Error reading source file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /**
     * Prints tokens in plain text format.
     *
     * @param tokens  List of tokens.
     * @param verbose Whether to include all tokens.
     */
    private static void printTokensAsText(List<Token> tokens, boolean verbose) {
        System.out.println("\nTokens:");
        System.out.println(Token.header()); // Print header
        for (Token token : tokens) {
            if (verbose || token.getType() != TokenType.WHITESPACE) {
                System.out.println(token); // Token's toString handles formatting
            }
        }
    }

    /**
     * Prints tokens in JSON format.
     *
     * @param tokens  List of tokens.
     * @param verbose Whether to include all tokens.
     */
    private static void printTokensAsJson(List<Token> tokens, boolean verbose) {
        System.out.println("\nTokens (JSON):");
        System.out.println("[");
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (verbose || token.getType() != TokenType.WHITESPACE) {
                System.out.print("  {");
                System.out.printf("\n    \"type\": \"%s\",\n    \"lexeme\": \"%s\",\n    \"line\": %d,\n    \"column\": %d\n  ",
                                    token.getType(), token.getLexeme(), token.getLine(), token.getColumn());
                System.out.println(i < tokens.size() - 1 ? "}," : "}");
            }
        }
        System.out.println("]");
    }

    /**
     * Prints a summary of token counts by type.
     *
     * @param tokens List of tokens.
     */
    private static void printTokenSummary(List<Token> tokens) {
        Map<TokenType, Long> tokenSummary = tokens.stream()
                .collect(Collectors.groupingBy(Token::getType, Collectors.counting()));

        System.out.println("\nToken Summary:");
        tokenSummary.forEach((type, count) -> System.out.printf("%-20s : %d%n", type, count));
    }
}
