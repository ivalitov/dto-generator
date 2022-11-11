package laoruga.dtogenerator.api.util;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Il'dar Valitov
 * Created on 25.05.2022
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    public static Class<?> getGenericType(Field field) throws DtoGeneratorException {
        return (Class<?>) getGenericTypeOrPair(field);
    }

    private static final String CREATE_GENERATOR_MSG_PATTERN = " please create custom generator for type: %s";

    static Object getGenericTypeOrPair(Field field) throws DtoGeneratorException {
        String typeName = field.getGenericType().getTypeName();
        char[] chars = typeName.toCharArray();
        Integer leftIdx = null;
        Integer rightIdx = null;
        boolean multipleTypes = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '<') {
                if (leftIdx == null) {
                    leftIdx = i + 1;
                } else {
                    throw new DtoGeneratorException("Can't generate via default generator,"
                            + String.format(CREATE_GENERATOR_MSG_PATTERN, typeName));
                }
            } else if (chars[i] == '>') {
                rightIdx = i;
                break;
            } else if (chars[i] == ',') {
                if (multipleTypes) {
                    throw new DtoGeneratorException("Can't generate via default generator,"
                            + String.format(CREATE_GENERATOR_MSG_PATTERN, typeName));
                }
                multipleTypes = true;
            } else if (chars[i] == '?') {
                throw new DtoGeneratorException("Can't generate wildcard type via default generator,"
                        + String.format(CREATE_GENERATOR_MSG_PATTERN, typeName));
            }
        }
        if (rightIdx == null || leftIdx == null) {
            throw new DtoGeneratorException("Can't generate raw type via default generator,"
                    + String.format(CREATE_GENERATOR_MSG_PATTERN, typeName));
        } else {
            String className = typeName.substring(leftIdx, rightIdx);
            if (multipleTypes) {
                String[] split = className.split(",");
                return new Pair<>(getClass(split[0], typeName), getClass(split[1], typeName));
            } else {
                return getClass(className, typeName);
            }
        }
    }

    private static Class<?> getClass(String className, String typeName) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DtoGeneratorException("Can't create class for name: '" + className + "' obtained from" +
                    " type: '" + typeName + "'", e);
        }
    }

    private static final String FAILED_MSG_PATTERN = "Failed to instantiate class: '%s'";

    public static <T> T createInstance(Class<T> dtoClass) {
        try {
            Constructor<?>[] declaredConstructors = dtoClass.getDeclaredConstructors();
            if (declaredConstructors.length == 0) {
                throw new DtoGeneratorException(String.format(FAILED_MSG_PATTERN, dtoClass) +
                        " Class don't have public constructors. It must have public no-args constructor.");
            }
            Optional<Constructor<?>> maybeNoArgsConstructor = Arrays.stream(declaredConstructors)
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .findAny();
            if (!maybeNoArgsConstructor.isPresent()) {
                throw new DtoGeneratorException(String.format(FAILED_MSG_PATTERN, dtoClass) +
                        " Class must have public no-args constructor.");
            }
            Constructor<?> constructor = maybeNoArgsConstructor.get();
            boolean isAccessible = constructor.isAccessible();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            constructor.setAccessible(isAccessible);
            return (T) instance;
        } catch (InstantiationException ie) {
            throw new DtoGeneratorException(String.format(FAILED_MSG_PATTERN, dtoClass) +
                    " Maybe no-args constructor was not found.", ie);
        } catch (Exception e) {
            throw new DtoGeneratorException(String.format(FAILED_MSG_PATTERN, dtoClass), e);
        }
    }

    /**
     * 1. Filed type should be assignable from required collectionClass
     * 2. CollectionClass should not be an interface or abstract
     *
     * @param fieldType checking dto field type
     */
    public static <T> T createCollectionFieldInstance(Class<?> fieldType, Class<T> collectionClass) {
        if (!fieldType.isAssignableFrom(collectionClass)) {
            throw new DtoGeneratorException("CollectionClass from rules: '" + collectionClass + "' can't" +
                    " be assign to the field: " + fieldType);
        }
        if (collectionClass.isInterface() || Modifier.isAbstract(collectionClass.getModifiers())) {
            throw new DtoGeneratorException("Can't create instance of '" + collectionClass + "' because" +
                    " it is interface or abstract.");
        }
        T collectionInstance;
        try {
            collectionInstance = collectionClass.newInstance();
        } catch (Exception e) {
            log.error("Exception while creating Collection instance ", e);
            throw new DtoGeneratorException(e);
        }
        return collectionInstance;
    }
}
