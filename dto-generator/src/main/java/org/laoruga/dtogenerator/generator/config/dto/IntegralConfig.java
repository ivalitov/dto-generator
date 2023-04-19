package org.laoruga.dtogenerator.generator.config.dto;

import com.google.common.primitives.Primitives;
import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.rules.IntegralRule;

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
public class IntegralConfig implements ConfigDto {

    private Number maxValue;
    private Number minValue;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Class<? extends Number> fieldType;

    private boolean isAtomic;
    private RuleRemark ruleRemark;

    public IntegralConfig(IntegralRule rules, Class<? extends Number> fieldType) {
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

        boolean commonConfig = configDto.getClass() == IntegralConfigCommonConfig.class;

        IntegralConfig configFrom = commonConfig
                ? ((IntegralConfigCommonConfig) configDto).getConfigOrNull(fieldType)
                : (IntegralConfig) configDto;

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