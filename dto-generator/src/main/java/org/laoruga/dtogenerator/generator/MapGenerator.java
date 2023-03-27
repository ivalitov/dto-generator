package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.MapGeneratorBuilder;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
@AllArgsConstructor
@Getter
public class MapGenerator<K, V> implements IGenerator<Map<K, V>> {

    private Integer minSize;
    private Integer maxSize;
    private Supplier<Map<K, V>> mapInstanceSupplier;
    private IGenerator<K> keyGenerator;
    private IGenerator<V> valueGenerator;
    private IRuleRemark ruleRemark;

    @Override
    public Map<K, V> generate() {
        Map<K,V> mapInstance = mapInstanceSupplier.get();

        int maxAttempts = DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().getMaxCollectionGenerationCycles();
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

        int attempts = 0;
        while (mapInstance.size() < size) {
            K key = keyGenerator.generate();
            if (mapInstance.containsKey(key)) {
                if (attempts >= maxAttempts) {
                    throw new DtoGeneratorException("Expected size: '" + size + "' of map: '"
                            + mapInstance.getClass() + "' can't be reached. After '" + attempts
                            + "' attempts map has size: '"
                            + mapInstance.size() + "'");
                }
                attempts++;
                continue;
            }
            mapInstance.put(key, valueGenerator.generate());
        }

        return mapInstance;
    }

    public static MapGeneratorBuilder builder() {
        return new MapGeneratorBuilder();
    }

}
