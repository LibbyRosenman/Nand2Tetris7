import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {
        // validate the input file
        if (args.length == 0) {
            System.out.println("Error: No command-line arguments provided");
        }
        String inputPath = args[0].trim();
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("Error: file or directory not found");
        }

        try {
            if (inputFile.isDirectory()) {
                // Input is a directory, process all files in the directory into a single file
                File[] files = inputFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".vm"));
                if (files != null) {
                    processDir(files);
                } else {
                    System.out.println("Error: Unable to list files in the directory");
                }
            } else if (inputFile.isFile()) {
                // Input is a single file
                processFile(inputFile);
            } else {
                System.out.println("Error: Invalid input");
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e);
        }
    }

    /**
     * this function translates a vm file to a new asm file
     * it creates a new asm file in the same folder
     * it constructs a parser to handle the input file and a code writer to handle
     * the ouputfile
     * then - iterates through the input file, parsing each line and generating code
     * from it
     * 
     * @param sourceFile
     * @throws IOException
     */
    private static void processFile(File sourceFile) throws IOException {
        // create the output file - same as the original path with .asm suffix
        String sourceAbsolutePath = sourceFile.getAbsolutePath();
        String fileName = sourceFile.getName();
        int fileNameExtensionIndex = fileName.lastIndexOf(".");
        String fileNameNoExtension = fileName.substring(0, fileNameExtensionIndex);
        int fileNameIndex = sourceFile.getAbsolutePath().indexOf(sourceFile.getName());
        String sourceDirectory = sourceAbsolutePath.substring(0, fileNameIndex);
        String outputFilePath = sourceDirectory + fileNameNoExtension + ".asm";
        File outputFile = new File(outputFilePath);

        // add the bootstrap to the output file
        Bootstrap(outputFile, sourceFile);

        // translating the vm file to the hack-assembly language
        translator(sourceFile, outputFile);
    }

    /**
     * this function takes a directory of vm files and translates them to one new
     * asm file
     * it creates a new asm file in the same folder
     *
     * @param dir - an array of files
     * @throws IOException
     */
    private static void processDir(File[] dir) throws IOException {
        // create the output file - same as the original path of the directory with .asm
        // suffix
        // Check if the directory is not empty
        if (dir.length == 0) {
            System.out.println("The directory is empty. No files to process.");
            return;
        }
        // Get the parent directory path
        String parentDir = dir[0].getParent();
        // Create the output file path by using the name of the parent directory
        String outputFileName = new File(parentDir).getName() + ".asm";
        String outputPath = parentDir + File.separator + outputFileName;
        // Create the output file
        File outputFile = new File(outputPath);
        outputFile.createNewFile();

        // add the bootstrap to the output file
        Bootstrap(outputFile, outputFile);

        // Process each VM file and write to the output ASM file
        for (File vmFile : dir) {
            // translating the vm file to the hack-assembly language
            translator(vmFile, outputFile);

        }
    }

    /**
     * this method writes the bootstrap of the file which initializes the operating
     * system
     * 
     * @param asmFile
     */
    public static void Bootstrap(File asmFile, File vmFile) {
        CodeWriter coder = new CodeWriter(asmFile,vmFile);
        coder.WriteBootstrap();
        coder.close();
    }

    /**
     * this function translates the vm commands into assembly commands.
     * it constructs a parser to handle the input file and a code writer to handle
     * the ouputfile
     * then - iterates through the input file, parsing each line and generating code
     * from it
     * 
     * @param vmFile  - input
     * @param asmFlie - output
     */
    public static void translator(File vmFile, File asmFile) throws FileNotFoundException, IOException {
        Parser parser = new Parser(vmFile);
        CodeWriter coder = new CodeWriter(asmFile, vmFile);

        while (parser.hasMoreLines()) {
            parser.advance();
            if (parser.commandType().equals(commandType.C_ARITHMETIC)) {
                coder.WriteArithmetic(parser.arg1());
            } else if (parser.commandType().equals(commandType.C_POP)
                    || parser.commandType().equals(commandType.C_PUSH)) {
                coder.WritePushPop(parser.commandType(), parser.arg1(), parser.arg2());
            } else if (parser.commandType().equals(commandType.C_CALL)) {
                coder.WriteCall(parser.arg1(), parser.arg2());
            } else if (parser.commandType().equals(commandType.C_FUNCTION)) {
                coder.WriteFunction(parser.arg1(), parser.arg2());
            } else if (parser.commandType().equals(commandType.C_LABEL)) {
                coder.WriteLabel(parser.arg1());
            } else if (parser.commandType().equals(commandType.C_GOTO)) {
                coder.WriteGoTo(parser.arg1());
            } else if (parser.commandType().equals(commandType.C_IF)) {
                coder.WriteIf(parser.arg1());
            } else if (parser.commandType().equals(commandType.C_RETURN)) {
                coder.WriteReturn();
            } else {
                throw new IOException("This is not a legal command");
            }
        }
        coder.close();

    }

}
