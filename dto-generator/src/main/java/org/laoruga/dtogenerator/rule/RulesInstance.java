package org.laoruga.dtogenerator.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
    @DoubleRule
    @EnumRule
    @LocalDateTimeRule
    @CollectionRule
    @CustomRule(generatorClass = Object.class)
    @NestedDtoRule
    private static Object annotations;

    @NumberRule(maxInt = 0, minInt = 0, maxLong = 0, minLong = 0, maxByte = 0, minByte = 0, maxShort = 0, minShort = 0)
    private static Object annotationsNumberZeros;

    /*
     * Default Values
     */
    public static final BooleanRule booleanRule;
    public static final StringRule stringRule;
    public static final NumberRule numberRule;
    public static final EnumRule enumRule;
    public static final DoubleRule doubleRule;
    public static final LocalDateTimeRule localDateTimeRule;
    public static final CollectionRule collectionRule;
    public static final CustomRule customRule;
    public static final NestedDtoRule nestedDtoRule;

    /*
     * Other
     */

    public static final NumberRule NUMBER_RULE_ZEROS;


    static {
        try {
            Field annotations = RulesInstance.class.getDeclaredField("annotations");

            booleanRule = getAnnotationInstance(annotations, BooleanRule.class);
            stringRule = getAnnotationInstance(annotations, StringRule.class);
            numberRule = getAnnotationInstance(annotations, NumberRule.class);
            enumRule = getAnnotationInstance(annotations, EnumRule.class);
            doubleRule = getAnnotationInstance(annotations, DoubleRule.class);
            localDateTimeRule = getAnnotationInstance(annotations, LocalDateTimeRule.class);
            collectionRule = getAnnotationInstance(annotations, CollectionRule.class);
            customRule = getAnnotationInstance(annotations, CustomRule.class);
            nestedDtoRule = getAnnotationInstance(annotations, NestedDtoRule.class);

            NUMBER_RULE_ZEROS = getAnnotationInstance(annotations, NumberRule.class);

        } catch (NoSuchFieldException e) {
            throw new DtoGeneratorException(e);
        }
    }

    private static <T extends Annotation> T getAnnotationInstance(Field field, Class<T> annotationClass) {
        return Objects.requireNonNull(field.getDeclaredAnnotation(annotationClass),
                "Annotation instance was not set for class: '" + annotationClass + "'");
    }

}
