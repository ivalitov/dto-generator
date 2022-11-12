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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Il'dar Valitov
 * Created on 25.05.2022
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {
    private static final String TYPE_WITH_SINGLE_GENERIC_TYPE_REGEXP = "[a-zA-Z0-9_.]*<[a-zA-Z0-9_.]*>";
    private static final Pattern SINGLE_GENERIC_TYPE_REGEXP = Pattern.compile("<([a-zA-Z0-9_.]*)>");

    public static Object extractSingeGenericType(String typeName) {

        if (!typeName.matches(TYPE_WITH_SINGLE_GENERIC_TYPE_REGEXP)) {
            throw new DtoGeneratorException("Next type must have single generic type: '" + typeName + "'");
        }

        Matcher matcher = SINGLE_GENERIC_TYPE_REGEXP.matcher(typeName);

        if (!matcher.find()) {
            throw new DtoGeneratorException("Cannot find generic type using next regex pattern: "
                    + SINGLE_GENERIC_TYPE_REGEXP.pattern() + " in type: '" + typeName + "'");
        }
        return getClass(matcher.group(1), typeName);
    }

    public static Class<?> getSingleGenericType(Field field) throws DtoGeneratorException {
        return (Class<?>) extractSingeGenericType(field.getGenericType().getTypeName());
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
