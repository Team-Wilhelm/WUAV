package be.enums;

public enum UserRole {
    ALL("All"),
    SALESPERSON("Salesperson"),
    TECHNICIAN("Technician"),
    PROJECT_MANAGER("Project Manager"),
    ;

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



