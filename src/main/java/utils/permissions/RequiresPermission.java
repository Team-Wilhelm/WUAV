package utils.permissions;

import utils.enums.UserRole;
import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPermission {
    UserRole[] value() default{};

}

