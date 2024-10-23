import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CodeWriter {

    private BufferedWriter writer;
    private int arthJumpFlag;
    private int retAddrFlag;
    private String push_sp;
    private String fileName;

    /**
     * constructor of the codeWriter. opens an output file / stream and gets ready
     * to
     * write into it
     * 
     * @param outputfile / stream
     */
    public CodeWriter(File outputFile, File vmFile) {
        try {
            // Create a FileWriter that writes to the specified file
            FileWriter fileWriter = new FileWriter(outputFile, true);
            arthJumpFlag = 0;
            retAddrFlag = 0;
            push_sp = "@SP\nA=M\nM=D\n//SP++\n@SP\nM=M+1\n";
            String fullFileName = vmFile.getName();
            int fileNameExtensionIndex = fullFileName.lastIndexOf(".");
            fileName = fullFileName.substring(0, fileNameExtensionIndex);

            // Wrap the FileWriter in a BufferedWriter for efficient writing
            writer = new BufferedWriter(fileWriter);
        } catch (IOException e) {
            // Handle the exception (e.g., print an error message or throw a custom
            // exception)
            e.printStackTrace();
        }
    }

    /**
     * this method writes the first lines of the file which initializes the
     * operating system
     */
    public void WriteBootstrap() {
        // SP = 256
        String line = "@256\nD=A\n@SP\nM=D\n";
        try {
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // call sys.init
        WriteCall("Sys.init", 0);
    }

    /**
     * this method writes to the output file the assembly code
     * that implements the given arithmetic-logical command.
     * 
     * @param command
     */
    public void WriteArithmetic(String command) {
        List<String> validCommands = Arrays.asList("add", "sub", "not", "and", "or", "neg", "eq", "lt", "gt");
        if (!validCommands.contains(command)) {
            throw new IllegalArgumentException("Call writeArithmetic() for a non-arithmetic command");
        }
        StringBuilder line = new StringBuilder();
        line.append("@SP\n");
        if (command.equals("neg")) {
            line.append("A=M-1\n");
            line.append("D=0\n");
            line.append("M=D-M\n");
        } else if (command.equals("not")) {
            line.append("A=M-1\n");
            line.append("M=!M\n");
        } else {
            line.append("AM=M-1\n");
            line.append("D=M\n");
            line.append("A=A-1\n");
            switch (command) {
                case "add":
                    line.append("M=M+D\n");
                    break;
                case "sub":
                    line.append("M=M-D\n");
                    break;
                case "and":
                    line.append("M=M&D\n");
                    break;
                case "or":
                    line.append("M=M|D\n");
                    break;
            }
            if ((command.equals("gt")) || (command.equals("lt")) || (command.equals("eq"))) {
                line.append("D=M-D\n");
                line.append("@TRUE" + arthJumpFlag + "\n");
                switch (command) {
                    case "gt":
                        line.append("D;JGT\n");
                        break;
                    case "lt":
                        line.append("D;JLT\n");
                        break;
                    case "eq":
                        line.append("D;JEQ\n");
                        break;
                }
                // if not jump then false:
                line.append("@SP\n");
                line.append("A=M-1\n");
                line.append("M=0\n");// 0=FALSE
                line.append("@CONT" + arthJumpFlag + "\n");// CONTINUE
                line.append("0;JMP\n");// CONTINUE
                // jumped, so true
                line.append("(TRUE" + arthJumpFlag + ")\n");
                line.append("@SP\n");
                line.append("A=M-1\n");
                line.append("M=-1\n");// -1=TRUE
                line.append("@CONT" + arthJumpFlag + "\n");// CONTINUE
                line.append("0;JMP\n");// CONTINUE
                line.append("(CONT" + arthJumpFlag + ")\n");// CONTINUE
                arthJumpFlag++;
            }

        }
        // write line to output file:
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * this method writes to the output file the assembly code
     * that implements the given push or pop command.
     * 
     * @param command
     * @param segment
     * @param index
     */
    public void WritePushPop(commandType command, String segment, int index) {
        StringBuilder line = new StringBuilder();
        String segmentPointer = segment; // changed to sement from empty string
        switch (segment) {
            case "local":
                segmentPointer = "LCL";
                break;
            case "argument":
                segmentPointer = "ARG";
                break;
            case "this":
                segmentPointer = "THIS";
                break;
            case "that":
                segmentPointer = "THAT";
                break;
            case "temp":
                segmentPointer = "5";
                break;
            case "pointer":
                if (index == 0) {
                    segmentPointer = "THIS";
                } else {
                    segmentPointer = "THAT";
                }
                break;

        }
        if ((!segment.equals("constant")) && (!segment.equals("static")) && (!segment.equals("pointer"))) {
            // addr = segmpentpointer+i
            line.append("// D = " + segmentPointer + "+" + index + "\n"); // add a comment line: D=segmpentpointer+i
            line.append("@" + segmentPointer + "\n"); // @segmentpointer
            if (segment.equals("temp")) {
                line.append("D=A\n"); // @i
            } else {
                line.append("D=M\n"); // @i
            }

            line.append("@" + index + "\n"); // @i

            line.append("D=D+A\n"); // D=D+A
        }

        // push command
        if (command.equals(commandType.C_PUSH)) {
            if (segment.equals("constant")) {
                line.append("// D = " + index + "\n"); // add a comment line D = index
                line.append("@");// @i
                line.append(index);
                line.append("\n");
                line.append("D=A\n"); // D=A
                line.append("//RAM[SP]=D\n"); // add a comment line RAM[SP]=D
            } else if (segment.equals("static")) {
                line.append("@" + (fileName + "." + index) + "\n"); // @16+i
                line.append("D=M\n"); // D=M
            } else if (segment.equals("pointer")) {
                line.append("@" + segmentPointer + "\n"); // @16+i
                line.append("D=M\n"); // D=M
            } else {
                line.append("//RAM[SP] = RAM[D]\n"); // add a comment line: RAM[SP] = RAM[D]
                line.append("A=D\n"); // A=D
                line.append("D=M\n"); // D=M

            }
            // "@SP\nA=M\nM=D\n//SP++\n@SP\nM=M+1\n";
            line.append(push_sp);
        }

        // pop command
        if (command.equals(commandType.C_POP)) {
            if (segment.equals("static")) {
                line.append("@" + (fileName + "." + index) + "\n"); // @FileName.+i
                line.append("D=A\n"); // D=M
            } else if (segment.equals("pointer")) {
                line.append("@" + segmentPointer + "\n"); // @pointer
                line.append("D=A\n"); // D=M
            }

            // store the address in R13
            line.append("//R13=D\n"); // add a comment line: R13=D
            line.append("@R13\n"); // @R13
            line.append("M=D\n"); // M=D
            line.append("//SP--\n"); // add a comment line SP++
            line.append("@SP\n"); // @SP
            line.append("AM=M-1\n"); // AM=M-1
            line.append("//RAM[R13] = RAM[SP]\n"); // add a comment line: RAM[R13] = RAM[SP]
            line.append("D=M\n"); // D=M
            line.append("@R13\n"); // @R13
            line.append("A=M\n"); // A=M
            line.append("M=D\n"); // M=D

        }
        // write line to output file:
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this methods translates the Label command and writes it into the asmFile
     * 
     * @param label
     */
    public void WriteLabel(String label) {
        // (label)
        String line = "(" + label + ")\n";
        try {
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this methods translates the GOTO command and writes it into the asmFile
     * 
     * @param label
     */
    public void WriteGoTo(String label) {
        // @label
        // 0;JMP
        StringBuilder line = new StringBuilder();
        line.append("//goto label\n");
        line.append("@" + label + "\n");
        line.append("0;JMP\n");
        // write line to output file:
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this methods translates the IF-GOTO command and writes it into the asmFile
     * 
     * @param label
     */
    public void WriteIf(String label) {
        // @SP
        // M=M-1
        // A=M
        // D=M
        // @label
        // D;JNE
        StringBuilder line = new StringBuilder();
        line.append("//D=RAM[SP-1]\n");
        line.append("@SP\n");
        line.append("M=M-1\n");
        line.append("A=M\n");
        line.append("D=M\n");
        line.append("//if (D>0) goto label\n");
        line.append("@" +  label + "\n");
        line.append("D;JNE\n");
        // write line to output file:
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this methods translates the CALL command and writes it into the asmFile
     * 
     * @param segment: function name
     * @param Nargs    : number of args
     */
    public void WriteCall(String segment, int Nargs) {
        StringBuilder line = new StringBuilder();
        String retAddr = (segment + "$ret." + retAddrFlag);
        retAddrFlag++;
        line.append("//push " + retAddr + "\n"); // add a comment
        line.append("@" + retAddr + "\n" + "D=A\n");
        line.append(push_sp);

        String[] addresses = { "@LCL", "@ARG", "@THIS", "@THAT" };

        for (String address : addresses) {
            line.append("//push " + address + "\n"); // add a comment
            line.append(address + "\n");
            line.append("D=M\n");
            line.append(push_sp);
        }

        // ARG = SP-5-Nargs
        line.append("// ARG = SP-5-Nargs\n");
        String arg = "@SP\n" +
                "D=M\n" +
                "@5\n" +
                "D=D-A\n" +
                "@" + Nargs + "\n" +
                "D=D-A\n" +
                "@ARG\n" +
                "M=D\n";
        line.append(arg);

        // LCL=SP
        line.append("// LCL=SP\n");
        String lcl = "@SP\n" +
                "D=M\n" +
                "@LCL\n" +
                "M=D\n";
        line.append(lcl);

        // goto functionName
        line.append("//goto functionName\n");
        line.append("@" + segment + "\n");
        line.append("0;JMP\n");
        // write line to output file:
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // (retAddrLabel)
        this.WriteLabel(retAddr);
    }

    /**
     * this methods translates the FUNCTION command and writes it into the asmFile
     * 
     * @param segment: the name of the function
     * @param Nargs    : the number of args
     */
    public void WriteFunction(String segment, int Nargs) {
        // add a comment
        String comment = "// function " + segment + " number of args: " + Nargs + "\n";
        try {
            writer.write(comment);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // print label with the name of the function
        this.WriteLabel(segment);
        // initializes the local variables to 0;
        StringBuilder lines = new StringBuilder();

        if (Nargs > 0) {
            lines.append("//initializes the local variables to 0\n");
            lines.append("@LCL\n");
            lines.append("A=M\n");
        }
        String push0nArgs = "M=0\nA=A+1\nD=A\n@SP\nM=M+1\nA=D\n";
        for (int i = 0; i < Nargs; i++) {
            lines.append(push0nArgs);
        }
        // write line to output file:
        try {
            writer.write(lines.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this methods translates the RETURN command and writes it into the asmFile
     */
    public void WriteReturn() {
        // endFrame = LCL
        // retAddr = *(endFrame-5)
        // *ARG = pop()
        // SP = ARG+1
        // THAT=*(endFrame-1)
        // THIS=*(endFrame-2)
        // ARG=*(endFrame-3)
        // LCL=*(endFrame-4)
        // goto retAddr
        StringBuilder line = new StringBuilder();
        line.append("//return\n");
        // endFrame (R13) = LCL
        String command = "//endFrame=LCL\n" +
                "@LCL\n" +
                "D=M\n" +
                "@R13\n" +
                "M=D\n";
        line.append(command);
        // retAddr (R14) = *(endFrame-5)
        command = "//retAddr=*(endFrame-5)\n" +
                "@R13\n" +
                "D=M\n" +
                "@5\n" +
                "D=D-A\n" +
                "A=D\n" +
                "D=M\n" +
                "@R14\n" +
                "M=D\n";
        line.append(command);
        // *ARG = pop()
        command = "// *ARG=pop()\n" +
                "@SP\n" +
                "M=M-1\n" +
                "A=M\n" +
                "D=M\n" +
                "@ARG\n" +
                "A=M\n" +
                "M=D\n";
        line.append(command);
        // SP = ARG+1
        command = "//SP=ARG+1\n" +
                "@ARG\n" +
                "D=M\n" +
                "@SP\n" +
                "M=D+1\n";
        line.append(command);
        // THAT=*(endFrame-1)
        command = "//THAT=*(endFrame-1)\n" +
                "@R13\n" +
                "D=M\n" +
                "@1\n" +
                "D=D-A\n" +
                "A=D\n" +
                "D=M\n" +
                "@THAT\n" +
                "M=D\n";
        line.append(command);
        // THIS=*(endFrame-2)
        command = "//THIS=*(endFrame-2)\n" +
                "@R13\n" +
                "D=M\n" +
                "@2\n" +
                "D=D-A\n" +
                "A=D\n" +
                "D=M\n" +
                "@THIS\n" +
                "M=D\n";
        line.append(command);
        // ARG=*(endFrame-3)
        command = "//ARG=*(endFrame-3)\n" +
                "@R13\n" +
                "D=M\n" +
                "@3\n" +
                "D=D-A\n" +
                "A=D\n" +
                "D=M\n" +
                "@ARG\n" +
                "M=D\n";
        line.append(command);
        // LCL=*(endFrame-4)
        command = "//ARG=*(endFrame-3)\n" +
                "@R13\n" +
                "D=M\n" +
                "@4\n" +
                "D=D-A\n" +
                "A=D\n" +
                "D=M\n" +
                "@LCL\n" +
                "M=D\n";
        line.append(command);
        // goto retAddr
        command = "//goto retAddr\n" +
                "@R14\n" +
                "A=M\n" +
                "0;JMP\n";
        line.append(command);
        // write line to output file:
        try {
            writer.write(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method closes the output file.
     * 
     */
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
