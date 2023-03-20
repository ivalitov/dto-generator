package org.laoruga.dtogenerator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.rules.Entry;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
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

    public static Class<?> extractSingeGenericType(String typeName) {

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

    // TODO merge with previous method?

    /**
     * 1. Filed type should be assignable from required concreateClass
     * 2. CollectionClass should not be an interface or abstract
     *
     * @param concreteClass - class of collection
     * @param <T>            - collection element type
     * @return - new collection instance
     */
    public static <T> T createInstanceOfConcreteClass(Class<T> concreteClass) {
        if (concreteClass.isInterface() || Modifier.isAbstract(concreteClass.getModifiers())) {
            throw new DtoGeneratorException("Can't create instance of '" + concreteClass + "' because" +
                    " it is interface or abstract.");
        }
        T instance;
        try {
            instance = concreteClass.newInstance();
        } catch (Exception e) {
            log.error("Exception while creating instance ", e);
            throw new DtoGeneratorException(e);
        }
        return instance;
    }

    public static Object createInstanceOfConcreteClassAsObject(Class<?> concreteClass) {
        return createInstanceOfConcreteClass(concreteClass);
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

    /**
     * @param fieldType       - type of field to assign collectionClass instance
     * @param collectionClass - type to be assigned to the field
     */
    public static void assertTypeCompatibility(Class<?> fieldType, Class<?> collectionClass) {
        if (!fieldType.isAssignableFrom(collectionClass)) {
            throw new DtoGeneratorException("CollectionClass from rules: '" + collectionClass + "' can't" +
                    " be assign to the field: " + fieldType);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultMethodValue(Class<? extends Annotation> annotationClass,
                                              String methodName,
                                              Class<T> valueType) throws NoSuchMethodException {
        Method declaredMethod = annotationClass.getDeclaredMethod(methodName);
        Object defaultValue = declaredMethod.getDefaultValue();
        if (defaultValue.getClass() == valueType) {
            return (T) defaultValue;
        }
        throw new ClassCastException("Field");
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] getStaticFieldValueArray(Class<?> sourceClass, String fieldName, Class<T> fieldType) {
        try {
            Field field = sourceClass.getField(fieldName);
            return (T[]) field.get(sourceClass);
        } catch (Exception e) {
            throw new DtoGeneratorException("Unable to get value of the field: '" + fieldName + "'", e);
        }
    }

    public static <T> T[] invokeMethodReturningArray(Object sourceClass, String fieldName, Class<T> returnedType) {
        try {
            Method method = sourceClass.getClass().getMethod(fieldName);
            return (T[]) method.invoke(sourceClass);
        } catch (Exception e) {
            throw new DtoGeneratorException("Unable to get value of the field: '" + fieldName + "'", e);
        }
    }

    public static Annotation[] getRepeatableAnnotations(Annotation repeatableAnnotationSource) {
        try {
            Method value = repeatableAnnotationSource.annotationType().getMethod("value");
            Object arr = value.invoke(repeatableAnnotationSource);
            Annotation[] copy = new Annotation[Array.getLength(arr)];
            Array.getLength(arr);
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (Annotation) Array.get(arr, 0);
            }
            return copy;
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while extracting first of repeatable annotation", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callStaticMethod(String methodName, Class<?> sourceClass, Class<T> returnType) {
        try {
            return (T) sourceClass.getMethod(methodName).invoke(sourceClass);
        } catch (Exception e) {
            throw new DtoGeneratorException("Error during invocation of static method: '" + methodName + "'" +
                    " of class: '" + sourceClass.getName() + "" , e);
        }
    }

    public static Annotation getRuleOrNull(Entry mapRule) {
        Class<? extends Annotation> clazz = mapRule.annotationType();
        Annotation found = null;
        for (Method method : clazz.getMethods()) {
            if (method.getName().endsWith("Rule")) {
                Annotation[] values =
                        invokeMethodReturningArray(mapRule, method.getName(), Annotation.class);
                if (values.length >= 1) {
                    if (values.length > 1 || found != null) {
                        log.error("More than one annotation found in: " + mapRule);
                    }
                    found = values[0];
                }
            }
        }
        return found;
    }
}
