package org.laoruga.dtogenerator.constants;

import com.google.common.collect.ImmutableMap;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 11.03.2023
 */
public class GeneratedTypes {

    private static final Map<Class<? extends Annotation>, Class<?>[]> GENERATED_TYPES;

    static {
        Map<Class<? extends Annotation>, Class<?>[]> generatedTypes = new HashMap<>();

        add(generatedTypes, StringRule.class);
        add(generatedTypes, NumberRule.class);
        add(generatedTypes, DoubleRule.class);
        add(generatedTypes, LocalDateTimeRule.class);
        add(generatedTypes, EnumRule.class);
        add(generatedTypes, CollectionRule.class);
        add(generatedTypes, BooleanRule.class);
        add(generatedTypes, CustomRule.class);
        add(generatedTypes, NestedDtoRule.class);

        GENERATED_TYPES = ImmutableMap.copyOf(generatedTypes);
    }

    private static void add(Map<Class<? extends Annotation>, Class<?>[]> generatedTypes,
                            Class<? extends Annotation> rulesAnnotationClass) {
        generatedTypes.put(
                rulesAnnotationClass,
                ReflectionUtils.getStaticFieldValueArray(rulesAnnotationClass, "GENERATED_TYPES", Class.class)
        );
    }

    public static Class<?>[] get(Class<? extends Annotation> rules) {
        return Objects.requireNonNull(GENERATED_TYPES.get(rules),
                "Generated types wasn't added for rule: '" + rules + "'");
    }

}
