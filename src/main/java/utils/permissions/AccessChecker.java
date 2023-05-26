package utils.permissions;

import be.User;
import utils.enums.UserRole;
import gui.model.UserModel;

import java.lang.reflect.Method;
import java.util.Arrays;

public class AccessChecker {
    /**
     * Checks if the logged-in user's role has access based on the contents of the RequiresPermission annotation.
     * @param thisClass
     * @return
     */
    public boolean hasAccess(Class<?> thisClass) {
        UserRole loggedInUserRole = UserModel.getLoggedInUser().getUserRole();
        return calculateAccess(thisClass, loggedInUserRole);
    }

    /**
     * Checks if the given serRole has access based on the contents of the RequiresPermission annotation.
     * Visiblity is set to public for testing purposes.
     */
    public boolean calculateAccess(Class<?> thisClass, UserRole userRole){
        boolean hasAccess = false;
        Method[] methods = thisClass.getMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(RequiresPermission.class)) {
                RequiresPermission rp = m.getAnnotation(RequiresPermission.class);
                hasAccess = Arrays.stream(rp.value()).toList().contains(userRole);
            }
        }
        return hasAccess;
    }
}