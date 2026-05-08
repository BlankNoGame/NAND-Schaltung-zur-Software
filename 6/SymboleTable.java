import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SymboleTable {

  private static final HashMap<String, Integer>
      TABLE =  new HashMap<>(Map.ofEntries(Map.entry("R0", 0),
      Map.entry("R1", 1), Map.entry("R2", 2), Map.entry("R3", 3), Map.entry("R4", 4),
      Map.entry("R5", 5), Map.entry("R6", 6), Map.entry("R7", 7), Map.entry("R8", 8),
      Map.entry("R9", 9), Map.entry("R10", 10), Map.entry("R11", 11), Map.entry("R12", 12),
      Map.entry("R13", 13), Map.entry("R14", 14), Map.entry("R15", 15), Map.entry("SCREEN", 16384),
      Map.entry("KBD", 24576), Map.entry("SP", 0), Map.entry("LCL", 1), Map.entry("ARG", 2),
      Map.entry("THIS", 3), Map.entry("THAT", 4)));

  private final Map<String, Integer> symbolTable = new HashMap<>(TABLE);
  private final Set<Integer> benutzeAdressen = new TreeSet<>();
  private int nextFreeAddress = 16;

  public SymboleTable () {
    benutzeAdressen.addAll(TABLE.values());
  }

  public void addEntry (String entry, int address) {

    if (contains(entry)) {
      throw new IllegalStateException("Entry bereits vorhanden");
    }

    symbolTable.put(entry, address);
    benutzeAdressen.add(address);

    if (address == nextFreeAddress) nextFreeAddress++;

  }

  public int getNextFreeAddress () {
    return nextFreeAddress;
  }

  public boolean contains (String symbol) {
    return symbolTable.containsKey(symbol);
  }

  public int getAddress (String symbol) {
    return symbolTable.getOrDefault(symbol, -1);
  }

}
