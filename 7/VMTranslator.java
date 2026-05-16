import java.io.File;
import java.io.IOException;

public class VMTranslator {

  static void main(String[] args) {

    if (args.length != 1) {
      System.out.println("Keine Datei übergeben, Programm wird nicht ausgeführt");
    }

    String inputFile = args[0];
    String outputFile = inputFile.replace(".vm", ".asm");

    try {
      translate(inputFile, outputFile);
      System.out.println("Done: " + outputFile);
    } catch (Exception e) {
      System.err.println("Fehler: " + e.getMessage());
    }

  }

  public static void translate(String inputFile, String outputFile) throws IOException {

    File eingabeFile = new File(inputFile);
    File ausgabeFile = new File(outputFile);

    Parser parser = new Parser(eingabeFile);
    CodeWriter codeWriter = new CodeWriter(ausgabeFile);

    while(parser.hasMoreLines()) {
      Command command = parser.commandType();
      if (Command.C_ARITHMETIC == command) {
        codeWriter.writeArithmetic(parser.arg1());
      }
      else {
        codeWriter.WritePushPop(command, parser.arg1(), parser.arg2());
      }
      parser.advance();
    }
    codeWriter.close();
  }
}
