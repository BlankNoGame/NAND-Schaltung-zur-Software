import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Keine Datei übergeben, braucht genau eine Datei um zu funktionieren");
    }

    String inputFile = args[0];
    String outputFile = inputFile.replace(".asm", ".hack");

    try {
      assemble(inputFile, outputFile);
      System.out.println("Done: " + outputFile);
    } catch (Exception e) {
      System.err.println("Fehler: " + e.getMessage());
    }

  }

  private static void assemble (String inputFile, String outputFile) throws IOException {

    File eingabeFile = new File(inputFile);
    File ausgabeFile = new File(outputFile);

    Parser parser = new Parser(eingabeFile);
    SymboleTable tabelle = new SymboleTable();

    int lineNr = 0;

    while (parser.hasMoreLines()) {
      int type = parser.instructionType();
      if (type == Parser.L_INSTRUCTION) {
        String label = parser.symbol();
        tabelle.addEntry(label, lineNr);
      } else {
        lineNr++;
      }
      parser.advance();
    }
    parser.close();

    parser = new Parser(eingabeFile);

    BufferedWriter writer = new BufferedWriter(new FileWriter(ausgabeFile));


    while (parser.hasMoreLines()) {
      int type = parser.instructionType();

      if (type == Parser.L_INSTRUCTION) {
        parser.advance();
        continue;
      }

      String binary = "";

      if (type == Parser.A_INSTRUCTION) {
        String symbol = parser.symbol();
        int adresse;

        if (Character.isDigit(symbol.charAt(0))) {
          adresse = Integer.parseInt(symbol);
        } else {
          if (tabelle.contains(symbol)) {
            adresse = tabelle.getAddress(symbol);
          } else {
            adresse = tabelle.getNextFreeAddress();
            tabelle.addEntry(symbol, adresse);
          }
        }
        binary = toBinaryString(adresse);

      } else if (type == Parser.C_INSTRUCTION) {
        String dest = Code.dest(parser.dest());
        String comp = Code.comp(parser.comp());
        String jump = Code.jump(parser.jump());

        binary = "111" + comp + dest + jump;
      }

      writer.write(binary);
      writer.newLine();
      parser.advance();
    }

    writer.close();
    parser.close();

  }

  private static String toBinaryString(int value) {
    return String.format("%16s", Integer.toBinaryString(value & 0xFFFF)).replace(' ', '0');
  }


}
