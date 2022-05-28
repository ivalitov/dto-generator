package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.NestedDtoGenerator;
import laoruga.dtogenerator.api.markup.generators.*;
import laoruga.dtogenerator.api.markup.rules.*;
import laoruga.dtogenerator.api.utils.ReflectionUtils;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator {

    private Object dtoInstance;

    private final String[] fieldsFromRoot;
    private final GeneratorBuildersProvider generatorBuildersProvider;

    private final Map<Field, Exception> errors = new HashMap<>();
    private final Map<Field, IGenerator<?>> fieldGeneratorMap = new LinkedHashMap<>();

    private final DtoGeneratorBuilder builderInstance;

    protected DtoGenerator(String[] fieldsFromRoot,
                           GeneratorBuildersProvider generatorBuildersProvider,
                           DtoGeneratorBuilder dtoGeneratorBuilder) {
        this.fieldsFromRoot = fieldsFromRoot;
        this.generatorBuildersProvider = generatorBuildersProvider;
        this.builderInstance = dtoGeneratorBuilder;
    }

    public static DtoGeneratorBuilder builder() {
        return new DtoGeneratorBuilder();
    }


    public <T> T generateDto(Class<T> dtoClass) {
        createDtoInstance(dtoClass);
        prepareGenerators();
        applyGenerators();
        return (T) dtoInstance;
    }

    public <T> T generateDto(T dtoInstance) {
        this.dtoInstance = dtoInstance;
        prepareGenerators();
        applyGenerators();
        return dtoInstance;
    }

    void createDtoInstance(Class<?> dtoClass) {
        try {
            Constructor<?>[] declaredConstructors = dtoClass.getDeclaredConstructors();
            if (declaredConstructors.length == 0) {
                throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                        "Class don't have public constructors. It must have public no-args constructor.");
            }
            Optional<Constructor<?>> maybeNoArgsConstructor = Arrays.stream(declaredConstructors)
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .findAny();
            if (!maybeNoArgsConstructor.isPresent()) {
                throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                        "Class must have public no-args constructor.");
            }
            Constructor<?> constructor = maybeNoArgsConstructor.get();
            boolean isAccessible = constructor.isAccessible();
            constructor.setAccessible(true);
            dtoInstance = constructor.newInstance();
            constructor.setAccessible(isAccessible);
        } catch (InstantiationException ie) {
            throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                    "Maybe no-args constructor was not found.", ie);
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'.", e);
        }
    }

    /**
     * Check whether DTO is ready for using CustomGeneratorDtoDependent or not.
     * There is limited attempts to prevent infinite loops.
     *
     * @param generator   - generator to check
     * @param attempts    - attempts counter
     * @param maxAttempts - max limit
     * @return - doesn't DTO ready?
     * @throws DtoGeneratorException - throws if all attempts are spent
     */
    private boolean doesDtoDependentGeneratorNotReady(IGenerator<?> generator,
                                                      AtomicInteger attempts,
                                                      AtomicInteger maxAttempts) throws DtoGeneratorException {
        if (generator instanceof ICustomGeneratorDtoDependent) {
            if (!((ICustomGeneratorDtoDependent<?, ?>) generator).isDtoReady()) {
                if (attempts.get() < maxAttempts.get() - 1) {
                    log.debug("Object is not ready to generate dependent field value");
                    return true;
                } else {
                    throw new DtoGeneratorException("Generator " + generator.getClass() +
                            " wasn't prepared in " + attempts + " attempts");
                }
            }
        }
        return false;
    }

    void applyGenerators() {
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicInteger maxAttempts = new AtomicInteger(100);
        while (!getFieldGeneratorMap().isEmpty() && attempts.get() < maxAttempts.get()) {
            attempts.incrementAndGet();
            log.debug("Attempt {} to generate field values", attempts);
            Iterator<Map.Entry<Field, IGenerator<?>>> iterator = getFieldGeneratorMap().entrySet().iterator();
            while (iterator.hasNext()) {

                Map.Entry<Field, IGenerator<?>> nextFieldAndGenerator = iterator.next();
                Field field = nextFieldAndGenerator.getKey();
                IGenerator<?> generator = nextFieldAndGenerator.getValue();
                try {
                    // if it's dto dependent generator - checking if dto is ready for this generator
                    if (generator instanceof ICustomGeneratorDtoDependent) {
                        if (doesDtoDependentGeneratorNotReady(generator, attempts, maxAttempts)) {
                            continue;
                        }
                    }
                    // if it's collection generator + dto dependent generator - checking if dto is ready for this generator
                    if (generator instanceof ICollectionGenerator) {
                        IGenerator<?> innerGenerator = ((ICollectionGenerator<?>) generator).getInnerGenerator();
                        if (innerGenerator instanceof ICustomGeneratorDtoDependent) {
                            if (doesDtoDependentGeneratorNotReady(innerGenerator, attempts, maxAttempts)) {
                                continue;
                            }
                        }
                    }
                    boolean isFieldAccessible = field.isAccessible();
                    try {
                        if (!isFieldAccessible) field.setAccessible(true);
                        field.set(dtoInstance, generator.generate());
                    } catch (IllegalAccessException e) {
                        log.error("Access error while generation value for a field: " + field, e);
                        errors.put(field, e);
                    } catch (Exception e) {
                        log.error("Error while generation value for the field: " + field, e);
                        errors.put(field, e);
                    } finally {
                        iterator.remove();
                        if (!isFieldAccessible) field.setAccessible(false);
                    }
                } catch (Exception e) {
                    log.error("Error while generation value for the field: " + field, e);
                    errors.put(field, e);
                    iterator.remove();
                }

            }
        }
        if (!getFieldGeneratorMap().isEmpty() || !errors.isEmpty()) {
            if (!getFieldGeneratorMap().isEmpty()) {
                log.error("Unexpected state. There {} unused generator(s) left. Fields vs Generators: " +
                        getFieldGeneratorMap(), getFieldGeneratorMap().size());
            }
            if (!errors.isEmpty()) {
                log.error("{} error(s) while generators execution. Fields vs Generators: " + errors, errors.size());
            }
            throw new RuntimeException("Error while generators execution (see log above)");
        }

    }

    void prepareGenerators() {
        for (Field field : dtoInstance.getClass().getDeclaredFields()) {
            IGenerator<?> generator = prepareGenerator(field);
            if (generator != null) {
                getFieldGeneratorMap().put(field, generator);
            }
        }
        if (!errors.isEmpty()) {
            int i = 0;
            final AtomicInteger counter = new AtomicInteger(0);
            String formattedErrors = errors.entrySet().stream()
                    .map(fieldExceptionEntry ->
                            "- [" + counter.incrementAndGet() + "] Field: '" + fieldExceptionEntry.getKey().toString() + "'\n" +
                                    "- [" + counter.get() + "] Exception:\n" +
                                    ExceptionUtils.getStackTrace(fieldExceptionEntry.getValue())
                    )
                    .collect(Collectors.joining("\n"));
            log.error("{} error(s) while generators preparation. See problems below: \n" + formattedErrors, errors.size());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (getFieldGeneratorMap().isEmpty()) {
            log.debug("No generators have been found");
        } else {
            log.debug(getFieldGeneratorMap().size() + " generators was created for fields: " + getFieldGeneratorMap().keySet());
        }
    }

    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = null;
        try {
            RulesType rulesType = checkFieldAnnotations(field);
            switch (rulesType) {
                case BASIC:
                    generator = selectBasicGenerator(field);
                    break;
                case CUSTOM:
                    generator = selectCustomGenerator(field);
                    break;
                case COLLECTION_BASIC:
                    IGenerator<?> collectionItemGenerator = selectBasicGenerator(
                            ReflectionUtils.getGenericType(field),
                            field.getName() + " " + field.getGenericType().getClass(),
                            field.getDeclaredAnnotations()
                    );
                    generator = selectCollectionGenerator(field, collectionItemGenerator);
                    break;
                case MAP_BASIC:
                    Pair<Class<?>, Class<?>> genericTypesPair = ReflectionUtils.getGenericTypesPair(field);
                    IGenerator<?> mapKeyGenerator = selectBasicGenerator(
                            genericTypesPair.getKey(),
                            field.getName() + " " + field.getGenericType().getClass(),
                            field.getDeclaredAnnotations()
                    );
                    IGenerator<?> mapValueGenerator = selectBasicGenerator(
                            genericTypesPair.getValue(),
                            field.getName() + " " + field.getGenericType().getClass(),
                            field.getDeclaredAnnotations()
                    );
                    generator = selectMapGenerator(field, mapKeyGenerator, mapValueGenerator);
                    break;
                case COLLECTION_CUSTOM:
                    generator = selectCollectionGenerator(field, selectCustomGenerator(field));
                    break;
                case NOT_ANNOTATED:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + rulesType);
            }
        } catch (Exception e) {
            errors.put(field, e);
        }
        prepareCustomRemarks(generator);
        return generator;
    }

    void prepareCustomRemarks(IGenerator<?> generator) {
        if (generator instanceof ICustomGeneratorRemarkable) {
            ICustomGeneratorRemarkable<?> remarkableGenerator = (ICustomGeneratorRemarkable<?>) generator;
            if (generatorBuildersProvider.getGeneratorRemarksProvider().isCustomRuleRemarkExists(remarkableGenerator)) {
                remarkableGenerator.setRuleRemarks(generatorBuildersProvider.getGeneratorRemarksProvider()
                        .getCustomRuleRemarks(remarkableGenerator));
            }
        }
    }

    enum RulesType {
        BASIC,
        CUSTOM,
        COLLECTION_BASIC,
        COLLECTION_CUSTOM,
        MAP_BASIC,
        NOT_ANNOTATED
    }

    @Value
    static class CollectionRuleWrapper {
        Annotation collectionGenerationRules;
        Annotation itemGenerationRules;
    }

    Map<String, CollectionRuleWrapper> map = new LinkedHashMap<>();

    /**
     * Correctness checks and evaluation of field's annotations type.
     *
     * @param field field to check
     * @return type field's annotations
     */
    private RulesType checkFieldAnnotations(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        String fieldName = field.getName();
        if (annotations.length == 0) {
            return RulesType.NOT_ANNOTATED;
        }
        List<Annotation> generationRules = Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(Rule.class) != null)
                .collect(Collectors.toList());
        if (generationRules.isEmpty()) {
            return RulesType.NOT_ANNOTATED;
        }
        if (generationRules.size() == 1) {
            Class<? extends Annotation> rulesClass = generationRules.get(0).annotationType();
            if (rulesClass == CustomGenerator.class) {
                return RulesType.CUSTOM;
            }
            if (rulesClass.getDeclaredAnnotation(RuleForCollection.class) != null) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with collection generation rules, " +
                        " but not annotated with collection's item generation rules. There is also generation rules annotation expected.");
            }
            return RulesType.BASIC;
        }
        if (generationRules.size() == 2) {
            generationRules.sort(Comparator.comparing(DtoGenerator::isItCollectionRule));
            Annotation itemRule = generationRules.get(0);
            Annotation collectionRule = generationRules.get(1);
            if (isItCollectionRule(itemRule)) {
                throw new DtoGeneratorException("Field '" + fieldName + "'annotated with 2 collection generation rules." +
                        " There are no more than one RuleForCollection annotation expected");
            }
            if (!isItCollectionRule(collectionRule)) {
                throw new DtoGeneratorException("Field '" + fieldName + "'annotated with 2 generation rules " +
                        " non of which is RuleForCollection annotation.");
            }
            map.put(fieldName, new CollectionRuleWrapper(collectionRule, itemRule));
            if (isItCustomRule(itemRule)) {
                return RulesType.COLLECTION_CUSTOM;
            } else {
                return RulesType.COLLECTION_BASIC;
            }

        } else {
            throw new DtoGeneratorException("Field '" + fieldName + "'  annotated with '" + generationRules.size() +
                    "' generation rules" +
                    " annotations. No more than '2' expected.");
        }
    }

    private static boolean isItCollectionRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) != null;
    }

    private static boolean isItCustomRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == CustomGenerator.class;
    }

        /*
    1) если коллекция
    2) если есть ли аннотация
    3) если есть класс листа и поле == null - создать коллецию из класса (передать её в генератор для заполнения)
    4) если нет класса листа и поле null:
    5)      если интерфейс - ошибка
    6)      если класс листа - создать инстанс списка (передать в генератор для заполнения)

    7) выбрать генератор:
    8) если есть класс - создать кастонмый генератор как обычно
    9) если нет - выбрать дефлотный генератор иначе ошибка
    10) обернуть дефоллтный генератор в List генератор

    11) лист генератор - выполняет дефолтный генератор столько раз, сколько указано когда наступает его время

     */

    private IGenerator<?> selectCollectionGenerator(Field field, IGenerator<?> listItemGenerator) throws DtoGeneratorException {

        Class<?> fieldType = field.getType();
        String fieldName = field.getName();

        if (List.class.isAssignableFrom(fieldType)) {
            ListRules rules = field.getAnnotation(ListRules.class);
            if (rules != null) {
                return generatorBuildersProvider.getListGenerator(fieldName, fieldType, rules, listItemGenerator);
            }
        }

        throw new DtoGeneratorException("Field " + field + " hasn't been mapped with any collection generator.");
    }

    private IGenerator<?> selectMapGenerator(Field field, IGenerator<?> mapKeyGenerator, IGenerator<?> mapValueGenerator) {
        return null;
    }

    private static <T extends Annotation> T getAnnotationOrNull(Class<?> annotationClass, T[] declaredAnnotations) {
        for (T annotation : declaredAnnotations) {
            if (annotation.annotationType() == annotationClass) {
                return annotation;
            }
        }
        return null;
    }

    private IGenerator<?> selectBasicGenerator(Field field) throws DtoGeneratorException {
        return selectBasicGenerator(field.getType(), field.getName(), field.getDeclaredAnnotations());
    }

    private IGenerator<?> selectBasicGenerator(Class<?> fieldType, String fieldName, Annotation[] fieldAnnotations) throws DtoGeneratorException {

        if (fieldType == Double.class || fieldType == Double.TYPE) {
            DoubleRules rules = (DoubleRules) getAnnotationOrNull(DoubleRules.class, fieldAnnotations);
            if (rules != null) {
                return generatorBuildersProvider.getDoubleGenerator(fieldName, rules, fieldType == Double.TYPE);
            }
        }

        if (fieldType == String.class) {
            StringRules rules = (StringRules) getAnnotationOrNull(StringRules.class, fieldAnnotations);
            if (rules != null) {
                return generatorBuildersProvider.getStringGenerator(fieldName, rules);
            }
        }

        if (fieldType == Integer.class || fieldType == Integer.TYPE) {
            IntegerRules rules = (IntegerRules) getAnnotationOrNull(IntegerRules.class, fieldAnnotations);
            if (rules != null) {
                return generatorBuildersProvider.getIntegerGenerator(fieldName, rules, fieldType == Integer.TYPE);
            }
        }

        if (fieldType == Long.class || fieldType == Long.TYPE) {
            LongRules rules = (LongRules) getAnnotationOrNull(LongRules.class, fieldAnnotations);
            if (rules != null) {
                return generatorBuildersProvider.getLongGenerator(fieldName, rules, fieldType == Integer.TYPE);
            }
        }

        if (fieldType.isEnum()) {
            EnumRules rules = (EnumRules) getAnnotationOrNull(EnumRules.class, fieldAnnotations);
            if (rules != null) {
                return generatorBuildersProvider.getEnumGenerator(fieldName, rules);
            }
        }

        if (fieldType == LocalDateTime.class) {
            LocalDateTimeRules rules = (LocalDateTimeRules) getAnnotationOrNull(LocalDateTimeRules.class, fieldAnnotations);
            if (rules != null) {
                return generatorBuildersProvider.getLocalDateTimeGenerator(fieldName, rules);
            }
        }

        NestedDtoRules nestedDtoRules = (NestedDtoRules) getAnnotationOrNull(NestedDtoRules.class, fieldAnnotations);
        if (nestedDtoRules != null) {
            builderInstance.build();
            String[] pathToNestedDtoField = Arrays.copyOf(fieldsFromRoot, fieldsFromRoot.length + 1);
            pathToNestedDtoField[fieldsFromRoot.length] = fieldName;
            return new NestedDtoGenerator<>(
                    builderInstance.buildNestedFieldGenerator(pathToNestedDtoField), fieldType);
        }

        throw new DtoGeneratorException("Field " + fieldName + " hasn't been mapped with any basic generator.");
    }

    private IGenerator<?> selectCustomGenerator(Field field) throws DtoGeneratorException {
        CustomGenerator customGeneratorRules = field.getAnnotation(CustomGenerator.class);
        Class<?> generatorClass = null;
        try {
            generatorClass = customGeneratorRules.generatorClass();
            Object generatorInstance = generatorClass.newInstance();
            if (generatorInstance instanceof ICustomGeneratorArgs) {
                log.debug("Args {} have been obtained from Annotation: {}",
                        Arrays.asList(customGeneratorRules.args()), customGeneratorRules);
                ((ICustomGeneratorArgs<?>) generatorInstance).setArgs(customGeneratorRules.args());
            }
            if (generatorInstance instanceof ICustomGeneratorDtoDependent) {
                try {
                    ((ICustomGeneratorDtoDependent) generatorInstance).setDto(dtoInstance);
                } catch (ClassCastException e) {
                    throw new DtoGeneratorException("ClassCastException while trying to set basic DTO into " +
                            "DTO dependent custom generator. Perhaps there is wrong argument type is passing into " +
                            "'setDto' method of generator class. " +
                            "Generator class: '" + generatorInstance.getClass() + "', " +
                            "Passing argument type: '" + dtoInstance.getClass() + "'", e);
                } catch (Exception e) {
                    throw new DtoGeneratorException("Exception was thrown while trying to set DTO into " +
                            "DTO dependent custom generator: " + generatorInstance.getClass(), e);
                }
            }
            if (generatorInstance instanceof ICustomGenerator) {
                return (ICustomGenerator<?>) generatorInstance;
            } else {
                throw new DtoGeneratorException("Failed to prepare custom generator. " +
                        "Custom generator must implements: '" + ICustomGenerator.class + "' or it's heirs.");
            }
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while preparing custom generator from class: " + generatorClass, e);
        }
    }

    protected Map<Field, IGenerator<?>> getFieldGeneratorMap() {
        return fieldGeneratorMap;
    }

}
