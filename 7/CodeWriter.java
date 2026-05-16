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

  public CodeWriter(File output) throws IOException {
    this.writer = new BufferedWriter(new FileWriter(output));
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
          writeVergleiche("JEQ");
          break;

        case "gt":
          writeVergleiche("JGT");
          break;

        case "lt":
          writeVergleiche("JLT");
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
      case "static" -> {
        int temp1 = 16 + index;
        yield temp1 + "";
      }
      default -> throw new IllegalStateException("Unexpected value: " + segment);
    };
  }

  private void writeVergleiche(String jumpType) throws IOException {

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

  public void close() throws IOException {
    writer.close();
  }

}
