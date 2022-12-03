package org.laoruga.dtogenerator.generators;

import org.laoruga.dtogenerator.api.rules.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 30.11.2022
 */
public class RulesInstance {

    @StringRule
    @IntegerRule
    @LongRule
    @DoubleRule
    @EnumRule
    @LocalDateTimeRule
    @ListRule
    @SetRule
    private static Object annotations;

    public static final StringRule stringRule;
    public static final IntegerRule integerRule;
    public static final LongRule longRule;
    public static final EnumRule enumRule;
    public static DoubleRule doubleRule;
    public static LocalDateTimeRule localDateTimeRule;
    public static SetRule setRule;
    public static ListRule listRule;

    static {
        try {
            Field annotations = RulesInstance.class.getDeclaredField("annotations");

            stringRule = getAnnotationInstance(annotations, StringRule.class);
            integerRule = getAnnotationInstance(annotations, IntegerRule.class);
            longRule = getAnnotationInstance(annotations, LongRule.class);
            enumRule = getAnnotationInstance(annotations, EnumRule.class);
            doubleRule = getAnnotationInstance(annotations, DoubleRule.class);
            localDateTimeRule = getAnnotationInstance(annotations, LocalDateTimeRule.class);
            setRule = getAnnotationInstance(annotations, SetRule.class);
            listRule = getAnnotationInstance(annotations, ListRule.class);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Annotation> T getAnnotationInstance(Field field, Class<T> annotationClass){
       return Objects.requireNonNull(field.getDeclaredAnnotation(annotationClass),
               "Annotation instance was not set for class: '" + annotationClass + "'");
    }

}
