import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, Symbol> classScope;
    private final Map<String, Symbol> subroutineScope;
    private int staticCount;
    private int fieldCount;
    private int argCount;
    private int varCount;

    public SymbolTable() {
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        staticCount = 0;
        fieldCount = 0;
        argCount = 0;
        varCount = 0;
    }

    public void reset() {
        subroutineScope.clear();
        argCount = 0;
        varCount = 0;
    }

    public void define(String name, String type, Kind kind) {
        int index = switch (kind) {
            case STATIC -> staticCount++;
            case FIELD -> fieldCount++;
            case ARG -> argCount++;
            case VAR -> varCount++;
            default -> -1;
        };

        Symbol symbol = new Symbol(type, kind, index);

        if (kind == Kind.STATIC || kind == Kind.FIELD) {
            classScope.put(name, symbol);
        } else {
            subroutineScope.put(name, symbol);
        }
    }

    public int varCount(Kind kind) {
        return switch (kind) {
            case STATIC -> staticCount;
            case FIELD -> fieldCount;
            case ARG -> argCount;
            case VAR -> varCount;
            default -> 0;
        };
    }

    public Kind kindOf(String name) {
        if(subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).kind();
        }
        if(classScope.containsKey(name)) {
            return classScope.get(name).kind();
        }
        return Kind.NONE;
    }

    public String typeOf(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).type();
        }
        if(classScope.containsKey(name)) {
            return classScope.get(name).type();
        }
        return null;
    }

    public int indexOf(String name) {
        if (subroutineScope.containsKey(name)) {
            return subroutineScope.get(name).index();
        }
        if(classScope.containsKey(name)) {
            return classScope.get(name).index();
        }
        return -1;
    }

}
