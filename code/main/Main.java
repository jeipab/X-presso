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
     * 
     * Creates an instance of the Main class and runs its {@link #run(String[])} method with the provided command line arguments.
     * Catches any exceptions thrown during the execution and prints the error message to the console.
     * Finally, calls the {@link #cleanup()} method to release any system resources allocated during the execution.
     * 
     * @param args The command line arguments.
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
     * 
     * This method is the entry point for the application when run from the command line.
     * It processes the source file provided as an argument and prints the token analysis to the console.
     * If no argument is provided, it runs in interactive mode and prompts the user for the file path and other configuration options.
     * 
     * @param args The command line arguments.
     * @throws IOException If an error occurs while reading the source file.
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
     * 
     * This method is called when no command line arguments are provided.
     * It prompts the user to enter the source file path, verbosity preference, output format, and output destination preference.
     * The user's inputs are validated and stored in the corresponding fields.
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
     * 
     * @param args The command-line arguments containing the source file path and optional parameters.
     * @throws IOException If the file path is invalid.
     */
    private void handleCommandLineMode(String[] args) throws IOException {
        filePath = args[0];
        // Validate file path
        if (!validateFilePath(filePath)) {
            throw new IOException("Invalid file path: " + filePath);
        }
        
        // Process optional parameters
        for (int i = 1; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--verbose" -> verbose = true;
                    // Enable verbose mode
                case "--file" -> outputToFile = true;
                    // Output to file
                default -> {
                    if (args[i].startsWith("--output=")) {
                        // Custom output format
                        outputFormat = validateOutputFormat(
                            // Extract output format from argument
                            args[i].substring("--output=".length())
                        );
                    }
                }
            }
        }
    }

    /**
     * Validates the file path.
     * Checks if the file path is not null or empty and if the file exists and can be read.
     * If any of these conditions are not met, an error message is printed and the method returns false.
     * Otherwise, true is returned.
     * @param path The file path to validate.
     * @return true if the file path is valid, false otherwise.
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
     * 
     * This method takes a string representing the desired output format and validates it.
     * If the format is invalid, it prints an error message and returns the default output format.
     * Otherwise, it returns the validated and normalized output format.
     * 
     * @param format The output format to validate (e.g. "text", "json")
     * @return The validated and normalized output format
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
     * 
     * If the output format is set to 'file', it writes the tokens to a file in the specified format.
     * Otherwise, it prints the tokens to the console in the specified format.
     * @param tokens The list of tokens to output.
     * @throws IOException If an error occurs while writing to the file.
     */
    
    /**
     * Processes the input file and generates output.
     * 
     * This method performs the following steps:
     * 1. Creates the output directory if needed.
     * 2. Creates a SourceReader for the input file.
     * 3. Tokenizes the source using a Lexer instance.
     * 4. Outputs the tokens to the console or file, depending on the output format preference.
     * 5. Prints a summary of the token analysis to the console.
     * 
     * @throws IOException If an error occurs while processing the file.
     */
    private void processFile() throws IOException {
        // 1. Create output directory if needed
        if (outputToFile) {
            createOutputDirectory();
        }
        
        try (SourceReader reader = new SourceReader(filePath, StandardCharsets.UTF_8)) {
            // 2. Create lexer and tokenize
            List<Token> tokens = tokenizeSource(reader);
            
            // 3. Output tokens
            outputTokens(tokens);
            
            // 4. Print token summary
            printTokenSummary(tokens);
        } catch (Exception e) {
            throw new IOException("Error processing file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tokenizes the source using the lexer.
     * 
     * This method creates a Lexer instance and uses it to tokenize the source code.
     * It then handles any lexical errors that may have occurred during tokenization
     * by reporting them to the error handler.
     * Finally, it returns the list of tokens generated by the lexer.
     * 
     * @param reader The source reader for the file.
     * @return A list of tokens generated by the lexer.
     * @throws IOException If an error occurs during tokenization.
     */
    private List<Token> tokenizeSource(SourceReader reader) throws IOException {
        try {
            Lexer lexer = createLexer(reader);
            List<Token> tokens = lexer.tokenize();
            handleLexicalErrors(lexer);
            return tokens;
        } catch (Exception e) {
            // Wrap the exception in an IOException with a descriptive message
            throw new IOException("Error during tokenization: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a new Lexer instance.
     * 
     * @param reader The source reader for the file.
     * @return A new Lexer instance.
     */
    private Lexer createLexer(SourceReader reader) {
        // Create a Lexer instance for tokenization
        return new Lexer(reader);
    }

    
    /**
     * Validates and creates the output directory if it does not exist.
     * 
     * @throws IOException If an error occurs while creating the output directory.
     */
    private void createOutputDirectory() throws IOException {
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            // Create the directories if they do not exist
            Files.createDirectories(outputPath);
        }
    }
    
    private void outputTokens(List<Token> tokens) throws IOException {
        if (outputToFile) {
            writeTokensToFile(tokens);
        } else {
            if ("json".equals(outputFormat)) {
                // Print tokens in JSON format to console
                printTokensAsJson(tokens);
            } else {
                // Print tokens in text format to console
                printTokensAsText(tokens);
            }
        }
    }
    
    /**
     * Prints tokens in text format to console.
     * 
     * This method takes a list of tokens and prints them to the console in a human-readable format.
     * The tokens are filtered to only include those that should be printed, as determined by the
     * shouldPrintToken method.
     * 
     * @param tokens The list of tokens to print.
     */
    private void printTokensAsText(List<Token> tokens) {
        System.out.println("\nTokens:");
        System.out.println(Token.header());
        // Print only the tokens that should be printed
        tokens.stream()
            .filter(token -> shouldPrintToken(token))
            .forEach(System.out::println);
    }
    
    /**
     * Prints tokens in JSON format to console.
     * 
     * This method takes a list of tokens, filters out the ones that shouldn't be printed (as determined by the
     * shouldPrintToken method), converts them to JSON format using the tokenToJson method, and prints them
     * to the console as a JSON array.
     */
    private void printTokensAsJson(List<Token> tokens) {
        System.out.println("\nTokens (JSON):");
        System.out.println("[");
        
        // Filter out tokens that shouldn't be printed
        String jsonTokens = tokens.stream()
            .filter(this::shouldPrintToken)
            .map(this::tokenToJson)
            .collect(Collectors.joining(",\n"));
            
        System.out.println(jsonTokens);
        System.out.println("]");
    }
    
    /**
     * Writes tokens to a file in the specified format.
     * 
     * This method takes the base name of the input file, strips off any file extension, and appends
     * "_output.<extension>" to it to form the output file path. The extension is determined by the
     * outputFormat parameter, which can be either "json" or "txt". The tokens are written to the file
     * using the appropriate writeTokensAsXxx method.
     * 
     * @param tokens The list of tokens to write to the file.
     * @throws IOException If an error occurs while writing to the file.
     */
    private void writeTokensToFile(List<Token> tokens) throws IOException {
        // Get the base name of the input file
        String baseName = Paths.get(filePath).getFileName().toString();
        if (baseName.contains(".")) {
            // Strip off any file extension
            baseName = baseName.substring(0, baseName.lastIndexOf("."));
        }
        
        // Determine the output file extension based on the format
        String extension = "json".equals(outputFormat) ? "json" : "txt";
        Path outputPath = Paths.get(OUTPUT_DIR, baseName + "_output." + extension);
        
        // Write the tokens to the file
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
     * 
     * This method takes a list of tokens and a FileWriter object, and writes the tokens to the file
     * in a human-readable format. It first writes the header to the file, and then iterates over
     * the tokens, writing each one to the file if it should be printed (as determined by the
     * shouldPrintToken method).
     * 
     * @param tokens The list of tokens to write to the file.
     * @param writer The FileWriter object to write to.
     * @throws IOException If an error occurs while writing to the file.
     */
    private void writeTokensAsText(List<Token> tokens, FileWriter writer) throws IOException {
        // Write the header to the file
        writer.write(Token.header() + "\n");
        
        // Iterate over the tokens and write each one to the file if it should be printed
        for (Token token : tokens) {
            if (shouldPrintToken(token)) {
                writer.write(token.toString() + "\n");
            }
        }
    }
    
    /**
     * Writes tokens in JSON format to a file.
     * 
     * This method takes a list of tokens and a FileWriter object, and writes the tokens to the file
     * in JSON format. It first writes the opening bracket to the file, and then iterates over the
     * tokens, writing each one to the file if it should be printed (as determined by the
     * shouldPrintToken method). It adds a comma after each token, unless it is the last token in the
     * list. Finally, it writes the closing bracket to the file.
     * 
     * @param tokens The list of tokens to write to the file.
     * @param writer The FileWriter object to write to.
     * @throws IOException If an error occurs while writing to the file.
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
     * @param token The token to convert
     * @return The token in JSON format
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
     * Determines whether a token should be included in output. This decision is based on the
     * value of the verbose flag. If the verbose flag is set to true, all tokens are included in
     * the output. Otherwise, only tokens that are not of type WHITESPACE, COMMENT, or EOF are
     * included in the output. This is useful for debugging purposes, as it allows you to see
     * all tokens that are produced during lexical analysis.
     * @param token The token to check
     * @return true if the token should be included in output, false otherwise
     */
    private boolean shouldPrintToken(Token token) {
        return verbose || 
                (token.getType() != TokenType.WHITESPACE && 
                token.getType() != TokenType.COMMENT && 
                token.getType() != TokenType.EOF);
    }
    
    /**
     * Prints a summary of token counts by type.
     * 
     * This method takes a list of tokens, groups them by their type, 
     * and counts the occurrences of each type. It then prints a formatted 
     * summary to the console, showing the count of each token type.
     * 
     * @param tokens The list of tokens to summarize.
     */
    private void printTokenSummary(List<Token> tokens) {
        // Group tokens by their type and count the occurrences of each type
        Map<TokenType, Long> tokenSummary = tokens.stream()
            .collect(Collectors.groupingBy(Token::getType, Collectors.counting()));
        
        // Print the token summary header
        System.out.println("\nToken Summary:");
        
        // Iterate through the token summary and print each type with its count
        tokenSummary.forEach((type, count) -> 
            System.out.printf("%-20s : %d%n", type, count));
    }
    
    /**
     * Handles and displays lexical errors from the lexer.
     * 
     * This method retrieves the error handler from the given lexer 
     * and checks if any errors have been recorded. If errors are present, 
     * it outputs them in the specified format (JSON or plain text) and 
     * prints error statistics showing the count of each error type.
     * 
     * @param lexer The lexer instance containing the error handler.
     */
    private void handleLexicalErrors(Lexer lexer) {
        // Retrieve the error handler from the lexer
        ErrorHandler errorHandler = lexer.getErrorHandler();
        
        // Check if there are any recorded errors
        if (errorHandler.hasErrors()) {
            // Output errors in JSON format if specified
            if ("json".equals(outputFormat)) {
                printErrorsAsJson(errorHandler.getErrors());
            } else {
                // Print errors in plain text format
                errorHandler.printErrors();
            }
            
            // Print error statistics header
            System.err.println("\nError Statistics:");
            
            // Group and count errors by their type
            Map<ErrorHandler.ErrorType, Long> errorStats = errorHandler.getErrors().stream()
                .collect(Collectors.groupingBy(error -> error.getType(), Collectors.counting()));
                
            // Iterate through error statistics and print each type with its count
            errorStats.forEach((type, count) -> 
                System.err.printf("%-25s : %d%n", type, count));
        }
    }
    
    /**
     * Prints errors in JSON format.
     */
    private void printErrorsAsJson(List<ErrorHandler.LexicalError> errors) {
        // Start JSON array
        System.err.println("\nErrors (JSON):");
        System.err.println("[");
        
        // Iterate through errors and print each as a JSON object
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
                // Error type
                error.getType(),
                // Error message
                escapeJsonString(error.getMessage()),
                // Line and column of the error
                error.getLine(),
                error.getColumn(),
                // Suggestion if available
                error.getSuggestion() != null && !error.getSuggestion().isEmpty() 
                    ? String.format(",\n    \"suggestion\": \"%s\"", escapeJsonString(error.getSuggestion()))
                    : "",
                // Comma separator if not last element
                i < errors.size() - 1 ? "," : ""
            );
        }
        
        // End JSON array
        System.err.println("]");
    }
    
    /**
     * Escapes special characters in JSON strings.
     * 
     * This method takes an input string and returns a new string where special
     * characters are replaced with their corresponding escape sequences. This
     * is useful to ensure that the JSON data is properly formatted and
     * transmitted without errors.
     * 
     * @param input The input string to be escaped.
     * @return The escaped JSON string.
     */
    private String escapeJsonString(String input) {
        // If the input is null, return an empty string
        if (input == null) {
            return "";
        }
        
        // StringBuilder to accumulate the escaped result
        StringBuilder escaped = new StringBuilder();
        
        // Iterate over each character in the input
        for (char ch : input.toCharArray()) {
            // Append the corresponding escape sequence or the character itself
            escaped.append(switch (ch) {
                case '"' -> "\\\"";   // Escape double quotes
                case '\\' -> "\\\\";  // Escape backslashes
                case '\b' -> "\\b";   // Escape backspace
                case '\f' -> "\\f";   // Escape form feed
                case '\n' -> "\\n";   // Escape newline
                case '\r' -> "\\r";   // Escape carriage return
                case '\t' -> "\\t";   // Escape tab
                // Escape non-printable ASCII characters using unicode
                default -> ch < ' ' || ch > '~' ? String.format("\\u%04x", (int) ch) : ch;
            });
        }
        // Return the escaped string
        return escaped.toString();
    }
    
    /**
     * Cleans up resources by closing the scanner if it is not null.
     * 
     * This method ensures that the scanner resource is properly closed to 
     * prevent resource leaks. It checks if the scanner is not null before 
     * attempting to close it.
     */
    private void cleanup() {
        // Check if the scanner is not null
        if (scanner != null) {
            // Close the scanner to release resources
            scanner.close();
        }
    }
}