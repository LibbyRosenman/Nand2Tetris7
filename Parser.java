import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Parser {
    private BufferedReader reader;
    private String currentLine;

    /**
     * contructor of the parser
     * 
     * @param source
     * @throws FileNotFoundException
     */
    public Parser(File source) throws FileNotFoundException {
        if (source == null) {
            throw new NullPointerException("source");
        }
        if (!source.exists()) {
            throw new FileNotFoundException(source.getAbsolutePath());
        }
        this.reader = new BufferedReader(new FileReader(source));
        this.currentLine = null;
    }

    /**
     * checks if there are more lines in the assembly file
     * 
     * @return true or false
     * @throws IOException
     */
    public boolean hasMoreLines() throws IOException {
        if ((this.currentLine = this.reader.readLine()) != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * this method moves the pointer to the next line
     * this method is called only if hasMoreLines() is true
     * this method handles empty lines and documentation lines
     */
    public void advance() throws IOException {
        while (currentLine == null || currentLine.trim().isEmpty() || currentLine.trim().startsWith("//")) {
            currentLine = this.reader.readLine();
            if (currentLine == null) {
                break; // exit the loop if end of file is reached
            }
        }
    }

    /**
     * this method returns the type of the current command
     * by matching the prefix of the line to the corresponding command.
     * 
     * @return C_ARITHMETIC | C_PUSH | C_POP | C_LABEL | C_GOTO | C_IF | C_FUNCTION
     *         | C_RETURN | C_CALL
     */
    public commandType commandType() {
        if (currentLine.trim().startsWith("pop")) {
            return commandType.C_POP;
        } else if (currentLine.trim().startsWith("push")) {
            return commandType.C_PUSH;
        } else if (arithmetic(currentLine)) {
            return commandType.C_ARITHMETIC;
        } else if (currentLine.trim().startsWith("label")) {
            return commandType.C_LABEL;
        } else if (currentLine.trim().startsWith("goto")) {
            return commandType.C_GOTO;
        } else if (currentLine.trim().startsWith("if")) {
            return commandType.C_IF;
        } else if (currentLine.trim().startsWith("function")) {
            return commandType.C_FUNCTION;
        } else if (currentLine.trim().startsWith("call")) {
            return commandType.C_CALL;
        } else {
            return commandType.C_RETURN;
        }
    }

    /**
     * this helper function verifies whether the current line is an arithmetic
     * command
     * or not by checking the prefix.
     * 
     * @param line
     * @return true or false
     */
    public static boolean arithmetic(String line) {
        if (line.trim().startsWith("add")) {
            return true;
        } else if (line.trim().startsWith("sub")) {
            return true;
        } else if (line.trim().startsWith("neg")) {
            return true;
        } else if (line.trim().startsWith("eq")) {
            return true;
        } else if (line.trim().startsWith("gt")) {
            return true;
        } else if (line.trim().startsWith("lt")) {
            return true;
        } else if (line.trim().startsWith("and")) {
            return true;
        } else if (line.trim().startsWith("or")) {
            return true;
        } else if (line.trim().startsWith("not")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * this method returns the first argument of the current command.
     * In the case of C_ARITHMETIC the command itself (add, sub, etc.) is returnd.
     * 
     * @return string
     */
    public String arg1() {
        currentLine = currentLine.trim();
        String[] args = currentLine.split(" ");
        if (commandType().equals(commandType.C_ARITHMETIC)) {
            // if the command is arithmetic - return the command itself
            return args[0];
        }
        // else, return the next word (=non space string) after the command
        return args[1];
    }

    /**
     * this method returns the second argument of the current command.
     * Should be called if the current command is C_PUSH, C_POP, C_FUNCTION, C_CALL.
     * 
     * @return int
     */
    public int arg2() {
        // find the second "word" after the command itself
        currentLine = currentLine.trim();
        String[] args = currentLine.split("\\s+");
        String answer = args[2].trim();

        // Check if the answer is numeric before parsing
        if (isNumeric(answer)) {
            return Integer.parseInt(answer);
        } else {
            // Handle the case where the string is not numeric (e.g., log an error, return a
            // default value, etc.)
            System.err.println("Invalid numeric input: " + answer);
            return 0; // or another default value
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }
}
