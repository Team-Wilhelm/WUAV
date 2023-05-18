package utils.permissions;

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
        boolean hasAccess = false;
        UserRole loggedInUser = UserModel.getLoggedInUser().getUserRole();
        Method[] methods = thisClass.getMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(RequiresPermission.class)) {
                RequiresPermission rp = m.getAnnotation(RequiresPermission.class);
                hasAccess = Arrays.stream(rp.value()).toList().contains(loggedInUser);
            }
        }
        return hasAccess;
    }
}