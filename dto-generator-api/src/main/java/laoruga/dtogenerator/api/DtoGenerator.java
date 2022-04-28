package laoruga.dtogenerator.api;

import com.sun.istack.internal.Nullable;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.BasicTypeGenerators;
import laoruga.dtogenerator.api.generators.NestedDtoGenerator;
import laoruga.dtogenerator.api.markup.generators.*;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static laoruga.dtogenerator.api.markup.remarks.RuleRemark.MIN_VALUE;
import static laoruga.dtogenerator.api.markup.remarks.RuleRemark.NULL_VALUE;

@Slf4j
public class DtoGenerator {

    private Object dtoInstance;

    private final Map<Field, Exception> errors = new HashMap<>();
    private final Map<Field, IGenerator<?>> fieldIGeneratorMap = new LinkedHashMap<>();

    private final IRuleRemark ruleRemark;
    private final Map<String, IRuleRemark> fieldRuleRemarkMap;

    private final DtoGeneratorBuilder builderInstance;

    protected DtoGenerator(IRuleRemark ruleRemark,
                           Map<String, IRuleRemark> fieldRuleRemarkMap,
                           DtoGeneratorBuilder dtoGeneratorBuilder) {
        this.ruleRemark = ruleRemark;
        this.fieldRuleRemarkMap = fieldRuleRemarkMap;
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
        return (T) dtoInstance;
    }

    void createDtoInstance(Class<?> dtoClass) {
        try {
            if (dtoClass.getConstructors().length == 0) {
                throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                        "Class don't have public constructors. It must have public no-args constructor.");
            }
            Optional<Constructor<?>> maybeNoArgsConstructor = Arrays.stream(dtoClass.getConstructors())
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
        while (!fieldIGeneratorMap.isEmpty() && attempts.get() < maxAttempts.get()) {
            attempts.incrementAndGet();
            log.debug("Attempt {} to generate field values", attempts);
            Iterator<Map.Entry<Field, IGenerator<?>>> iterator = fieldIGeneratorMap.entrySet().iterator();
            while (iterator.hasNext()) {

                Map.Entry<Field, IGenerator<?>> nextFieldAndGenerator = iterator.next();
                Field field = nextFieldAndGenerator.getKey();
                IGenerator<?> generator = nextFieldAndGenerator.getValue();
                try {
                    if (generator instanceof ICollectionGenerator) {
                        IGenerator<?> innerGenerator = ((ICollectionGenerator<?>) generator).getInnerGenerator();
                        if (innerGenerator instanceof ICustomGeneratorDtoDependent) {
                            if (doesDtoDependentGeneratorNotReady(innerGenerator, attempts, maxAttempts)) {
                                continue;
                            }
                        }
                    }
                    if (generator instanceof ICustomGeneratorDtoDependent) {
                        if (doesDtoDependentGeneratorNotReady(generator, attempts, maxAttempts)) {
                            continue;
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
        if (!fieldIGeneratorMap.isEmpty() || !errors.isEmpty()) {
            if (!fieldIGeneratorMap.isEmpty()) {
                log.error("Unexpected state. There {} unused generator(s) left. Fileds vs Generators: " +
                        fieldIGeneratorMap, fieldIGeneratorMap.size());
            }
            if (!errors.isEmpty()) {
                log.error("{} error(s) while generators execution. Fileds vs Generators: " + errors, errors.size());
            }
            throw new RuntimeException("Error while generators execution (see log above)");
        }

    }

    void prepareGenerators() {
        for (Field field : dtoInstance.getClass().getDeclaredFields()) {
            IGenerator<?> generator = prepareGenerator(field);
            if (generator != null) {
                fieldIGeneratorMap.put(field, generator);
            }
        }
        if (!errors.isEmpty()) {
            log.error("{} error(s) while generators preparation. Fileds vs Generators: " + errors, errors.size());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (fieldIGeneratorMap.isEmpty()) {
            log.debug("No generators have been found");
        } else {
            log.debug(fieldIGeneratorMap.size() + " generators was created for fields: " + fieldIGeneratorMap.keySet());
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
                    generator = selectCollectionGenerator(field, selectBasicGenerator(field));
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
        return generator;
    }

    private IRuleRemark getBasicRuleRemark(Field field) {
        if (fieldRuleRemarkMap.containsKey(field.getName())) {
            return fieldRuleRemarkMap.get(field.getName());
        }
        return ruleRemark;
    }

    enum RulesType {
        BASIC,
        CUSTOM,
        COLLECTION_BASIC,
        COLLECTION_CUSTOM,
        NOT_ANNOTATED
    }

    /**
     * Correctness checks and evaluation of field's annotations type.
     *
     * @param field field to check
     * @return type field's annotations
     */
    private RulesType checkFieldAnnotations(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
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
                throw new DtoGeneratorException("Field '" + field.getName() + "'annotated with only collection generation rules." +
                        " There is second generation rules annotation expected.");
            }
            return RulesType.BASIC;
        }
        if (generationRules.size() == 2) {
            int collectionCount = (int) generationRules.stream()
                    .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) == null)
                    .count();
            if (collectionCount == 1) {
                boolean customGenerator = generationRules.stream()
                        .anyMatch(annotation -> annotation.annotationType().getDeclaredAnnotation(CustomGenerator.class) != null);
                if (customGenerator) {
                    return RulesType.COLLECTION_CUSTOM;
                } else {
                    return RulesType.COLLECTION_BASIC;
                }
            } else if (collectionCount == 2) {
                throw new DtoGeneratorException("Field '" + field.getName() + "'annotated with 2 collection generation rules." +
                        " There are no more than one collection annotation expected");
            } else {
                throw new DtoGeneratorException("Field '" + field.getName() + "'annotated with 2 collection generation rules " +
                        " non of which is collection annotation.");
            }
        } else {
            throw new DtoGeneratorException("Field '" + field.getName() + "'  annotated with '" + generationRules.size() +
                    "' generation rules" +
                    " annotations. No more than '2' expected.");
        }
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

    /**
     * 1. Filed type should be assignable from Collection.class
     * 2. Field should be annotated with any of @Rules annotation
     *
     * @param field checking dto field
     */
    private void createCollectionFieldInstance(Field field) {
        Class<?> type = field.getType();
        if (Collection.class.isAssignableFrom(type)) {

            if (type.isInterface()) {

            } else {

            }
        }
    }

    private IGenerator<?> selectCollectionGenerator(Field field, IGenerator<?> listItemGenerator) {

        Class<?> fieldType = field.getType();

        if (List.class.isAssignableFrom(fieldType)) {
            ListRules listRules = field.getAnnotation(ListRules.class);
            if (listRules != null) {
                Class<? extends List> listClass = listRules.listClass();
                if (!dtoInstance.getClass().isAssignableFrom(listClass)) {
                    throw new DtoGeneratorException("ListClass from rules: '" + listRules + "' can't" +
                            " be assign to the field: " + fieldType);
                }
                if (listClass.isInterface() || Modifier.isAbstract(listClass.getModifiers())) {
                    throw new DtoGeneratorException("Can't create instance of '" + listClass + "' because" +
                            " it is interface or abstract.");
                }
                List<?> listInstance;
                try {
                    listInstance = listClass.newInstance();
                } catch (Exception e) {
                    log.error("Exception while creating Collection instance ", e);
                    throw new DtoGeneratorException(e);
                }
                return new BasicTypeGenerators.ListGenerator(
                        listInstance,
                        listItemGenerator);
            }
        }

        throw new DtoGeneratorException("Field " + field + " hasn't been mapped with any collection generator.");
    }

    private @Nullable
    IGenerator<?> selectBasicGenerator(Field field) {

        Class<?> fieldType = field.getType();

        if (fieldType == Double.class || fieldType == Double.TYPE) {
            DoubleRules doubleBounds = field.getAnnotation(DoubleRules.class);
            if (doubleBounds != null) {
                IRuleRemark basicRuleRemark = getBasicRuleRemark(field);
                double minValue = doubleBounds.minValue();
                if (basicRuleRemark == NULL_VALUE && fieldType == Double.TYPE) {
                    log.debug("Doubel primitive field '" + field.getName() + "' can't be null, it will be assigned " +
                            " to DoubleRules.DEFAULT_MIN");
                    basicRuleRemark = MIN_VALUE;
                    minValue = DoubleRules.DEFAULT_MIN;
                }
                return new BasicTypeGenerators.DoubleGenerator(
                        doubleBounds.maxValue(),
                        minValue,
                        doubleBounds.precision(),
                        basicRuleRemark
                );
            }
        }

        if (fieldType == String.class) {
            StringRules stringBounds = field.getAnnotation(StringRules.class);
            if (stringBounds != null) {
                return new BasicTypeGenerators.StringGenerator(
                        stringBounds.maxSymbols(),
                        stringBounds.minSymbols(),
                        stringBounds.charset(),
                        stringBounds.chars(),
                        getBasicRuleRemark(field)
                );
            }
        }

        if (fieldType == Integer.class || fieldType == Integer.TYPE) {
            IntegerRules integerRules = field.getAnnotation(IntegerRules.class);
            if (integerRules != null) {
                IRuleRemark basicRuleRemark = getBasicRuleRemark(field);
                int minValue = integerRules.minValue();
                if (basicRuleRemark == NULL_VALUE && fieldType == Integer.TYPE) {
                    log.debug("Integer primitive field '" + field.getName() + "' can't be null, it will be assigned " +
                            " to IntegerRules.DEFAULT_MIN");
                    basicRuleRemark = MIN_VALUE;
                    minValue = IntegerRules.DEFAULT_MIN;
                }
                return new BasicTypeGenerators.IntegerGenerator(
                        integerRules.maxValue(),
                        minValue,
                        basicRuleRemark
                );
            }
        }

        if (fieldType == Long.class || fieldType == Long.TYPE) {
            LongRules longRules = field.getAnnotation(LongRules.class);
            if (longRules != null) {
                IRuleRemark basicRuleRemark = getBasicRuleRemark(field);
                long minValue = longRules.minValue();
                if (basicRuleRemark == NULL_VALUE && fieldType == Long.TYPE) {
                    log.debug("Long primitive field '" + field.getName() + "' can't be null, it will be assigned " +
                            " to LongRules.DEFAULT_MIN");
                    basicRuleRemark = MIN_VALUE;
                    minValue = LongRules.DEFAULT_MIN;
                }
                return new BasicTypeGenerators.LongGenerator(
                        longRules.maxValue(),
                        minValue,
                        basicRuleRemark
                );
            }
        }

        if (fieldType.isEnum()) {
            EnumRules enumBounds = field.getAnnotation(EnumRules.class);
            if (enumBounds != null) {
                return new BasicTypeGenerators.EnumGenerator(
                        enumBounds.possibleEnumNames(),
                        enumBounds.enumClass(),
                        getBasicRuleRemark(field)
                );
            }
        }

        if (fieldType == LocalDateTime.class) {
            LocalDateTimeRules enumBounds = field.getAnnotation(LocalDateTimeRules.class);
            if (enumBounds != null) {
                return new BasicTypeGenerators.LocalDateTimeGenerator(
                        enumBounds.leftShiftDays(),
                        enumBounds.rightShiftDays(),
                        getBasicRuleRemark(field)
                );
            }
        }

        NestedDtoRules nestedDtoRules = field.getAnnotation(NestedDtoRules.class);

        if (nestedDtoRules != null) {
            return new NestedDtoGenerator<>(builderInstance.build(), fieldType);
        }

        throw new DtoGeneratorException("Field " + field + " hasn't been mapped with any basic generator.");
    }

    private IGenerator<?> selectCustomGenerator(Field field) {
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

}
