package org.laoruga.dtogenerator.constants;

import com.google.common.collect.ImmutableMap;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Il'dar Valitov
 * Created on 11.03.2023
 */
public class GeneratedTypes {

    private static final Map<Class<? extends Annotation>, Class<?>[]> GENERATED_TYPES;

    static {
        Map<Class<? extends Annotation>, Class<?>[]> generatedTypes = new HashMap<>();

        generatedTypes.put(StringRule.class, new Class[]{StringRule.GENERATED_TYPE});
        generatedTypes.put(IntegralRule.class, IntegralRule.GENERATED_TYPES);
        generatedTypes.put(DecimalRule.class, DecimalRule.GENERATED_TYPES);
        generatedTypes.put(DateTimeRule.class, new Class[]{DateTimeRule.GENERATED_TYPE});
        generatedTypes.put(EnumRule.class, new Class[]{EnumRule.GENERATED_TYPE});
        generatedTypes.put(BooleanRule.class, new Class[]{BooleanRule.GENERATED_TYPE});
        generatedTypes.put(CollectionRule.class, new Class[]{CollectionRule.GENERATED_TYPE});
        generatedTypes.put(MapRule.class, new Class[]{MapRule.GENERATED_TYPE});
        generatedTypes.put(ArrayRule.class, ArrayRule.GENERATED_TYPES);
        generatedTypes.put(CustomRule.class, new Class[]{CustomRule.GENERATED_TYPE});
        generatedTypes.put(NestedDtoRule.class, new Class[]{NestedDtoRule.GENERATED_TYPE});

        GENERATED_TYPES = ImmutableMap.copyOf(generatedTypes);
    }

    public static Class<?>[] get(Class<? extends Annotation> rules) {
        return Objects.requireNonNull(GENERATED_TYPES.get(rules),
                "Generated types wasn't added for rule: '" + rules + "'");
    }

    public static Optional<Class<? extends Annotation>> getRulesClass(final Class<?> requiredType) {
        return GENERATED_TYPES.entrySet().stream()
                .filter(e -> e.getKey() != CustomRule.class && e.getKey() != NestedDtoRule.class)
                .filter(e -> isAssignableFrom(e.getValue(), requiredType))
                .findFirst()
                .map(Map.Entry::getKey);
    }

    public static boolean isAssignableFrom(Class<?>[] possibleTypes, Class<?> generatedType) {
        for (Class<?> possibleType : possibleTypes) {
            if (possibleType.isAssignableFrom(generatedType)) {
                return true;
            }
        }
        return false;
    }

    public static Class<?> getAssignableType(Class<?>[] possibleTypes, Class<?> generatedType) {
        for (Class<?> possibleType : possibleTypes) {
            if (possibleType.isAssignableFrom(generatedType)) {
                return possibleType;
            }
        }
        throw new DtoGeneratorException("There is no types assignable from '" + generatedType +
                "' in the types list: " + Arrays.asList(possibleTypes));
    }
}
