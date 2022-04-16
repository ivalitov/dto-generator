package laoruga.dtogenerator.api.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static Object getValue(Object instance, Field field) {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            return e;
        } finally {
            field.setAccessible(accessible);
        }
    }
}
