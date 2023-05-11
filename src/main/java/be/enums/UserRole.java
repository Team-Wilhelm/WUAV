package be.enums;

public enum UserRole {
    TECHNICIAN("Technician"),
    SALESPERSON("Salesperson"),
    PROJECT_MANAGER("Project Manager"),
    ADMINISTRATOR("Administrator"),
    ALL("All");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static UserRole fromString(String text) {
        for (UserRole b : UserRole.values()) {
            if (b.name.equals(text)) {
                return b;
            }
        }
        return null;
    }
}



