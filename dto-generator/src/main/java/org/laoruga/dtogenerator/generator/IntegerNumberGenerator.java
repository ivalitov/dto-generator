package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.generator.config.dto.IntegralConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class IntegerNumberGenerator implements Generator<Number> {

    private final Number maxValue;
    private final Number minValue;
    private final boolean isAtomic;
    private final RuleRemark ruleRemark;

    public IntegerNumberGenerator(IntegralConfig configDto) {
        maxValue = configDto.getMaxValue();
        minValue = configDto.getMinValue();
        isAtomic = configDto.isAtomic();
        ruleRemark = configDto.getRuleRemark();
    }

    @Override
    public Number generate() {

        Number result;

        switch ((Boundary) ruleRemark) {

            case MIN_VALUE:
                result = minValue;
                break;

            case MAX_VALUE:
                result = maxValue;
                break;

            case NULL_VALUE:
                return null;

            case RANDOM_VALUE:
            case NOT_DEFINED:
                result = RandomUtils.nextNumber(minValue, maxValue);
                break;

            default:
                throw new IllegalStateException("Unexpected value " + ruleRemark);
        }

        if (isAtomic) {
            if (result.getClass() == Long.class) {
                return new AtomicLong((long) result);
            }
            if (result.getClass() == Integer.class) {
                return new AtomicInteger((int) result);
            }
            throw new IllegalStateException("Unexpected type: '" + result.getClass() + "'");
        }

        return result;
    }

}
