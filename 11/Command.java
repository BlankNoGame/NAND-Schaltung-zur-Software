public enum Command {ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT;

    public String toString(){
        return switch (this) {
            case ADD -> "add";
            case SUB -> "sub";
            case NEG -> "neg";
            case EQ -> "eq";
            case GT -> "gt";
            case LT -> "lt";
            case AND -> "and";
            case OR -> "or";
            case NOT -> "not";
            default -> "unknown";
        };
    }
}
