package utils.permissions;

import be.enums.UserRole;
import gui.controller.ViewControllers.DocumentController;
import gui.model.UserModel;
import gui.util.AlertManager;
import javafx.stage.Window;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Checker {
    /**
     * Checks if the logged-in user has access to the feature.
     * NOTE: Will invalidate any following checks, be sure to call this last!
     * @param thisClass
     * @return
     */
    public boolean hasAccess(Class<?> thisClass) {
        boolean hasAccess = false;
        UserRole loggedInUser = UserModel.getInstance().getLoggedInUser().getUserRole();
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