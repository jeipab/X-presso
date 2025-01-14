package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import util.ErrorHandler;
import util.SourceReader;

/**
 * Entry point for the X-presso lexical analyzer.
 * Processes source files and generates token analysis with configurable output options.
 */
public class Main {
    private static final String OUTPUT_DIR = "output";
    private static final String DEFAULT_OUTPUT_FORMAT = "text";
    
    private final Scanner scanner;
    private String filePath;
    private boolean verbose;
    private String outputFormat;
    private boolean outputToFile;
    
    public Main() {
        this.scanner = new Scanner(System.in);
        this.verbose = false;
        this.outputFormat = DEFAULT_OUTPUT_FORMAT;
        this.outputToFile = false;
    }
    
    /**
     * Entry point for the application.
     */
    public static void main(String[] args) {
        Main application = new Main();
        try {
            application.run(args);
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            application.cleanup();
        }
    }
    
    /**
     * Runs the lexical analysis process.
     */
    private void run(String[] args) throws IOException {
        System.out.println("Welcome to X-presso Lexer!");
        
        if (args.length < 1) {
            handleInteractiveMode();
        } else {
            handleCommandLineMode(args);
        }
        
        processFile();
    }
    
    /**
     * Handles interactive mode configuration.
     */
    private void handleInteractiveMode() throws IOException {
        System.out.println("No arguments provided. Enter details interactively:");
        
        // Get file path
        do {
            System.out.print("Enter source file path: ");
            filePath = scanner.nextLine().trim();
        } while (!validateFilePath(filePath));
        
        // Get verbosity preference
        System.out.print("Enable verbose mode? (yes/no): ");
        verbose = scanner.nextLine().trim().equalsIgnoreCase("yes");
        
        // Get output format
        System.out.print("Select output format (text/json): ");
        outputFormat = validateOutputFormat(scanner.nextLine().trim());
        
        // Get output destination preference
        System.out.print("Do you want to receive the output as a file? (yes/no): ");
        outputToFile = scanner.nextLine().trim().equalsIgnoreCase("yes");
    }
    
    /**
     * Handles command-line mode configuration.
     */
    private void handleCommandLineMode(String[] args) throws IOException {
        filePath = args[0];
        if (!validateFilePath(filePath)) {
            throw new IOException("Invalid file path: " + filePath);
        }
        
        for (int i = 1; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--verbose" -> verbose = true;
                case "--file" -> outputToFile = true;
                default -> {
                    if (args[i].startsWith("--output=")) {
                        outputFormat = validateOutputFormat(
                            args[i].substring("--output=".length())
                        );
                    }
                }
            }
        }
    }
    
    /**
     * Processes the input file and generates output.
     */
    private void processFile() throws IOException {
        // Create output directory if needed
        if (outputToFile) {
            createOutputDirectory();
        }
        
        try (SourceReader reader = new SourceReader(filePath, StandardCharsets.UTF_8)) {
            // Create lexer and tokenize
            List<Token> tokens = tokenizeSource(reader);
            
            // Output tokens
            outputTokens(tokens);
            
            // Print token summary
            printTokenSummary(tokens);
            
        } catch (Exception e) {
            throw new IOException("Error processing file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tokenizes the source using the lexer.
     */
    private List<Token> tokenizeSource(SourceReader reader) throws IOException {
        try {
            Lexer lexer = createLexer(reader);
            List<Token> tokens = lexer.tokenize();
            handleLexicalErrors(lexer);
            return tokens;
        } catch (Exception e) {
            throw new IOException("Error during tokenization: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a new Lexer instance.
     */
    private Lexer createLexer(SourceReader reader) {
        return new Lexer(reader);
    }
    
    /**
     * Validates and creates output directory if needed.
     */
    private void createOutputDirectory() throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
    }
    
    /**
     * Validates the file path.
     */
    private boolean validateFilePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            System.err.println("File path cannot be empty.");
            return false;
        }
        
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("File does not exist: " + path);
            return false;
        }
        
        if (!file.canRead()) {
            System.err.println("Cannot read file: " + path);
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates and normalizes the output format.
     */
    private String validateOutputFormat(String format) {
        if (format == null || (!format.equals("text") && !format.equals("json"))) {
            System.out.println("Invalid output format, defaulting to 'text'.");
            return DEFAULT_OUTPUT_FORMAT;
        }
        return format;
    }
    
    /**
     * Outputs tokens in the specified format.
     */
    private void outputTokens(List<Token> tokens) throws IOException {
        if (outputToFile) {
            writeTokensToFile(tokens);
        } else {
            if ("json".equals(outputFormat)) {
                printTokensAsJson(tokens);
            } else {
                printTokensAsText(tokens);
            }
        }
    }
    
    /**
     * Prints tokens in text format to console.
     */
    private void printTokensAsText(List<Token> tokens) {
        System.out.println("\nTokens:");
        System.out.println(Token.header());
        tokens.stream()
            .filter(token -> shouldPrintToken(token))
            .forEach(System.out::println);
    }
    
    /**
     * Prints tokens in JSON format to console.
     */
    private void printTokensAsJson(List<Token> tokens) {
        System.out.println("\nTokens (JSON):");
        System.out.println("[");
        
        String jsonTokens = tokens.stream()
            .filter(this::shouldPrintToken)
            .map(this::tokenToJson)
            .collect(Collectors.joining(",\n"));
            
        System.out.println(jsonTokens);
        System.out.println("]");
    }
    
    /**
     * Writes tokens to a file in the specified format.
     */
    private void writeTokensToFile(List<Token> tokens) throws IOException {
        String baseName = Paths.get(filePath).getFileName().toString();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf("."));
        }
        
        String extension = "json".equals(outputFormat) ? "json" : "txt";
        Path outputPath = Paths.get(OUTPUT_DIR, baseName + "_output." + extension);
        
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            if ("json".equals(outputFormat)) {
                writeTokensAsJson(tokens, writer);
            } else {
                writeTokensAsText(tokens, writer);
            }
            System.out.println("Tokens written to file: " + outputPath);
        }
    }
    
    /**
     * Writes tokens in text format to a file.
     */
    private void writeTokensAsText(List<Token> tokens, FileWriter writer) throws IOException {
        writer.write(Token.header() + "\n");
        for (Token token : tokens) {
            if (shouldPrintToken(token)) {
                writer.write(token.toString() + "\n");
            }
        }
    }
    
    /**
     * Writes tokens in JSON format to a file.
     */
    private void writeTokensAsJson(List<Token> tokens, FileWriter writer) throws IOException {
        writer.write("[\n");
        List<Token> filteredTokens = tokens.stream()
            .filter(this::shouldPrintToken)
            .collect(Collectors.toList());
            
        for (int i = 0; i < filteredTokens.size(); i++) {
            writer.write(tokenToJson(filteredTokens.get(i)));
            if (i < filteredTokens.size() - 1) {
                writer.write(",");
            }
            writer.write("\n");
        }
        writer.write("]\n");
    }
    
    /**
     * Converts a token to JSON format.
     */
    private String tokenToJson(Token token) {
        return String.format("""
            {
                "type": "%s",
                "lexeme": "%s",
                "line": %d,
                "column": %d
            }""",
            token.getType(),
            escapeJsonString(token.getLexeme()),
            token.getLine(),
            token.getColumn()
        );
    }
    
    /**
     * Determines whether a token should be included in output.
     */
    private boolean shouldPrintToken(Token token) {
        return verbose || 
                (token.getType() != TokenType.WHITESPACE && 
                token.getType() != TokenType.COMMENT && 
                token.getType() != TokenType.EOF);
    }
    
    /**
     * Prints a summary of token counts by type.
     */
    private void printTokenSummary(List<Token> tokens) {
        Map<TokenType, Long> tokenSummary = tokens.stream()
            .collect(Collectors.groupingBy(Token::getType, Collectors.counting()));
            
        System.out.println("\nToken Summary:");
        tokenSummary.forEach((type, count) -> 
            System.out.printf("%-20s : %d%n", type, count));
    }
    
    /**
     * Handles and displays lexical errors.
     */
    private void handleLexicalErrors(Lexer lexer) {
        ErrorHandler errorHandler = lexer.getErrorHandler();
        if (errorHandler.hasErrors()) {
            if ("json".equals(outputFormat)) {
                printErrorsAsJson(errorHandler.getErrors());
            } else {
                errorHandler.printErrors();
            }
            
            System.err.println("\nError Statistics:");
            Map<ErrorHandler.ErrorType, Long> errorStats = errorHandler.getErrors().stream()
                .collect(Collectors.groupingBy(error -> error.getType(), Collectors.counting()));
                
            errorStats.forEach((type, count) -> 
                System.err.printf("%-25s : %d%n", type, count));
        }
    }
    
    /**
     * Prints errors in JSON format.
     */
    private void printErrorsAsJson(List<ErrorHandler.LexicalError> errors) {
        System.err.println("\nErrors (JSON):");
        System.err.println("[");
        for (int i = 0; i < errors.size(); i++) {
            ErrorHandler.LexicalError error = errors.get(i);
            System.err.printf("""
                {
                    "type": "%s",
                    "message": "%s",
                    "line": %d,
                    "column": %d%s
                }%s
                """,
                error.getType(),
                escapeJsonString(error.getMessage()),
                error.getLine(),
                error.getColumn(),
                error.getSuggestion() != null && !error.getSuggestion().isEmpty() 
                    ? String.format(",\n    \"suggestion\": \"%s\"", escapeJsonString(error.getSuggestion()))
                    : "",
                i < errors.size() - 1 ? "," : ""
            );
        }
        System.err.println("]");
    }
    
    /**
     * Escapes special characters in JSON strings.
     */
    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder escaped = new StringBuilder();
        for (char ch : input.toCharArray()) {
            escaped.append(switch (ch) {
                case '"' -> "\\\"";
                case '\\' -> "\\\\";
                case '\b' -> "\\b";
                case '\f' -> "\\f";
                case '\n' -> "\\n";
                case '\r' -> "\\r";
                case '\t' -> "\\t";
                default -> ch < ' ' || ch > '~' ? String.format("\\u%04x", (int) ch) : ch;
            });
        }
        return escaped.toString();
    }
    
    /**
     * Cleans up resources.
     */
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
}