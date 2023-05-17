package utils.enums;

public enum DocumentPropertyType {
    DATE_OF_CREATION("Date of creation: "),
    JOB_TITLE("Job title: "),
    JOB_DESCRIPTION("Job description: "),
    NOTES("Notes: "),
    TECHNICIANS("Technicians: "),
    IMAGE("Image: ");

    private final String name;

    DocumentPropertyType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static DocumentPropertyType fromString(String text) {
        for (DocumentPropertyType b : DocumentPropertyType.values()) {
            if (b.name.equals(text)) {
                return b;
            }
        }
        return null;
    }
}
