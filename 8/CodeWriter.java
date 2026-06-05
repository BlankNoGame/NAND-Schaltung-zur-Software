import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeWriter {

  private List<String> start2VarArithmetic = new ArrayList<>(List.of("add", "sub", "and", "or"));
  private List<String> start1VarArithmetic = new ArrayList<>(List.of("not", "neg"));


  BufferedWriter writer;
  private int labelCounter = 0;
  private int returnCounter = 0;
  private String currentFunction = "Sys.init";
  private String currentFileName = "";

  public CodeWriter(File output, int fileCount) throws IOException {
    this.writer = new BufferedWriter(new FileWriter(output));
    if (fileCount != 1) {
      writeInit();
    }
  }

  public void setFileName(String fileName) {
    currentFileName = fileName;
  }

  private void writeInit() throws IOException {

    writer.write("""
        @256
        D=A
        @SP
        M=D
        """);
    writer.newLine();
    writeCall("Sys.init", 0);

  }

  public void writeArithmetic (String command) throws IOException {
    writer.newLine();
    if (start2VarArithmetic.contains(command)) {
      writer.write("""
          @SP
          AM=M-1
          D=M
          A=A-1
          """);
      writer.newLine();

      switch(command) {
        case "add":
          writer.write("""
              M=M+D
              """);
          break;
        case "sub":
          writer.write("""
              M=M-D
              """);
          break;
        case "and":
          writer.write("""
              M=M&D
              """);
          break;
        case "or":
          writer.write("""
              M=M|D
              """);
          break;
      }
    }
    else if (start1VarArithmetic.contains(command)) {
      writer.write("""
          @SP
          A=M-1
          """);
      writer.newLine();

      switch(command) {
        case "not":
          writer.write("""
              M=!M
              """);
          break;
        case "neg":
          writer.write("""
              M=-M
              """);
          break;
      }
    } else {
      switch (command) {
        case "eq":
          writeLogic("JEQ");
          break;

        case "gt":
          writeLogic("JGT");
          break;

        case "lt":
          writeLogic("JLT");
          break;
      }
    }
  }

  public void WritePushPop (Command command, String segment, int index) throws IOException {
    if (command == Command.C_PUSH) {
      switch (segment) {
        case "constant" -> writer.write("""
            @""" + index + """
            
            D=A
            @SP
            A=M
            M=D
            @SP
            M=M+1
            
            """);
        case "temp", "static" -> writer.write("""
            @""" + translateSegToSymbol(segment, index) + """
            
            D=M
            @SP
            A=M
            M=D
            @SP
            M=M+1
            
            """);
        case "pointer" -> {
          String pointer = (index == 0) ? "THIS" : "THAT";

          writer.write("""
            @""" + pointer + """
            
            D=M
            @SP
            A=M
            M=D
            @SP
            M=M+1
            
            """);
        }
        default -> writer.write("""
            @""" + index + """
            
            D=A
            @""" + translateSegToSymbol(segment, index) + """
            
            D=M+D
            A=D
            D=M
            @SP
            A=M
            M=D
            @SP
            M=M+1
            
            """);
      }
    }
    else if (command == Command.C_POP) {
      if (segment.equals("pointer")) {
        String pointer = (index == 0) ? "THIS" : "THAT";

        writer.write("""
            @SP
            AM=M-1
            D=M
            @""" + pointer + """
            
            M=D
            
            """);
      }
      else if (segment.equals("temp") || segment.equals("static")) {
        writer.write("""
            @SP
            AM=M-1
            D=M
            @""" + translateSegToSymbol(segment, index) + """
            
            M=D
            
            """);
      } else {
        writer.write("""
            @""" + index + """
            
            D=A
            @""" + translateSegToSymbol(segment, index) + """
            
            D=M+D
            @R13
            M=D
            
            @SP
            AM=M-1
            D=M
            
            @R13
            A=M
            M=D
            
            """);
      }
    }
    writer.newLine();
  }

  private String translateSegToSymbol(String segment, int index) {
    return switch (segment) {
      case "local" -> "LCL";
      case "argument" -> "ARG";
      case "this" -> "THIS";
      case "that" -> "THAT";
      case "constant" -> index + "";
      case "temp" -> {
        int temp = 5 + index;
        yield temp + "";
      }
      case "static" -> currentFileName + "." + index;
      default -> throw new IllegalStateException("Unexpected value: " + segment);
    };
  }

  private void writeLogic(String jumpType) throws IOException {

    String trueLabel = "TRUE_" + labelCounter;
    String endLabel = "END_" + labelCounter;
    labelCounter++;

    writer.write("""
        @SP
        AM=M-1
        D=M
        A=A-1
        D=M-D
        
        @""" + trueLabel + """
        
        D;""" + jumpType + """
        
        @SP
        A=M-1
        M=0
        
        @""" + endLabel + """
        
        0;JMP
        
        (""" + trueLabel + """
        )
        
        @SP
        A=M-1
        M=-1
        
        (""" + endLabel + """
        )
        """);

  }

  private String scopedLabel (String label) {
    if (currentFunction.isEmpty()) {
      return label;
    }
    return currentFunction + "$" + label;
  }

  public void writeLabel(String label) throws IOException {
    writer.write("(" + scopedLabel(label) + ")" );
    writer.newLine();
  }

  public void writeGoto(String label) throws IOException {
    writer.write("@" + scopedLabel(label));
    writer.newLine();
    writer.write("0;JMP");
    writer.newLine();
  }

  public void writeIf(String label) throws IOException {
    writer.write("""
        @SP
        AM=M-1
        D=M
        
        @""" + scopedLabel(label) +
        """
        
        D;JNE
        """);
    writer.newLine();
  }

  public void writeFunction(String funcName, int nVars) throws IOException {
    currentFunction = funcName;

    writer.write("(" + funcName + ")");
    writer.newLine();

    for (int i = 0; i < nVars; i++) {
      writer.write("""
          @0
          D=A
          
          @SP
          A=M
          M=D
          
          @SP
          M=M+1
          """);
    }
    writer.newLine();
  }

  public void writeCall(String funcName, int nArgs) throws IOException {
    String returnLabel = currentFunction + "$ret." + returnCounter++;

    writer.write("""
        @""" + returnLabel + """
        
        D=A
        @SP
        A=M
        M=D
        @SP
        M=M+1
        
        @LCL
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        
        @ARG
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        
        @THIS
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        
        @THAT
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        
        @SP
        D=M
        @5
        D=D-A
        
        @""" + nArgs +
        """
        
        D=D-A
        
        @ARG
        M=D
        
        @SP
        D=M
         @LCL
        M=D
        
        @""" + funcName +
        """
        
        0;JMP
        
        (""" + returnLabel + """
        )""");

    writer.newLine();
  }

  public void writeReturn() throws IOException {

    writer.write("""
        @LCL
        D=M
        @R13
        M=D

        @5
        A=D-A
        D=M
        @R14
        M=D

        @SP
        AM=M-1
        D=M

        @ARG
        A=M
        M=D

        @ARG
        D=M+1
        @SP
        M=D

        @R13
        AM=M-1
        D=M
        @THAT
        M=D

        @R13
        AM=M-1
        D=M
        @THIS
        M=D

        @R13
        AM=M-1
        D=M
        @ARG
        M=D

        @R13
        AM=M-1
        D=M
        @LCL
        M=D

        @R14
        A=M
        0;JMP
        """);

    writer.newLine();

  }

  public void close() throws IOException {
    writer.close();
  }

}
