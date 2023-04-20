package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;

/**
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
public @interface Entry {

    DateTimeRule[] dateTimeRule() default {};

    BooleanRule[] booleanRule() default {};

    StringRule[] stringRule() default {};

    EnumRule[] enumRule() default {};

    IntegralRule[] numberRule() default {};

    DecimalRule[] decimalRule() default {};

    CustomRule[] customRule() default {};

}
