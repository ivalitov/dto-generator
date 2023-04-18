package org.laoruga.dtogenerator.generator.config.dto;

import com.google.common.primitives.Primitives;
import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.rules.IntegerRule;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class IntegerConfig implements ConfigDto {

    private Number maxValue;
    private Number minValue;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Class<? extends Number> fieldType;

    private boolean isAtomic;
    private RuleRemark ruleRemark;

    /**
     * Builder Class.
     * Types of min and max values have to be the same as generated type, this config intended for.
     */
    public static class IntegerConfigBuilder {

        public IntegerConfigBuilder maxValue(Integer maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public IntegerConfigBuilder minValue(Integer minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        public IntegerConfigBuilder maxValue(Long maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public IntegerConfigBuilder minValue(Long minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        public IntegerConfigBuilder maxValue(Short maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public IntegerConfigBuilder minValue(Short minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        public IntegerConfigBuilder maxValue(Byte maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public IntegerConfigBuilder minValue(Byte minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        public IntegerConfigBuilder maxValue(BigInteger maxValue) {
            check(maxValue, minValue);
            this.maxValue = maxValue;
            return this;
        }

        public IntegerConfigBuilder minValue(BigInteger minValue) {
            check(minValue, maxValue);
            this.minValue = minValue;
            return this;
        }

        private void check(Number valueToSet, Number another) {
            if (another != null && another.getClass() != valueToSet.getClass()) {
                throw new IllegalArgumentException("Wrong bound type: '" + valueToSet + "'. " +
                        "Bound with type '" + another + "' expexted.");
            }
        }

    }

    public IntegerConfig(IntegerRule rules, Class<? extends Number> fieldType) {
        fieldType = Primitives.wrap(fieldType);

        this.isAtomic = fieldType == AtomicInteger.class || fieldType == AtomicLong.class;
        this.fieldType = fieldType;
        this.ruleRemark = rules.boundary();

        if (fieldType == Integer.class || fieldType == AtomicInteger.class) {
            minValue = rules.minInt();
            maxValue = rules.maxInt();
        } else if (fieldType == Long.class || fieldType == AtomicLong.class) {
            minValue = rules.minLong();
            maxValue = rules.maxLong();
        } else if (fieldType == Short.class) {
            minValue = rules.minShort();
            maxValue = rules.maxShort();
        } else if (fieldType == Byte.class) {
            minValue = rules.minByte();
            maxValue = rules.maxByte();
        } else if (fieldType == BigInteger.class) {
            minValue = new BigInteger(rules.minBigInt());
            maxValue = new BigInteger(rules.maxBigInt());
        } else {
            throw new IllegalStateException("Unexpected field type: '" + fieldType + "'");
        }
    }

    public Number getMaxValue() {
        return maxValue;
    }

    public Number getMinValue() {
        return minValue;
    }

    public void merge(ConfigDto configDto) {

        boolean commonConfig = configDto.getClass() == IntegerCommonConfig.class;

        IntegerConfig configFrom = commonConfig
                ? ((IntegerCommonConfig) configDto).getConfigOrNull(fieldType)
                : (IntegerConfig) configDto;

        if (commonConfig) {
            if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        }

        if (configFrom != null) {
            if (configFrom.getMaxValue() != null) this.maxValue = configFrom.getMaxValue();
            if (configFrom.getMinValue() != null) this.minValue = configFrom.getMinValue();
            if (configFrom.getRuleRemark() != null) this.ruleRemark = configFrom.getRuleRemark();
        }
    }

}
