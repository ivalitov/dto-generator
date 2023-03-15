package org.laoruga.dtogenerator.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoFieldShift;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
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
    @CollectionRule
    @CustomRule(generatorClass = Object.class)
    @NestedDtoRule
    private static final Object ANNOTATIONS = null;

    @DateTimeRule(
            chronoUnitShift = @ChronoUnitShift(unit = ChronoUnit.DAYS),
            chronoFieldShift = @ChronoFieldShift(unit = ChronoField.DAY_OF_WEEK)
    )
    private static final Object ANNOTATIONS_2 = null;

    /*
     * Default Values
     */
    public static final BooleanRule BOOLEAN_RULE;
    public static final StringRule STRING_RULE;
    public static final NumberRule NUMBER_RULE;
    public static final EnumRule ENUM_RULE;
    public static final DecimalRule DECIMAL_RULE;
    public static final DateTimeRule DATE_TIME_RULE;
    public static final ChronoUnitShift CHRONO_UNIT_SHIFT;
    public static final ChronoFieldShift CHRONO_FIELD_SHIFT;
    public static final CollectionRule COLLECTION_RULE;
    public static final CustomRule CUSTOM_RULE;
    public static final NestedDtoRule NESTED_DTO_RULE;

    /*
     * Other
     */

    public static final NumberRule NUMBER_RULE_ZEROS;


    static {
        try {
            Field annotations = RulesInstance.class.getDeclaredField("ANNOTATIONS");

            BOOLEAN_RULE = getAnnotationInstance(annotations, BooleanRule.class);
            STRING_RULE = getAnnotationInstance(annotations, StringRule.class);
            NUMBER_RULE = getAnnotationInstance(annotations, NumberRule.class);
            NUMBER_RULE_ZEROS = getAnnotationInstance(annotations, NumberRule.class);
            ENUM_RULE = getAnnotationInstance(annotations, EnumRule.class);
            DECIMAL_RULE = getAnnotationInstance(annotations, DecimalRule.class);
            COLLECTION_RULE = getAnnotationInstance(annotations, CollectionRule.class);
            CUSTOM_RULE = getAnnotationInstance(annotations, CustomRule.class);
            NESTED_DTO_RULE = getAnnotationInstance(annotations, NestedDtoRule.class);


            annotations = RulesInstance.class.getDeclaredField("ANNOTATIONS_2");

            DATE_TIME_RULE = getAnnotationInstance(annotations, DateTimeRule.class);
            CHRONO_UNIT_SHIFT = DATE_TIME_RULE.chronoUnitShift()[0];
            CHRONO_FIELD_SHIFT = DATE_TIME_RULE.chronoFieldShift()[0];

        } catch (Exception e) {
            throw new DtoGeneratorException(e);
        }
    }

    private static <T extends Annotation> T getAnnotationInstance(Field field, Class<T> annotationClass) {
        return Objects.requireNonNull(field.getDeclaredAnnotation(annotationClass),
                "Annotation instance was not set for class: '" + annotationClass + "'");
    }

}
