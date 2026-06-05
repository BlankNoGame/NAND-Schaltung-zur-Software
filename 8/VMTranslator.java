import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class VMTranslator {

  public static void main(String[] args) {

    if (args.length != 1) {
      System.out.println("Keine Datei übergeben, Programm wird nicht ausgeführt");
    }

    String input = args[0];
    Path srcPath = Paths.get(input);

    System.out.println(srcPath);

    try {
      List<Path> vmFilesInSource;

      String baseName = srcPath.getFileName().toString();
      int i = baseName.lastIndexOf('.');
      if (i != -1) baseName = baseName.substring(0, i);

      Path outputFile = srcPath.resolveSibling(baseName + ".asm");

      if (Files.isDirectory(srcPath)) {
        outputFile = srcPath.resolve(srcPath.getFileName() + ".asm");

        try (Stream<Path> walk = Files.list(srcPath)) {
          vmFilesInSource = walk
              .filter(Files::isRegularFile)
              .filter(p -> p.toString().toLowerCase().endsWith(".vm"))
              .sorted(mainFirst)
              .toList();
        }

        if (vmFilesInSource.isEmpty()) {
          System.err.println("Keine .vm Dateien im Verzeichnis gefundne: " + srcPath);
        }

        translate(vmFilesInSource, outputFile);

      } else if (Files.isRegularFile(srcPath)) {

        vmFilesInSource = List.of(srcPath);
        translate(vmFilesInSource, outputFile);

      } else {

        if (!input.toLowerCase().endsWith(".vm")) {
          System.err.println("Argument ist weder ein existierender Pfad noch eine .vm Datei: " + input);
        }

        String name = input.replace(".vm", "");
        if (!name.isEmpty() && Character.isUpperCase(name.charAt(0))) {
          System.err.println("Argument ist kein existierender Pfad und endet nicht auf .vm: " + input);
        }

        translate(List.of(srcPath), outputFile);

      }
    } catch (IOException e) {
      System.err.println("Input / Output Fehler: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void translate(List<Path> inputFiles, Path outputFile) throws IOException {

    File ausgabeFile = outputFile.toFile();

    CodeWriter codeWriter = new CodeWriter(ausgabeFile, inputFiles.size());

    for (Path path : inputFiles) {

      String fileName = path.getFileName().toString();

      codeWriter.setFileName(fileName.substring(0, fileName.lastIndexOf('.')));

      Parser parser = new Parser(path.toFile());

      while(parser.hasMoreLines()) {
        Command command = parser.commandType();

        if (Command.C_ARITHMETIC == command) {
          codeWriter.writeArithmetic(parser.arg1());
        } else if (Command.C_LABEL == command) {
          codeWriter.writeLabel(parser.arg1());
        } else if (Command.C_GOTO == command) {
          codeWriter.writeGoto(parser.arg1());
        } else if (Command.C_IF == command) {
          codeWriter.writeIf(parser.arg1());
        } else if (Command.C_FUNCTION == command) {
          codeWriter.writeFunction(parser.arg1(), parser.arg2());
        } else if (Command.C_CALL == command) {
          codeWriter.writeCall(parser.arg1(), parser.arg2());
        } else if (Command.C_RETURN == command) {
          codeWriter.writeReturn();
        } else {
          codeWriter.WritePushPop(command, parser.arg1(), parser.arg2());
        }
        parser.advance();
      }
    }
    codeWriter.close();
  }

  static Comparator<Path> mainFirst = (p1, p2) -> {
    boolean p1IsMain = p1.getFileName().toString().equals("Main.vm");
    boolean p2IsMain = p2.getFileName().toString().equals("Main.vm");
    if (p1IsMain && !p2IsMain) return -1;
    if (!p1IsMain && p2IsMain) return 1;
    return 0;
  };

}
