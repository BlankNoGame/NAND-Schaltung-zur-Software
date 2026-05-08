import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

  private String currentInstruction;

  private BufferedReader reader;

  public static final int A_INSTRUCTION = 0;
  public static final int C_INSTRUCTION = 1;
  public static final int L_INSTRUCTION = 2;

  public Parser (File file) throws IOException {
    this.reader = new BufferedReader(new FileReader(file));
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

      int commentPräfixIndex = line.indexOf("//");

      if(commentPräfixIndex != -1) line = line.substring(0, commentPräfixIndex).trim();

      if (line.isEmpty()) continue;

      this.currentInstruction = line;
      return;
    }
    this.currentInstruction = null;
  }

  public int instructionType() {

    if (currentInstruction == null) {
      throw new IllegalStateException("keine Instruktion geladen");
    }

    if (currentInstruction.startsWith("@")) {
      return A_INSTRUCTION;
    } else if(currentInstruction.startsWith("(")) {
      return L_INSTRUCTION;
    } else {
      return C_INSTRUCTION;
    }
  }

  public String symbol() {

    int type = this.instructionType();

    if (type == C_INSTRUCTION) {
      throw new IllegalStateException("Kein Symbol erkannt");
    } else if (type == L_INSTRUCTION) {
      return currentInstruction.substring(1,currentInstruction.length() - 1);
    } else {
      return currentInstruction.substring(1);
    }
  }

  public String dest() {

    int equalsIndex = currentInstruction.indexOf('=');

    if (equalsIndex == -1) {
      return null;
    }

    String  destinationPart = currentInstruction.substring(0, equalsIndex).trim();

    return destinationPart.isEmpty() ? null : destinationPart;
  }

  public String comp() {

    int equalsIndex = currentInstruction.indexOf('=');
    int semiIndex = currentInstruction.indexOf(';');

    String compPart;

    if (equalsIndex != -1) {
      if (semiIndex != -1) {
        compPart = currentInstruction.substring(equalsIndex + 1, semiIndex).trim();
      } else {
        compPart = currentInstruction.substring(equalsIndex + 1).trim();
      }
    } else if (semiIndex != -1) {
      compPart = currentInstruction.substring(0, semiIndex).trim();
    } else {
      compPart = currentInstruction.trim();
    }
    return compPart.isEmpty() ? null : compPart;
  }

  public String jump() {

    int semiIndex = currentInstruction.indexOf(';');

    if (semiIndex == -1) {
      return null;
    }

    String jumpPart = currentInstruction.substring(semiIndex + 1).trim();

    return jumpPart.isEmpty() ? null : jumpPart;
  }

  public void close() throws IOException {
    if (reader != null) reader.close();
  }

}
