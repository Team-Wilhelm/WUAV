package bll;

public class ManagerFactory {
    public enum ManagerType {
        CUSTOMER, USER, DOCUMENT
    }

    public static IManager createManager(ManagerType type) {
        return switch (type) {
            case CUSTOMER -> new CustomerManager();
            case USER -> new UserManager();
            case DOCUMENT -> new DocumentManager();
        };
    }
}
