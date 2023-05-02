package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;

/**
 * Entry is designed to define rule for generating elements of collections, arrays or maps.
 * <p>
 * Select only one appropriate {@code Entry} method, depending on element type,
 * and set only one annotation instance, otherwise exception will be thrown.
 * <p>
 * Entry is applicable for the following rules:
 * <ul>
 *     <li>{@link ArrayRule}</li>
 *     <li>{@link CollectionRule}</li>
 *     <li>{@link MapRule}</li>
 * </ul>
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
public @interface Entry {

    DateTimeRule[] dateTimeRule() default {};

    BooleanRule[] booleanRule() default {};

    StringRule[] stringRule() default {};

    EnumRule[] enumRule() default {};

    IntegralRule[] integralRule() default {};

    DecimalRule[] decimalRule() default {};

    CustomRule[] customRule() default {};

}
