import java.io.*;
import java.util.Set;

public class CompilationEngine {

  private JackTokenizer tokenizer;
  private SymbolTable symbolTable;
  private VMWriter vmWriter;

  private String currentClassName;
  private String currentSubroutineName;
  private keyWort currentSubroutineType;
  private int whileCounter = 0;
  private int ifCounter = 0;

  private static final Set<Character> OPS = Set.of('+','-','*','/', '&','|','<','>','=');

  public CompilationEngine(JackTokenizer tokenizer, File vmOutput) throws IOException {
    this.vmWriter = new VMWriter(vmOutput);
    this.symbolTable = new SymbolTable();
    this.tokenizer = tokenizer;
    tokenizer.advance();
  }

  public void compileClass() throws IOException {
    expectKeyword(keyWort.CLASS);
    currentClassName = expectIdentifier();
    expectSymbol('{');

    while(tokenizer.tokenType() == tokenTyp.KEYWORD &&
        (tokenizer.keyWord() == keyWort.STATIC || tokenizer.keyWord() == keyWort.FIELD)) {
      compileClassVarDec();
    }
    while(tokenizer.tokenType() == tokenTyp.KEYWORD && (tokenizer.keyWord() == keyWort.CONSTRUCTOR
            || tokenizer.keyWord() == keyWort.FUNCTION || tokenizer.keyWord() == keyWort.METHOD)) {
      compileSubroutine();
    }
    expectSymbol('}');
    vmWriter.close();
  }

  public void compileClassVarDec() throws IOException {
    Kind kind = tokenizer.keyWord() == keyWort.STATIC ? Kind.STATIC : Kind.FIELD;
    consume();
    String type = expectType();

    while (true) {
      String name = expectIdentifier();
      symbolTable.define(name, type, kind);
      if (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',') {
        consume();
      } else {
        break;
      }
    }
    expectSymbol(';');
  }

  public void compileSubroutine() throws IOException {
    symbolTable.reset();

    currentSubroutineType = tokenizer.keyWord();
    consume();
    expectType();
    currentSubroutineName = expectIdentifier();
    expectSymbol('(');

    if (currentSubroutineType == keyWort.METHOD) {
      symbolTable.define("this", currentClassName, Kind.ARG);
    }
    compileParameterList();
    expectSymbol(')');
    compileSubroutineBody();
  }

  public void compileParameterList() throws IOException {
    if(tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ')') {
      return;
    }
    while (true) {
      String type = expectType();
      String name = expectIdentifier();
      symbolTable.define(name, type, Kind.ARG);
      if (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',') {
        consume();
      } else {
        break;
      }
    }
  }

  public void compileSubroutineBody() throws IOException {
    expectSymbol('{');
    while (tokenizer.tokenType() == tokenTyp.KEYWORD && tokenizer.keyWord() == keyWort.VAR) {
      compileVarDec();
    }
    vmWriter.writeFunction(currentClassName + "." + currentSubroutineName, symbolTable.varCount(Kind.VAR));
    if (currentSubroutineType == keyWort.CONSTRUCTOR) {
      vmWriter.writePush(Segment.CONSTANT, symbolTable.varCount(Kind.FIELD));
      vmWriter.writeCall("Memory.alloc", 1);
      vmWriter.writePop(Segment.POINTER, 0);
    }
    if (currentSubroutineType == keyWort.METHOD) {
      vmWriter.writePush(Segment.ARGUMENT,0);
      vmWriter.writePop(Segment.POINTER, 0);
    }
    compileStatements();
    expectSymbol('}');
  }

  public void compileVarDec() throws IOException {
    expectKeyword(keyWort.VAR);
    String type = expectType();
    while (true) {
      String name = expectIdentifier();
      symbolTable.define(name, type, Kind.VAR);
      if (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',') {
        consume();
      } else {
        break;
      }
    }
    expectSymbol(';');
  }

  public void compileStatements() throws IOException {
    //expectSymbol('{');
    while (tokenizer.tokenType() == tokenTyp.KEYWORD) {
      switch (tokenizer.keyWord()) {
        case LET -> compileLet();
        case IF -> compileIf();
        case WHILE -> compileWhile();
        case DO -> compileDo();
        case RETURN -> compileReturn();
        default -> {
          return;
        }
      }
    }
    //expectSymbol('}');
  }

  public void compileLet() throws IOException {
    expectKeyword(keyWort.LET);
    String varName = expectIdentifier();
    boolean isArray = false;
    if (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == '[') {
      isArray = true;
      consume();
      pushVariable(varName);
      compileExpression();
      expectSymbol(']');
      vmWriter.writeArithmetic(Command.ADD);
    }
    expectSymbol('=');
    compileExpression();
    expectSymbol(';');
    if (isArray) {
      vmWriter.writePop(Segment.TEMP, 0);
      vmWriter.writePop(Segment.POINTER, 1);
      vmWriter.writePush(Segment.TEMP, 0);
      vmWriter.writePop(Segment.THAT, 0);
    } else {
      popVariable(varName);
    }
  }

  public void compileIf() throws IOException {
    expectKeyword(keyWort.IF);
    int currentLabel = ifCounter++;
    String trueLabel = "IF_TRUE" + currentLabel;
    String falseLabel = "IF_FALSE" + currentLabel;
    String endLabel = "IF_END" + currentLabel;
    expectSymbol('(');
    compileExpression();
    expectSymbol(')');
    vmWriter.writeIf(trueLabel);
    vmWriter.writeGoto(falseLabel);
    vmWriter.writeLabel(trueLabel);
    expectSymbol('{');
    compileStatements();
    expectSymbol('}');
    if (tokenizer.tokenType() == tokenTyp.KEYWORD && tokenizer.keyWord() == keyWort.ELSE) {
      vmWriter.writeGoto(endLabel);
      vmWriter.writeLabel(falseLabel);
      expectKeyword(keyWort.ELSE);
      expectSymbol('{');
      compileStatements();
      expectSymbol('}');
      vmWriter.writeLabel(endLabel);
    } else {
      vmWriter.writeLabel(falseLabel);
    }
  }

  public void compileWhile() throws IOException {
    expectKeyword(keyWort.WHILE);
    int currentLable = whileCounter++;
    String startLabel = "WHILE_START" + currentLable;
    String endLabel = "WHILE_END" + currentLable;
    vmWriter.writeLabel(startLabel);
    expectSymbol('(');
    compileExpression();
    expectSymbol(')');
    vmWriter.writeArithmetic(Command.NOT);
    vmWriter.writeIf(endLabel);
    expectSymbol('{');
    compileStatements();
    expectSymbol('}');
    vmWriter.writeGoto(startLabel);
    vmWriter.writeLabel(endLabel);
  }

  public void compileDo() throws IOException {
    expectKeyword(keyWort.DO);
    String name = expectIdentifier();
    compileSubroutineCalls(name);
    vmWriter.writePop(Segment.TEMP, 0);
    expectSymbol(';');
  }

  public void compileReturn() throws IOException {
    expectKeyword(keyWort.RETURN);
    if(tokenizer.tokenType() != tokenTyp.SYMBOL || tokenizer.symbol() != ';') {
      compileExpression();
    } else {
      vmWriter.writePush(Segment.POINTER, 0); //Segment.CONSTANT
    }
    expectSymbol(';');
    vmWriter.writeReturn();
  }

  public void compileExpression() throws IOException {
    compileTerm();
    while(tokenizer.tokenType() == tokenTyp.SYMBOL && OPS.contains(tokenizer.symbol())) {
      char op = tokenizer.symbol();
      consume();
      compileTerm();
      writeOperator(op);
    }
  }

  public void compileTerm() throws IOException {
    switch (tokenizer.tokenType()) {
      case INT_CONST -> {
        vmWriter.writePush(Segment.CONSTANT, tokenizer.intVal());
        consume();
      }
      case STRING_CONST -> {
        String text =  tokenizer.tokenValue();
        vmWriter.writePush(Segment.CONSTANT, text.length());
        vmWriter.writeCall("String.new", 1);
        for (char c : text.toCharArray()) {
          vmWriter.writePush(Segment.CONSTANT, (int) c);
          vmWriter.writeCall("String.appendChar", 2);
        }
        consume();
      }
      case IDENTIFIER -> {
        String name = tokenizer.tokenValue();
        Token next = tokenizer.peekToken();
        if(next != null && next.type() == tokenTyp.SYMBOL) {
          switch(next.value()) {
            case "[" -> {
              consume();
              expectSymbol('[');
              pushVariable(name);
              compileExpression();
              expectSymbol(']');
              vmWriter.writeArithmetic(Command.ADD);
              vmWriter.writePop(Segment.POINTER,1);
              vmWriter.writePush(Segment.THAT,0);
            }
            case "(", "." -> {
              consume();
              compileSubroutineCalls(name);
            }
            default -> {
              pushVariable(name);
              consume();
            }
          }
        } else {
          pushVariable(name);
          consume();
        }
      }
      case SYMBOL -> {
        if(tokenizer.symbol() == '(') {
          consume();
          compileExpression();
          expectSymbol(')');
        } else if(tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
          char operator = tokenizer.symbol();
          consume();
          compileTerm();
          if(operator == '-') {
            vmWriter.writeArithmetic(Command.NEG);
          } else {
            vmWriter.writeArithmetic(Command.NOT);
          }
        } else {
          throw new IllegalStateException("Unexpected symbol " + tokenizer.symbol());
        }
      }
      case KEYWORD -> {
        switch (tokenizer.keyWord()) {
          case TRUE -> {
            vmWriter.writePush(Segment.CONSTANT, 0);
            vmWriter.writeArithmetic(Command.NOT);
          }
          case FALSE, NULL -> {
            vmWriter.writePush(Segment.CONSTANT, 0);
          }
          case THIS -> vmWriter.writePush(Segment.POINTER, 0);
          default -> throw new IllegalStateException("Unexpected token: " + tokenizer.tokenType());
        }
        consume();
      }
      default ->
        throw new IllegalArgumentException("Kein Konfiguration gefunden: " + tokenizer.tokenType() + " " + tokenizer.tokenValue());
    }
  }

  public int compileExpressionList() throws IOException {
    expectSymbol('(');
    int expressionCount = 0;
    if(tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ')') {
      expectSymbol(')');
      return 0;
    }
    compileExpression();
    expressionCount++;
    while (tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == ',') {
      consume();
      compileExpression();
      expressionCount++;
    }
    expectSymbol(')');
    return expressionCount;
  }

  //Helper methods Abschnitt

  //Abstraktion des Advancen des Compiliers, vorher gereicht in writeCurrentToken. Da die Methode nicht mehr benötigt wurde
  // und man an vielen Stellen weitere gehen muss im Code diese Hilfsmethode
  private void consume() throws IOException {
    if (tokenizer.hasMoreTokens()) {
      tokenizer.advance();
    }
  }

  /*
  * Abschnitt von expect Methoden welche durch eine Abfrage des Tokens garantieren, dass der korrekte Token gelesen wird
  * und falls true den tokenizer advancen. Vereinfacht das Verständnis wo wir im Code liegen
  * */

  private void expectKeyword(keyWort keyWort) throws IOException {
    if (tokenizer.tokenType() != tokenTyp.KEYWORD || tokenizer.keyWord() != keyWort) {
      throw new IllegalStateException("Expected keyword " + keyWort + " but got " + tokenizer.tokenType() + ": " + tokenizer.tokenValue());
    }
    consume();
  }

  private void expectSymbol(char symbol) throws IOException {
    if (tokenizer.tokenType() != tokenTyp.SYMBOL || tokenizer.symbol() != symbol) {
      throw new IllegalStateException("Expected symbol " + symbol + " but got " + tokenizer.tokenType() + ": " + tokenizer.tokenValue());
    }
    consume();
  }

  private String expectIdentifier() throws IOException {
    if (tokenizer.tokenType() != tokenTyp.IDENTIFIER) {
      throw new IllegalStateException("Expected identifier but got " + tokenizer.tokenType() + ": " + tokenizer.tokenValue());
    }
    String identifier = tokenizer.tokenValue();
    consume();
    return identifier;
  }

  private String expectType() throws IOException {
    if (tokenizer.tokenType() == tokenTyp.KEYWORD) {
      keyWort keyword = tokenizer.keyWord();
      if (keyword == keyWort.INT || keyword == keyWort.CHAR || keyword == keyWort.BOOLEAN || keyword == keyWort.VOID) {
        String type = tokenizer.tokenValue();
        consume();
        return type;
      }
    }
    if (tokenizer.tokenType() == tokenTyp.IDENTIFIER) {
      return expectIdentifier();
    }
    throw new IllegalStateException("Expected keyword but got " + tokenizer.tokenType() + " value= " + tokenizer.tokenValue());
  }

  // Methode welche compileExpression debloated
  private void writeOperator(char op) throws IOException {
    switch(op){
      case '+' -> vmWriter.writeArithmetic(Command.ADD);
      case '-' -> vmWriter.writeArithmetic(Command.SUB);
      case '&' -> vmWriter.writeArithmetic(Command.AND);
      case '|' -> vmWriter.writeArithmetic(Command.OR);
      case '<' -> vmWriter.writeArithmetic(Command.LT);
      case '>' -> vmWriter.writeArithmetic(Command.GT);
      case '=' -> vmWriter.writeArithmetic(Command.EQ);
      case '*' -> vmWriter.writeCall("Math.multiply",2);
      case '/' -> vmWriter.writeCall("Math.divide", 2);
      default -> throw new IllegalStateException("Unknown operator " + op);
    }
  }
  //Push und Pop für Vars
  private void pushVariable(String name) throws IOException {
    Kind kind = symbolTable.kindOf(name);
    vmWriter.writePush(kindToSeg(kind), symbolTable.indexOf(name));
  }

  private void popVariable(String name) throws IOException {
    Kind kind = symbolTable.kindOf(name);
    vmWriter.writePop(kindToSeg(kind), symbolTable.indexOf(name));
  }

  // Hilfsmethode welche das Aufrufen von Methoden/Subroutinen verarbeitet, entweder
  // von Klassen Methoden oder .methodName aufrufe
  private int compileSubroutineCalls(String name) throws IOException {
    int nArgs = 0;
    if(tokenizer.tokenType() == tokenTyp.SYMBOL && tokenizer.symbol() == '.') {
      consume();
      String subroutineName = expectIdentifier();
      boolean isMethod = symbolTable.kindOf(name) != Kind.NONE;
      if(isMethod) {
        pushVariable(name);
      }
      nArgs = compileExpressionList();
      if(isMethod) {
        vmWriter.writeCall(symbolTable.typeOf(name) + "." + subroutineName, nArgs + 1);
        return nArgs + 1;
      } else {
        vmWriter.writeCall(name + "." + subroutineName,nArgs);
        return nArgs;
      }
    } else {
      vmWriter.writePush(Segment.POINTER,0);
      nArgs = compileExpressionList();
      vmWriter.writeCall(currentClassName + "." + name,nArgs + 1);
    }
    return nArgs + 1;
  }

  // Methode welche von Kind to Segment übersetzt
  private Segment kindToSeg(Kind kind) {
    return switch(kind) {
      case STATIC -> Segment.STATIC;
      case FIELD -> Segment.THIS;
      case ARG -> Segment.ARGUMENT;
      case VAR -> Segment.LOCAL;
      case NONE -> throw new IllegalStateException("Variable does not exist in the symbol table");
    };
  }

}
