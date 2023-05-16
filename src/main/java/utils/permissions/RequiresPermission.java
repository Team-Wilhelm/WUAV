package utils.permissions;

import be.enums.UserRole;
import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPermission {
    UserRole[] value() default{};

}

