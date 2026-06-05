import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

  private BufferedReader reader;

  private String currentInstruction;

  private Command currentCommand;

  public Parser(File input) throws IOException {
    this.reader = new BufferedReader(new FileReader(input));
    this.currentInstruction = null;
    advance();
  }

  public boolean hasMoreLines() {
    return currentInstruction != null;
  }

  public void advance() throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();

      if(line.isEmpty()) continue;

      int commentPräfix = line.indexOf("//");

      if(commentPräfix != -1) line = line.substring(0, commentPräfix).trim();

      if(line.isEmpty()) continue;

      this.currentInstruction = line;
      this.currentCommand = commandType();
      return;
    }
    this.currentInstruction = null;
  }

  public Command commandType() {

    if (currentInstruction == null) {
      throw new IllegalStateException("keine Kommand geladen");
    } else {

      String firstPart = currentInstruction.split(" ")[0];

      switch(firstPart) {
        case "push":
          return Command.C_PUSH;
        case "pop":
          return Command.C_POP;
        case "function":
          return Command.C_FUNCTION;
        case "call":
          return Command.C_CALL;
        case "return":
          return Command.C_RETURN;
        case "label":
          return Command.C_LABEL;
        case "goto":
          return Command.C_GOTO;
        case "if-goto":
          return Command.C_IF;
        default:
          return Command.C_ARITHMETIC;
      }

    }

  }

  public String arg1() {
    int space = currentInstruction.indexOf(" ");
    if (space == -1) {
      return currentInstruction;
    } else {
      String[] words = currentInstruction.split(" ");
      if (currentCommand == Command.C_ARITHMETIC) {
        return words[0];
      } else {
        return words[1];
      }
    }
  }

  public int arg2() {
    if (currentCommand == Command.C_ARITHMETIC) {
      throw new IllegalStateException("Kein zweites Argument in Arithmetic calls");
    }
    String[] commandWords = currentInstruction.split(" ");

    return Integer.parseInt(commandWords[commandWords.length - 1]);
  }

}
