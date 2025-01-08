package code.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Handles reading the source code, character by character, while keeping track 
 * of the current line and column position. 
 * Provides methods for character peeking, advancing, and error recovery.
 */
public class SourceReader {
    public static final char EOF = (char) -1;

    private BufferedReader reader;
    private int line = 1;
    private int column = 0; // Tracks the column position of the current character
    private char lastChar = EOF;

    /**
     * Constructs a new SourceReader instance.
     * 
     * @param filePath the path to the file to read
     * @param charset  the charset to use for reading the file
     * @throws SourceReaderException if an error occurs while reading the file
     */
    public SourceReader(String filePath, Charset charset) throws SourceReaderException {
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset));
        } catch (IOException e) {
            throw new SourceReaderException("Error opening file: " + filePath, e);
        }
    }

    /**
     * Reads the next character from the file and advances the position.
     * 
     * @return the next character, or EOF if the end of the file is reached
     * @throws SourceReaderException if an error occurs while reading the file
     */
    public char readNext() throws SourceReaderException {
        try {
            int read = reader.read();
            if (read == -1) {
                lastChar = EOF;
                return EOF;
            }

            lastChar = (char) read;

            if (lastChar == '\n') {
                line++;
                column = 0; // Reset column for the new line
            } else {
                column++;
            }

            return lastChar;
        } catch (IOException e) {
            throw new SourceReaderException("Error reading from file", e);
        }
    }

    /**
     * Peeks at the next character in the file without advancing the position.
     * 
     * @return the next character, or EOF if the end of the file is reached
     * @throws SourceReaderException if an error occurs while reading the file
     */
    public char peek() throws SourceReaderException {
        try {
            reader.mark(1); // Mark the current position
            int read = reader.read();
            reader.reset(); // Reset to the marked position
            return read == -1 ? EOF : (char) read;
        } catch (IOException e) {
            throw new SourceReaderException("Error peeking at file", e);
        }
    }

    /**
     * Returns the current line number.
     * 
     * @return the current line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the current character column within the line.
     * 
     * @return the current character column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the last character read, useful for debugging and context-sensitive logic.
     * 
     * @return the last character read
     */
    public char getLastChar() {
        return lastChar;
    }

    /**
     * Closes the underlying reader.
     * 
     * @throws SourceReaderException if an error occurs while closing the reader
     */
    public void close() throws SourceReaderException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new SourceReaderException("Error closing reader", e);
            }
        }
    }

    /**
     * Custom exception class for SourceReader errors.
     */
    public static class SourceReaderException extends Exception {
        public SourceReaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
