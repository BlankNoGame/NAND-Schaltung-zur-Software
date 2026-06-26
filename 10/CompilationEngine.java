import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class CompilationEngine {

  private final BufferedWriter writer;
  private int einrückung = 0;
  private JackTokenizer tokenizer;

  private static final Set<Character> OPS = Set.of('+','-','*','/', '&','|','<','>','=');

  public CompilationEngine(JackTokenizer tokenizer, File output) throws IOException {
    this.writer = new BufferedWriter(new FileWriter(output));
    this.tokenizer = tokenizer;
    tokenizer.advance();
  }

  // Hilfsmethode: ermöglicht einfache Einrückung
  private void writeEinrueckunng() throws IOException {
    writer.write(" ".repeat(einrückung));
  }

  // Hilfsmethode: schreibt das Opening Tag für Non Terminals Grammar Rules
  private void openXMLTagNonTerminal(String tag) throws IOException {
    writeEinrueckunng();
    writer.write("<"+tag+">");
    writer.newLine();
    einrückung += 2;
  }

  // Hilfsmethode: schreibt das Closing Tag für Non Terminals Grammar Rules
  private void closeXMLTagNonTerminal(String tag) throws IOException {
    einrückung -= 2;
    writeEinrueckunng();
    writer.write("</"+tag+">");
    writer.newLine();
  }

  // Hilfsmethode: schreibt alle Tokens/Terminals, basierend auf deren Keyword
  private void writeCurrentToken() throws IOException {
    String value = tokenizer.tokenValue();
    value = exchangeSpecificSymbols(value);

    String tag = switch (tokenizer.tokenType()) {
      case KEYWORD -> "keyword";
      case SYMBOL -> "symbol";
      case IDENTIFIER -> "identifier";
      case INT_CONST -> "integerConstant";
      case STRING_CONST -> "stringConstant";
    };

    writeEinrueckunng();
    writer.write("<"+tag+"> ");
    writer.write(value);
    writer.write(" </"+tag+">");
    writer.newLine();
    if (tokenizer.hasMoreTokens()) tokenizer.advance();
  }

  // Hilfsmethode: ersetzt die Symbole welche auch in der XML syntax existieren
  private String exchangeSpecificSymbols(String value) {
    return value.replace("&", "&amp;").replace("<", "&lt;")
        .replace(">", "&gt;").replace("\"", "&quot;");
  }

  public void compileClass() throws IOException {
    openXMLTagNonTerminal("class");
    writeCurrentToken();
    writeCurrentToken();
    writeCurrentToken();

    while(tokenizer.tokenType() == tokenTyp.KEYWORD &&
        (tokenizer.keyWord() == keyWort.STATIC || tokenizer.keyWord() == keyWort.FIELD)) {
      compileClassVarDec();
    }
    while(tokenizer.tokenType() == tokenTyp.KEYWORD && (tokenizer.keyWord() == keyWort.CONSTRUCTOR
            || tokenizer.keyWord() == keyWort.FUNCTION || tokenizer.keyWord() == keyWort.METHOD)) {
      compileSubroutine();
    }
    writeCurrentToken();
    closeXMLTagNonTerminal("class");
    writer.close();

  }

  public void compileClassVarDec() throws IOException {
    openXMLTagNonTerminal("classVarDec");
    writeCurrentToken();
    do {
      writeCurrentToken();
      writeCurrentToken();
    } while (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',');
    writeCurrentToken();
    closeXMLTagNonTerminal("classVarDec");
  }

  public void compileSubroutine() throws IOException {
    openXMLTagNonTerminal("subroutineDec");
    writeCurrentToken();
    writeCurrentToken();
    writeCurrentToken();
    writeCurrentToken();
    compileParameterList();
    writeCurrentToken();
    compileSubroutineBody();
    closeXMLTagNonTerminal("subroutineDec");
  }

  public void compileParameterList() throws IOException {
    openXMLTagNonTerminal("parameterList");
    if(tokenizer.tokenType() != tokenTyp.SYMBOL || tokenizer.symbol() != ')') {
      writeCurrentToken();
      writeCurrentToken();
      while (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',') {
        writeCurrentToken();
        writeCurrentToken();
        writeCurrentToken();
      }
    }
    closeXMLTagNonTerminal("parameterList");
  }

  public void compileSubroutineBody() throws IOException {
    openXMLTagNonTerminal("subroutineBody");
    writeCurrentToken();
    while (tokenizer.tokenType() == tokenTyp.KEYWORD && tokenizer.keyWord() == keyWort.VAR) {
      compileVarDec();
    }
    compileStatements();
    writeCurrentToken();
    closeXMLTagNonTerminal("subroutineBody");
  }

  public void compileVarDec() throws IOException {
    openXMLTagNonTerminal("varDec");
    writeCurrentToken();
    writeCurrentToken();
    writeCurrentToken();
    while(tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',') {
      writeCurrentToken();
      writeCurrentToken();
    }
    writeCurrentToken();
    closeXMLTagNonTerminal("varDec");
  }

  public void compileStatements() throws IOException {
    openXMLTagNonTerminal("statements");
    while (tokenizer.tokenType() == tokenTyp.KEYWORD) {
      switch (tokenizer.keyWord()) {
        case LET -> compileLet();
        case IF -> compileIf();
        case WHILE -> compileWhile();
        case DO -> compileDo();
        case RETURN -> compileReturn();
        default -> {
          closeXMLTagNonTerminal("statements");
          return;
        }
      }
    }
    closeXMLTagNonTerminal("statements");
  }

  public void compileLet() throws IOException {
    openXMLTagNonTerminal("letStatement");
    writeCurrentToken();
    writeCurrentToken();
    if (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == '[') {
      writeCurrentToken();
      compileExpression();
      writeCurrentToken();
    }
    writeCurrentToken();
    compileExpression();
    writeCurrentToken();
    closeXMLTagNonTerminal("letStatement");
  }

  public void compileIf() throws IOException {
    openXMLTagNonTerminal("ifStatement");
    writeCurrentToken();
    writeCurrentToken();
    compileExpression();
    writeCurrentToken();
    writeCurrentToken();
    compileStatements();
    writeCurrentToken();
    if (tokenizer.tokenType() == tokenTyp.KEYWORD && tokenizer.keyWord() == keyWort.ELSE) {
      writeCurrentToken();
      writeCurrentToken();
      compileStatements();
      writeCurrentToken();
    }
    closeXMLTagNonTerminal("ifStatement");
  }

  public void compileWhile() throws IOException {
    openXMLTagNonTerminal("whileStatement");
    writeCurrentToken();
    writeCurrentToken();
    compileExpression();
    writeCurrentToken();
    writeCurrentToken();
    compileStatements();
    writeCurrentToken();
    closeXMLTagNonTerminal("whileStatement");
  }

  public void compileDo() throws IOException {
    openXMLTagNonTerminal("doStatement");
    writeCurrentToken();
    writeCurrentToken();
    compileSubroutineCalls();
    writeCurrentToken();
    closeXMLTagNonTerminal("doStatement");
  }

  public void compileReturn() throws IOException {
    openXMLTagNonTerminal("returnStatement");
    writeCurrentToken();
    if(tokenizer.tokenType() != tokenTyp.SYMBOL || tokenizer.symbol() != ';') {
      compileExpression();
    }
    writeCurrentToken();
    closeXMLTagNonTerminal("returnStatement");
  }

  public void compileExpression() throws IOException {
    openXMLTagNonTerminal("expression");
    compileTerm();
    while(tokenizer.tokenType() == tokenTyp.SYMBOL && OPS.contains(tokenizer.symbol())) {
      writeCurrentToken();
      compileTerm();
    }
    closeXMLTagNonTerminal("expression");
  }

  public void compileTerm() throws IOException {
    openXMLTagNonTerminal("term");

    if(tokenizer.tokenType() == tokenTyp.INT_CONST || tokenizer.tokenType() == tokenTyp.STRING_CONST) {
      writeCurrentToken();
    } else if(tokenizer.tokenType() == tokenTyp.KEYWORD && (tokenizer.keyWord() == keyWort.TRUE
        || tokenizer.keyWord() == keyWort.FALSE || tokenizer.keyWord() == keyWort.NULL
        || tokenizer.keyWord() == keyWort.THIS)) {
      writeCurrentToken();
    } else if(tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == '(') {
      writeCurrentToken();
      compileExpression();
      writeCurrentToken();
    } else if(tokenizer.tokenType() == tokenTyp.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
      writeCurrentToken();
      compileTerm();
    } else {
      writeCurrentToken();
      if(tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == '[') {
        writeCurrentToken();
        compileExpression();
        writeCurrentToken();
      } else if(tokenizer.tokenType() == tokenTyp.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
        compileSubroutineCalls();
      }
    }

    closeXMLTagNonTerminal("term");
  }

  // Hilfsmethode welche das Aufrufen von Methoden/Subroutinen verarbeitet, entweder
  // von Klassen Methoden oder .methodName aufrufe
  private int compileSubroutineCalls() throws IOException {
    if (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == '.') {
      writeCurrentToken();
      writeCurrentToken();
    }
    writeCurrentToken();
    int argCount = compileExpressionList();
    writeCurrentToken();
    return argCount;
  }

  public int compileExpressionList() throws IOException {
    openXMLTagNonTerminal("expressionList");
    int expressionCount = 0;
    if(tokenizer.tokenType() != tokenTyp.SYMBOL || tokenizer.symbol() != ')') {
      compileExpression();
      expressionCount++;
      while (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',') {
        writeCurrentToken();
        compileExpression();
        expressionCount++;
      }
    }
    closeXMLTagNonTerminal("expressionList");
    return expressionCount;
  }

}
