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
    @IntegerRule
    @LongRule
    @DoubleRule
    @EnumRule
    @LocalDateTimeRule
    @ListRule
    @SetRule
    @CustomRule(generatorClass = Object.class)
    @NestedDtoRule
    private static Object annotations;

    public static final BooleanRule booleanRule;
    public static final StringRule stringRule;
    public static final IntegerRule integerRule;
    public static final LongRule longRule;
    public static final EnumRule enumRule;
    public static final DoubleRule doubleRule;
    public static final LocalDateTimeRule localDateTimeRule;
    public static final SetRule setRule;
    public static final ListRule listRule;
    public static final CustomRule customRule;
    public static final NestedDtoRule nestedDtoRule;

    static {
        try {
            Field annotations = RulesInstance.class.getDeclaredField("annotations");

            booleanRule = getAnnotationInstance(annotations, BooleanRule.class);
            stringRule = getAnnotationInstance(annotations, StringRule.class);
            integerRule = getAnnotationInstance(annotations, IntegerRule.class);
            longRule = getAnnotationInstance(annotations, LongRule.class);
            enumRule = getAnnotationInstance(annotations, EnumRule.class);
            doubleRule = getAnnotationInstance(annotations, DoubleRule.class);
            localDateTimeRule = getAnnotationInstance(annotations, LocalDateTimeRule.class);
            setRule = getAnnotationInstance(annotations, SetRule.class);
            listRule = getAnnotationInstance(annotations, ListRule.class);
            customRule = getAnnotationInstance(annotations, CustomRule.class);
            nestedDtoRule = getAnnotationInstance(annotations, NestedDtoRule.class);

        } catch (NoSuchFieldException e) {
            throw new DtoGeneratorException(e);
        }
    }

    private static <T extends Annotation> T getAnnotationInstance(Field field, Class<T> annotationClass){
       return Objects.requireNonNull(field.getDeclaredAnnotation(annotationClass),
               "Annotation instance was not set for class: '" + annotationClass + "'");
    }

}
