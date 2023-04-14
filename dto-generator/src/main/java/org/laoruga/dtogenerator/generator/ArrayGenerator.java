package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.ListGenerator;
import org.laoruga.dtogenerator.api.remarks.RuleRemark;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.generator.config.dto.ArrayConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.lang.reflect.Array;
import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class ArrayGenerator implements ListGenerator {

    private int minSize;
    private int maxSize;
    private Class<?> elementType;
    private Generator<?> elementGenerator;
    private RuleRemark ruleRemark;

    public ArrayGenerator(ArrayConfig config) {
        minSize = config.getMinSize();
        maxSize = config.getMaxSize();
        elementType = Objects.requireNonNull(config.getElementType(), "Array element type must be set");
        elementGenerator = Objects.requireNonNull(config.getElementGenerator(), "Array element generator must be set");
        ruleRemark = Objects.requireNonNull(config.getRuleRemark(), "Unexpected error, rule remark haven't set.");
    }

    @Override
    public Object generate() {
        int size;
        switch ((BoundaryConfig) ruleRemark) {

            case MIN_VALUE:
                size = minSize;
                break;

            case MAX_VALUE:
                size = maxSize;
                break;

            case RANDOM_VALUE:
            case NOT_DEFINED:
                size = RandomUtils.nextInt(minSize, maxSize);
                break;

            case NULL_VALUE:
                return null;

            default:
                throw new IllegalStateException("Unexpected value: " + ruleRemark);
        }

        Object newArray = Array.newInstance(elementType, size);

        int generatedElementsNumber = 0;
        while (generatedElementsNumber != size) {
            Array.set(newArray, generatedElementsNumber, elementGenerator.generate());
            generatedElementsNumber++;
        }

        return newArray;
    }

    @Override
    public Generator<?> getElementGenerator() {
        return elementGenerator;
    }
}
