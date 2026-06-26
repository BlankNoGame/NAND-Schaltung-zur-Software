import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JackTokenizer {

  private static final Set<String> KEYWORDS = Set.of(
      "class", "constructor", "function", "method", "field", "static", "var",
      "int", "char", "boolean", "void", "true", "false", "null", "this",
      "let", "do", "if", "else", "while", "return");

  private static final Set<Character> SYMBOLS = Set.of(
      '{', '}', '(', ')', '[', ']', '.', ',', ';', '+',
      '-', '*', '/', '&', '|', '<', '>', '=', '~');

  private final List<Token> tokens = new ArrayList<>();
  private int currentIndex = -1;
  private Token currentToken;

  public JackTokenizer(File input) throws IOException {
    String inhalt = Files.readString(input.toPath());
    tokenize(removeComments(inhalt));
  }

  // Hilfsmethode: Iteriert durch Inputstream; Analysiert input,
  // je nach gegebenheit des Inputs, erstellt neues Token Objekt
  // mit entsprechenden tokenTyp und eingehenden Value
  private void tokenize(String inhalt) {
    int i = 0;
    while (i < inhalt.length()) {
      char identifier = inhalt.charAt(i);
      if (Character.isWhitespace(identifier)) {
        i++;
        continue;
      }
      if (identifier == '"') {
        i++;
        StringBuilder sb = new StringBuilder();
        while (i < inhalt.length() && inhalt.charAt(i) != '"') {
          sb.append(inhalt.charAt(i));
          i++;
        }
        i++;
        tokens.add(new Token(tokenTyp.STRING_CONST, sb.toString()));
        continue;
      }
      if (SYMBOLS.contains(identifier)) {
        tokens.add(new Token(tokenTyp.SYMBOL, String.valueOf(identifier)));
        i++;
        continue;
      }
      StringBuilder sb = new StringBuilder();

      while (i < inhalt.length()) {
        char current = inhalt.charAt(i);
        if (Character.isWhitespace(current) || SYMBOLS.contains(current)) {
          break;
        }
        sb.append(current);
        i++;
      }
      String fullQualifiedWord = sb.toString();
      if (KEYWORDS.contains(fullQualifiedWord)) {
        tokens.add(new Token(tokenTyp.KEYWORD, fullQualifiedWord));
      }
      else if (fullQualifiedWord.matches("\\d+")) {
        int intVal = Integer.parseInt(fullQualifiedWord);

        if(intVal < 0 || intVal > 32767) {
          int res = intVal - 32767;
          String typ = res < 0 ? "kleiner 0" : "größer als 32767";
          System.err.println("intConstant liegt nicht im Akzeptierten bereich: " + typ);
        } else {
          tokens.add(new Token(tokenTyp.INT_CONST, fullQualifiedWord));
        }
      } else {
        tokens.add(new Token(tokenTyp.IDENTIFIER, fullQualifiedWord));
      }
    }
  }

  // Hilfsmethode welche alle Kommentare der Jack Klasse direkt entfernt
  private String removeComments(String inhalt) {
    inhalt = inhalt.replaceAll("(?s)/\\*.*?\\*/", "");
    inhalt = inhalt.replaceAll("//.*", "");
    return inhalt;
  }

  public boolean hasMoreTokens() {
    return currentIndex < tokens.size() - 1;
  }

  public void advance() throws IOException {
    if(hasMoreTokens()) {
      currentIndex++;
      currentToken = tokens.get(currentIndex);
    }
  }

  public tokenTyp tokenType() {
    return currentToken.type();
  }

  public keyWort keyWord() {
    return keyWort.valueOf(currentToken.value().toUpperCase());
  }

  public char symbol() {
    return currentToken.value().charAt(0);
  }

  public String tokenValue() {
    return currentToken.value();
  }

  // Ungenutzt, da ich ein Datenobjekt Token implementiert habe,
  // welches jewals das Value des bestimmten Token als String speichert
  // somit brauchen wir keine externe Methode aufrufen, sondern direkt
  // das Value des Tokens auslesen. Da diese aber in der API stehen,
  // habe ich sie hier auch erstmal stehen lassen

  public String identifier() {
    return currentToken.value();
  }

  public int intVal() {
    return Integer.parseInt(currentToken.value());
  }

  public String stringVal() {
    return currentToken.value();
  }
}
