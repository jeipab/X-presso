package source.util;

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
    private int charPosition = 0;
    private int lastChar = -1;

    /**
     * Constructs a new SourceReader instance.
     * 
     * @param filePath the path to the file to read
     * @param charset  the charset to use for reading the file
     * @throws SourceReaderException if an error occurs while reading the file
     */
    public SourceReader(String filePath, Charset charset) throws SourceReaderException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            reader = new BufferedReader(new InputStreamReader(fileInputStream, charset));
        } catch (IOException e) {
            throw new SourceReaderException("Error opening file", e);
        }
    }

    /**
     * Reads the next character from the file.
     * 
     * @return the next character, or EOF if the end of the file is reached
     * @throws SourceReaderException if an error occurs while reading the file
     */
    public char readNext() throws SourceReaderException {
        try {
            lastChar = reader.read();
            if (lastChar == '\n') {
                line++;
                charPosition = 0;
            } else {
                charPosition++;
            }
            return lastChar == -1 ? EOF : (char) lastChar;
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
            reader.mark(1);
            lastChar = reader.read();
            reader.reset();
            return lastChar == -1 ? EOF : (char) lastChar;
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
     * Returns the current character position within the line.
     * 
     * @return the current character position
     */
    public int getColumn() {
        return charPosition;
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