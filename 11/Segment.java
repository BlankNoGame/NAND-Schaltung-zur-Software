public enum Segment {CONSTANT, ARGUMENT, LOCAL, STATIC, THIS, THAT, POINTER, TEMP, UNK;

    @Override
    public String toString() {
        return switch (this) {
            case CONSTANT -> "constant";
            case ARGUMENT -> "argument";
            case LOCAL -> "local";
            case STATIC -> "static";
            case THIS -> "this";
            case THAT -> "that";
            case POINTER -> "pointer";
            case TEMP -> "temp";
            default -> "unknown";
        };
    }

}
