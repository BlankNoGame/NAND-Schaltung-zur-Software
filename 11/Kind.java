public enum Kind { STATIC, FIELD, ARG, VAR, NONE;

    @Override
    public String toString() {
        return switch (this) {
            case STATIC -> "static";
            case FIELD -> "field";
            case ARG -> "arg";
            case VAR -> "var";
            case NONE -> "none";
            default -> "unknown";
        };
    }
}
