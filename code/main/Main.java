package main;

import java.io.FileWriter;
import java.io.IOException;
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

/* Compile with:
 * javac -d build -cp code code/main/Main.java  
 */

/* Run with:
 * java -cp build main.Main <filePath> [--verbose] [--output=text|json] [--file]
 */

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to X-presso Lexer!");

        Scanner scanner = new Scanner(System.in);

        String filePath = null;
        boolean verbose = false;
        String outputFormat = "text";
        boolean receiveFile = false;

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

            // Prompt to receive a file
            System.out.print("Do you want to receive the output as a file? (yes/no): ");
            receiveFile = scanner.nextLine().trim().equalsIgnoreCase("yes");
        } else {
            // Parse command-line arguments
            filePath = args[0]; // First argument is always the file path
            for (int i = 1; i < args.length; i++) {
                if ("--verbose".equalsIgnoreCase(args[i])) {
                    verbose = true;
                } else if (args[i].startsWith("--output=")) {
                    outputFormat = args[i].substring("--output=".length()).toLowerCase();
                    if (!outputFormat.equals("text") && !outputFormat.equals("json")) {
                        System.out.println("Invalid output format, defaulting to 'text'.");
                        outputFormat = "text";
                    }
                } else if ("--file".equalsIgnoreCase(args[i])) {
                    receiveFile = true;
                }
            }
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

            // Write tokens to a file if requested
            if (receiveFile) {
                writeTokensToFile(tokens, outputFormat, verbose, filePath);
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
                if (verbose || token.getType() != TokenType.COMMENT)
                    if (verbose || token.getType() != TokenType.EOF){
                        System.out.println(token); // Token's toString handles formatting
                    }
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
        boolean first = true;
        for (Token token : tokens) {
            if (verbose || token.getType() != TokenType.WHITESPACE) {
                if (verbose || token.getType() != TokenType.COMMENT) {
                    if (verbose || token.getType() != TokenType.EOF) {
                        if (!first) {
                            System.out.println(",");
                        }
                        first = false;
                        String escapedLexeme = escapeJsonString(token.getLexeme());
                        System.out.printf("  {\n    \"type\": \"%s\",\n    \"lexeme\": \"%s\",\n    \"line\": %d,\n    \"column\": %d\n  }",
                            token.getType(), escapedLexeme, token.getLine(), token.getColumn());
                    }
                }
            }
        }
        System.out.println("\n]");
    }     

    /**
     * Writes tokens to a file in either plain text or JSON format.
     * @param tokens List of tokens.
     * @param format "text" or "json".
     * @param verbose Include all tokens, not just non-whitespace.
     */
    private static void writeTokensToFile(List<Token> tokens, String format, boolean verbose, String inputFilePath) {
        // Extract the base name of the input file (without directory path and extension)
        String baseName = inputFilePath.substring(inputFilePath.lastIndexOf("\\") + 1);
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf("."));
        }
    
        String fileName = "output/" + baseName + "_output." + (format.equals("json") ? "json" : "txt");
    
        try (FileWriter writer = new FileWriter(fileName)) {
            if ("json".equalsIgnoreCase(format)) {
                writer.write("[\n");
                boolean first = true;
                for (Token token : tokens) {
                    if (verbose || token.getType() != TokenType.WHITESPACE) {
                        if (verbose || token.getType() != TokenType.COMMENT) {
                            if (verbose || token.getType() != TokenType.EOF) {
                                if (!first) {
                                    writer.write(",\n");
                                }
                                first = false;
                                String escapedLexeme = escapeJsonString(token.getLexeme());
                                writer.write(String.format(
                                    "  {\n    \"type\": \"%s\",\n    \"lexeme\": \"%s\",\n    \"line\": %d,\n    \"column\": %d\n  }",
                                    token.getType(), escapedLexeme, token.getLine(), token.getColumn()
                                ));
                            }
                        }
                    }
                }
                writer.write("\n]\n");
            } else {
                writer.write(Token.header() + "\n");
                for (Token token : tokens) {
                    if (verbose || token.getType() != TokenType.WHITESPACE) {
                        if (verbose || token.getType() != TokenType.COMMENT) {
                            if (verbose || token.getType() != TokenType.EOF) {
                                writer.write(token.toString() + "\n");
                            }
                        }
                    }
                }
            }
            System.out.println("Tokens written to file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
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

    private static String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    // Unicode escape for non-ASCII characters
                    if (ch < ' ' || ch > '~') {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
            }
        }
        return escaped.toString();
    }
}
