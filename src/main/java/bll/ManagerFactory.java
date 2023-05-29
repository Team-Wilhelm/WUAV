package bll;

import bll.manager.CustomerManager;
import bll.manager.DocumentManager;
import bll.manager.UserManager;
import utils.enums.BusinessEntityType;

/**
 * Factory for creating the Manager classes.
 * Since we are typecasting the Manager classes to their respective classes, this class is no longer needed.
 */
public class ManagerFactory {
    public static IManager<?> createManager(BusinessEntityType type) {
        return switch (type) {
            case CUSTOMER -> new CustomerManager();
            case USER -> new UserManager();
            case DOCUMENT -> new DocumentManager();
        };
    }
}
