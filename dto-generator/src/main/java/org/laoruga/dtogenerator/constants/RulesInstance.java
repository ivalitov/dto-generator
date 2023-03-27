package org.laoruga.dtogenerator.constants;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 30.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RulesInstance {

    @BooleanRule
    @StringRule
    @NumberRule
    @DecimalRule
    @EnumRule
    @DateTimeRule
    @CollectionRule(element = @Entry)
    @MapRule(key = @Entry, value = @Entry)
    @ArrayRule(element = @Entry)
    @CustomRule(generatorClass = Object.class)
    @NestedDtoRule
    private static final Object ANNOTATIONS = null;

    /*
     * Default Values
     */
    public static final BooleanRule BOOLEAN_RULE;
    public static final StringRule STRING_RULE;
    public static final NumberRule NUMBER_RULE;
    public static final EnumRule ENUM_RULE;
    public static final DecimalRule DECIMAL_RULE;
    public static final DateTimeRule DATE_TIME_RULE;
    public static final CollectionRule COLLECTION_RULE;
    public static final ArrayRule ARRAY_RULE;
    public static final MapRule MAP_RULE;
    public static final CustomRule CUSTOM_RULE;
    public static final NestedDtoRule NESTED_DTO_RULE;

    /*
     * Other
     */

    public static final NumberRule NUMBER_RULE_ZEROS;

    public static final Map<Class<? extends Annotation>, Annotation> INSTANCES_MAP;

    static {
        try {
            Field annotations = RulesInstance.class.getDeclaredField("ANNOTATIONS");
            Map<Class<? extends Annotation>, Annotation> instances = new HashMap<>();

            BOOLEAN_RULE = getAnnotationInstance(annotations, instances, BooleanRule.class);
            STRING_RULE = getAnnotationInstance(annotations, instances, StringRule.class);
            NUMBER_RULE = getAnnotationInstance(annotations, instances, NumberRule.class);
            NUMBER_RULE_ZEROS = getAnnotationInstance(annotations, instances, NumberRule.class);
            ENUM_RULE = getAnnotationInstance(annotations, instances, EnumRule.class);
            DECIMAL_RULE = getAnnotationInstance(annotations, instances, DecimalRule.class);
            DATE_TIME_RULE = getAnnotationInstance(annotations, instances, DateTimeRule.class);
            COLLECTION_RULE = getAnnotationInstance(annotations, instances, CollectionRule.class);
            MAP_RULE = getAnnotationInstance(annotations, instances, MapRule.class);
            ARRAY_RULE = getAnnotationInstance(annotations, instances, ArrayRule.class);
            CUSTOM_RULE = getAnnotationInstance(annotations, instances, CustomRule.class);
            NESTED_DTO_RULE = getAnnotationInstance(annotations, instances, NestedDtoRule.class);

            INSTANCES_MAP = ImmutableMap.copyOf(instances);
        } catch (Exception e) {
            throw new DtoGeneratorException("Unexpected error while initialisation.", e);
        }
    }

    private static <T extends Annotation> T getAnnotationInstance(Field field,
                                                                  Map<Class<? extends Annotation>, Annotation> instances,
                                                                  Class<T> annotationClass) {
        T instance = Objects.requireNonNull(field.getDeclaredAnnotation(annotationClass),
                "Annotation instance not set for an annotation class: '" + annotationClass + "'");
        instances.put(annotationClass, instance);
        return instance;
    }

}
