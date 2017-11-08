package main.java.graph;

public enum GraphType {
    PRIMAL,
    INCIDENCE,
    DUAL;

    @Override
    public String toString() {
        switch (this) {
            case PRIMAL: return "primal";
            case INCIDENCE: return "incidence";
            case DUAL: return "dual";
            default: throw new IllegalArgumentException();
        }
    }

    public GraphType fromString(String type) {
        switch (type) {
            case "primal": return PRIMAL;
            case "incidence": return INCIDENCE;
            case "dual": return DUAL;
            default: throw new IllegalArgumentException();
        }
    }
}
