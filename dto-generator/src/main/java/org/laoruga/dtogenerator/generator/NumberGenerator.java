package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.NumberGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.NumberConfigDto;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class NumberGenerator implements IGenerator<Number> {

    private final Number maxValue;
    private final Number minValue;
    private final boolean isAtomic;
    private final IRuleRemark ruleRemark;

    @Override
    public Number generate() {

        Number result;

        if (ruleRemark == RuleRemark.MIN_VALUE) {
            result = minValue;
        } else if (ruleRemark == RuleRemark.MAX_VALUE) {
            result = maxValue;
        } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            result = RandomUtils.nextNumber(minValue, maxValue);
        } else if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        } else {
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

    public static NumberGeneratorBuilder builder() {
        return new NumberGeneratorBuilder();
    }

    public static NumberGeneratorBuilder builder(NumberConfigDto configDto) {
        return new NumberGeneratorBuilder(configDto);
    }

}
