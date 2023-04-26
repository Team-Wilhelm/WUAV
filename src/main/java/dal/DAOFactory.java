package dal;

public class DAOFactory {
    public enum DAOType {
        CUSTOMER, USER, DOCUMENT
    }

    public static IDAO createDAO(DAOType type) {
        return switch (type) {
            case CUSTOMER -> new CustomerDAO();
            case USER -> new UserDAO();
            case DOCUMENT -> new DocumentDAO();
        };
    }
}
