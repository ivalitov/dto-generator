package org.laoruga.dtogenerator.constants;

/**
 * @author Il'dar Valitov
 * Created on 11.03.2023
 */
public class Bounds {

    /*
     * Integral
     */

    public static final String BIG_INTEGER_MAX_VALUE = "" + Long.MAX_VALUE;

    public static final String BIG_INTEGER_MIN_VALUE = "-" + BIG_INTEGER_MAX_VALUE;

    /*
     * Decimal
     */

    public static final double DOUBLE_MAX_VALUE = (double) Integer.MAX_VALUE * 100;
    public static final double DOUBLE_MIN_VALUE = -DOUBLE_MAX_VALUE;

    public static final float FLOAT_MAX_VALUE = 50_000F;
    public static final float FLOAT_MIN_VALUE = -FLOAT_MAX_VALUE;

    public static final String BIG_DECIMAL_MAX_VALUE = BIG_INTEGER_MAX_VALUE;

    public static final String BIG_DECIMAL_MIN_VALUE = "-" + BIG_DECIMAL_MAX_VALUE;

}
