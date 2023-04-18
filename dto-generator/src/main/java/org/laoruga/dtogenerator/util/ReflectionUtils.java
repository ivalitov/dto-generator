package org.laoruga.dtogenerator.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.rules.Entry;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.constants.RulesInstance;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

/**
 * @author Il'dar Valitov
 * Created on 25.05.2022
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {
    private static final String TYPE_WITH_SINGLE_GENERIC_TYPE_REGEXP = "[a-zA-Z0-9_.]*<[$a-zA-Z0-9_.]*>";
    private static final String TYPE_WITH_PAIRED_GENERIC_TYPE_REGEXP = "[a-zA-Z0-9_.]*<[$a-zA-Z0-9_.]*, [$a-zA-Z0-9_.]*>";
    private static final Pattern SINGLE_GENERIC_TYPE_REGEXP = Pattern.compile("<([$a-zA-Z0-9_.]*)>");
    private static final Pattern PAIRED_GENERIC_TYPE_REGEXP = Pattern.compile("<([$a-zA-Z0-9_.]*), ([$a-zA-Z0-9_.]*)>");
    private static final Pattern ARRAY_TYPE_REGEXP = Pattern.compile("\\[L([$a-zA-Z0-9_.]*);");

    private static final Map<Class<?>, Class<?>> PRIMITIVE_ARRAYS;

    static {
        Map<Class<?>, Class<?>> primitives = new HashMap<>();
        primitives.put(byte[].class, Byte.TYPE);
        primitives.put(short[].class, Short.TYPE);
        primitives.put(char[].class, Character.TYPE);
        primitives.put(int[].class, Integer.TYPE);
        primitives.put(long[].class, Long.TYPE);
        primitives.put(float[].class, Float.TYPE);
        primitives.put(double[].class, Double.TYPE);
        primitives.put(boolean[].class, Boolean.TYPE);
        PRIMITIVE_ARRAYS = ImmutableMap.copyOf(primitives);
    }

    public static Class<?> extractSingeGenericType(String typeName) {

        if (!typeName.matches(TYPE_WITH_SINGLE_GENERIC_TYPE_REGEXP)) {
            throw new DtoGeneratorException("Next type must have single generic type: '" + typeName + "'");
        }

        Matcher matcher = SINGLE_GENERIC_TYPE_REGEXP.matcher(typeName);

        if (!matcher.find()) {
            throw new DtoGeneratorException("Unexpected error. Cannot find generic type using next regex pattern: "
                    + SINGLE_GENERIC_TYPE_REGEXP.pattern() + " in type: '" + typeName + "'");
        }
        return getClass(matcher.group(1), typeName);
    }

    public static Class<?>[] extractPairedGenericType(String typeName) {

        if (!typeName.matches(TYPE_WITH_PAIRED_GENERIC_TYPE_REGEXP)) {
            throw new DtoGeneratorException("Next type must have pair of generic type: '" + typeName + "'");
        }

        Matcher matcher = PAIRED_GENERIC_TYPE_REGEXP.matcher(typeName);

        if (!matcher.find()) {
            throw new DtoGeneratorException("Cannot find pair of generic types using next regex pattern: "
                    + PAIRED_GENERIC_TYPE_REGEXP.pattern() + " in type: '" + typeName + "'");
        }
        return new Class[]{getClass(matcher.group(1), typeName), getClass(matcher.group(2), typeName)};
    }

    public static Class<?> getSingleGenericType(Field field) throws DtoGeneratorException {
        return extractSingeGenericType(field.getGenericType().getTypeName());
    }

    public static Class<?>[] getPairedGenericType(Field field) throws DtoGeneratorException {
        return extractPairedGenericType(field.getGenericType().getTypeName());
    }

    public static Class<?> getArrayElementType(Class<?> arrayType) throws DtoGeneratorException {

        if (PRIMITIVE_ARRAYS.containsKey(arrayType)) {
            return PRIMITIVE_ARRAYS.get(arrayType);
        }

        String typeName = arrayType.getName();

        Matcher matcher = ARRAY_TYPE_REGEXP.matcher(typeName);

        if (!matcher.find()) {
            throw new DtoGeneratorException("Cannot find array element type using next regex pattern: "
                    + ARRAY_TYPE_REGEXP.pattern() + " in type: '" + typeName + "'");
        }

        return getClass(matcher.group(1), typeName);
    }

    private static Class<?> getClass(String className, String typeName) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DtoGeneratorException("Can't create class for name: '" + className + "' obtained from" +
                    " type: '" + typeName + "'", e);
        }
    }

    /**
     * @param classToCreate   class to instantiate
     * @param constructorArgs constructor arguments or empty
     * @param <T>             type of instantiating class
     * @return class instance
     */
    @SuppressWarnings("unchecked")

    public static <T> T createInstance(Class<T> classToCreate, Object... constructorArgs) {

        if (classToCreate.isInterface() || Modifier.isAbstract(classToCreate.getModifiers())) {
            throw new DtoGeneratorException("Can't create instance of '" + classToCreate + "' because" +
                    " it is interface or abstract.");
        }

        Optional<Constructor<?>> suitableConstructor;
        try {

            suitableConstructor = Arrays.stream(classToCreate.getDeclaredConstructors())
                    .filter(constructor ->
                            constructor.getParameterCount() == constructorArgs.length &&
                                    isTypesAssignableFromObjects(constructor.getParameterTypes(), constructorArgs))
                    .findAny();

            if (!suitableConstructor.isPresent()) {
                String failedMsg = constructorArgs.length == 0 ? "Class must have no-args constructor." :
                        "Class must have constructor with params: " +
                                Arrays.stream(constructorArgs)
                                        .map(arg -> arg.getClass().toString())
                                        .collect(joining(",", "'", "'"));

                throw new DtoGeneratorException(failedMsg);
            }

            Constructor<?> constructor = suitableConstructor.get();
            constructor.setAccessible(true);
            return (T) constructor.newInstance(constructorArgs);

        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to instantiate class: '" + classToCreate + "'", e);
        }
    }

    private static boolean isTypesAssignableFromObjects(Class<?>[] types, Object[] objects) {

        if (types.length != objects.length) {
            throw new IllegalArgumentException("Arg lengths must be the same.");
        }

        for (int i = 0; i < types.length; i++) {
            if (!(types[i].isAssignableFrom(objects[i].getClass()))) {
                return false;
            }
        }

        return true;
    }


    @SuppressWarnings("unchecked")
    public static <T> T[] invokeMethodReturningArray(Object sourceClass, String fieldName, Class<T> returnedType) {
        try {
            Method method = sourceClass.getClass().getMethod(fieldName);
            return (T[]) method.invoke(sourceClass);
        } catch (Exception e) {
            throw new DtoGeneratorException("Unable to get value of the field: '" + fieldName + "'", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callStaticMethod(String methodName, Class<?> sourceClass, Class<T> returnType) {
        try {
            Method method = sourceClass.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return (T) method.invoke(sourceClass);
        } catch (Exception e) {
            throw new DtoGeneratorException("Error during invocation of static method: '" + methodName + "'" +
                    " of class: '" + sourceClass.getName() + "", e);
        }
    }

    public static Annotation getSingleRuleFromEntryOrDefaultForType(Entry mapRule, Class<?> requiredType) throws DtoGeneratorValidationException {
        Class<? extends Annotation> clazz = mapRule.annotationType();
        Annotation found = null;
        for (Method method : clazz.getMethods()) {
            if (method.getName().endsWith("Rule")) {
                Annotation[] values =
                        invokeMethodReturningArray(mapRule, method.getName(), Annotation.class);
                if (values.length >= 1) {
                    if (values.length > 1 || found != null) {
                        throw new DtoGeneratorValidationException("More than one annotation found in: '" + mapRule + "'");
                    }
                    found = values[0];
                }
            }
        }

        if (found != null) {
            return found;
        }

        Optional<Class<? extends Annotation>> rulesClass = GeneratedTypes.getRulesClass(Primitives.wrap(requiredType));
        if (rulesClass.isPresent() && RulesInstance.INSTANCES_MAP.containsKey(rulesClass.get())) {
            found = RulesInstance.INSTANCES_MAP.get(rulesClass.get());
        } else {
            throw new DtoGeneratorValidationException("Empty '@" + Entry.class.getSimpleName() + "' annotation," +
                    " but failed to select @Rules annotation by type: '" + requiredType + "'");
        }

        return found;
    }

    public static Field getField(Class<?> from, String fieldName) {
        try {
            return from.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new DtoGeneratorException("Field '" + fieldName + "' not found in the class: '" + from.getName() + "'", e);
        }
    }

    public static Class<?> getFieldType(String[] fields, int initialIdx, Class<?> initialType) {

        if (fields.length == initialIdx) {
            return initialType;
        }

        return getFieldType(
                fields,
                initialIdx + 1,
                ReflectionUtils.getFieldReclusive(initialType, fields[initialIdx], true).getType()
        );
    }

    private static Field getFieldReclusive(Class<?> fromClass, String fieldName, boolean upper) {
        Field field;

        if (fromClass == Object.class) {
            field = null;
        } else {
            try {
                field = fromClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = getFieldReclusive(fromClass.getSuperclass(), fieldName, false);
            }
        }

        if (upper && field == null) {
            throw new DtoGeneratorException("Field '" + fieldName + "'" +
                    " not found in the class: '" + fromClass.getName() + "'");
        }

        return field;
    }

}
