package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.ArrayGeneratorBuilder;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.lang.reflect.Array;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class ArrayGenerator implements IGenerator<Object> {

    private final int minSize;
    private final int maxSize;
    private final Class<?> elementType;
    private final IGenerator<?> elementGenerator;
    private final IRuleRemark ruleRemark;

    public static ArrayGeneratorBuilder builder() {
        return new ArrayGeneratorBuilder();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object generate() {
        int size;
        switch ((RuleRemark) ruleRemark) {
            case MIN_VALUE:
                size = minSize;
                break;
            case MAX_VALUE:
                size = maxSize;
                break;
            case RANDOM_VALUE:
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

    public IGenerator<?> getElementGenerator() {
        return elementGenerator;
    }

}
