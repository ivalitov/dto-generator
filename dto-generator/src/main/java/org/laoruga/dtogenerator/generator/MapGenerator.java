package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.MapConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
@AllArgsConstructor
@Getter
public class MapGenerator implements Generator<Map<Object, Object>> {

    private Integer minSize;
    private Integer maxSize;
    private Supplier<Map<Object, Object>> mapInstanceSupplier;
    private Generator<Object> keyGenerator;
    private Generator<Object> valueGenerator;
    private RuleRemark ruleRemark;

    public MapGenerator(MapConfig config) {
        minSize = config.getMinSize();
        maxSize = config.getMaxSize();
        mapInstanceSupplier = Objects.requireNonNull(config.getMapInstanceSupplier(), "Map instance supplier must be set");
        keyGenerator = Objects.requireNonNull(config.getKeyGenerator(), "Key generator must be set.");
        valueGenerator = Objects.requireNonNull(config.getValueGenerator(), "Value generator must be set.");
        ruleRemark = Objects.requireNonNull(config.getRuleRemark(), "Unexpected error, rule remark haven't set.");
    }

    @Override
    public Map<Object, Object> generate() {
        Map<Object, Object> mapInstance = mapInstanceSupplier.get();

        int maxAttempts = DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().getMaxCollectionGenerationCycles();
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

        int attempts = 0;
        while (mapInstance.size() < size) {
            Object key = keyGenerator.generate();
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

}
