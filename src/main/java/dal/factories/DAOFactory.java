package dal.factories;

import dal.dao.CustomerDAO;
import dal.dao.DocumentDAO;
import dal.dao.UserDAO;
import dal.interfaces.IDAO;
import utils.enums.BusinessEntityType;

public class DAOFactory {

    public static IDAO createDAO(BusinessEntityType type) {
        return switch (type) {
            case CUSTOMER -> new CustomerDAO();
            case USER -> new UserDAO();
            case DOCUMENT -> new DocumentDAO();
        };
    }
}
