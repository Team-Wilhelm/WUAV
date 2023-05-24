package bll;

import bll.manager.CustomerManager;
import bll.manager.DocumentManager;
import bll.manager.UserManager;
import utils.enums.BusinessEntityType;

public class ManagerFactory {

    public static IManager createManager(BusinessEntityType type) {
        return switch (type) {
            case CUSTOMER -> new CustomerManager();
            case USER -> new UserManager();
            case DOCUMENT -> new DocumentManager();
        };
    }
}
