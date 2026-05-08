public class Code {

  public static String dest(String s) {

    if (s == null) return "000";

    return switch (s) {
      case "M", "JGT" -> "001";
      case "D", "JEQ" -> "010";
      case "DM", "MD", "JGE" -> "011";
      case "A", "JLT" -> "100";
      case "AM", "JNE" -> "101";
      case "AD", "JLE" -> "110";
      case "ADM", "AMD","JMP" -> "111";
      default -> "000";
    };
  }

  public static String comp (String s) {

    if (s == null) return "000000";

    return switch (s) {
      case "0" -> "0101010";
      case "1" -> "0111111";
      case "-1" -> "0111010";
      case "D" -> "0001100";
      case "A" -> "0110000";
      case "M" -> "1110000";
      case "!D" -> "0001101";
      case "!A" -> "0110001";
      case "!M" -> "1110001";
      case "-D" -> "0001111";
      case "-A" -> "0110011";
      case "-M" -> "1110011";
      case "D+1" -> "0011111";
      case "A+1" -> "0110111";
      case "M+1" -> "1110111";
      case "D-1" -> "0001110";
      case "A-1" -> "0110010";
      case "M-1" -> "1110010";
      case "D+A" -> "0000010";
      case "D+M" -> "1000010";
      case "D-A" -> "0010011";
      case "D-M" -> "1010011";
      case "A-D" -> "0000111";
      case "M-D" -> "1000111";
      case "D|A" -> "0010101";
      case "D|M" -> "1010101";
      case "D&M", "M&D" -> "1000000";
      default -> "0000000";
    };
  }

  public static String jump (String s) {
    return dest(s);
  }

}
