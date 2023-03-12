package org.laoruga.dtogenerator.generator.configs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.NotImplementedException;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@NoArgsConstructor
public class DecimalCommonConfigDto implements ConfigDto {

    @Getter
    @Setter
    @Accessors(chain = true)
    private IRuleRemark ruleRemark;

    private final Map<Class<?>, DecimalConfigDto> map = new HashMap<>();

    public DecimalCommonConfigDto setMaxDoubleValue(double maxDoubleValue) {
        map.putIfAbsent(Double.class, new DecimalConfigDto());
        map.get(Double.class).setMaxValue(maxDoubleValue);
        return this;
    }

    public DecimalCommonConfigDto setMinDoubleValue(double minDoubleValue) {
        map.putIfAbsent(Double.class, new DecimalConfigDto());
        map.get(Double.class).setMinValue(minDoubleValue);
        return this;
    }

    public DecimalCommonConfigDto setMaxFloatValue(float maxFloatValue) {
        map.putIfAbsent(Float.class, new DecimalConfigDto());
        map.get(Float.class).setMaxValue(maxFloatValue);
        return this;
    }

    public DecimalCommonConfigDto setMinFloatValue(float minFloatValue) {
        map.putIfAbsent(Float.class, new DecimalConfigDto());
        map.get(Float.class).setMinValue(minFloatValue);
        return this;
    }

    public DecimalCommonConfigDto setMaxBigDecimalValue(BigDecimal maxBigDecimalValue) {
        map.putIfAbsent(BigDecimal.class, new DecimalConfigDto());
        map.get(BigDecimal.class).setMaxValue(maxBigDecimalValue);
        return this;
    }

    public DecimalCommonConfigDto setMinBigDecimalValue(BigDecimal minBigDecimalValue) {
        map.putIfAbsent(BigDecimal.class, new DecimalConfigDto());
        map.get(BigDecimal.class).setMinValue(minBigDecimalValue);
        return this;
    }

    public DecimalCommonConfigDto setMaxBigDecimalValue(String maxBigDecimalValue) {
        return setMaxBigDecimalValue(new BigDecimal(maxBigDecimalValue));
    }

    public DecimalCommonConfigDto setMinBigDecimalValue(String minBigDecimalValue) {
        return setMinBigDecimalValue(new BigDecimal(minBigDecimalValue));
    }

    DecimalConfigDto getConfigOrNull(Class<? extends Number> generateType) {
        return map.get(generateType);
    }

    public void merge(ConfigDto configDto) {
        throw new NotImplementedException("Not supposed to be in use.");
    }

}
