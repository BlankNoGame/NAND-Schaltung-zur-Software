import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class JackAnalyzer {

  public static void main(String[] args) {

    if (args.length != 1) {
      System.err.println("Keine Datei/Ordner übergeben, Programm wird nicht ausgeführt");
      return;
    }
    Path input = Paths.get(args[0]);
    try {
      if (Files.isDirectory(input)) {
        compileDirectory(input);
      } else {
        compileFile(input);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void compileFile(Path file) throws IOException {
    String name = file.toString();

    String output = name.substring(0, name.length() - 5) + "T.xml";
    File outputFile = new File(output);
    JackTokenizer tokenizer = new JackTokenizer(file.toFile());
    CompilationEngine engine = new CompilationEngine(tokenizer, outputFile);
    engine.compileClass();
    System.out.println("Compiled: " + file.getFileName());
  }

  private static void compileDirectory(Path dir) throws IOException {
    try (Stream<Path> stream = Files.list(dir)) {
      List<Path> files = stream
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".jack"))
          .toList();

      for (Path f : files) {
        compileFile(f);
      }
    }
  }

}
