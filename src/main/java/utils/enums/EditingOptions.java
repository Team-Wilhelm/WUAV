package utils.enums;

public enum EditingOptions {
    EDIT("Edit"),
    DELETE("Delete");

    private final String name;

    EditingOptions(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static EditingOptions fromString(String text) {
        for (EditingOptions b : EditingOptions.values()) {
            if (b.name.equals(text)) {
                return b;
            }
        }
        return null;
    }
}
