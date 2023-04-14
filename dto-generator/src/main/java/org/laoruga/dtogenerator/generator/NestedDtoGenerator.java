package org.laoruga.dtogenerator.generator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGeneratorBuildersTree;
import org.laoruga.dtogenerator.DtoInstanceSupplier;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.NestedConfig;

import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 23.04.2022
 */
@AllArgsConstructor
@Slf4j
@Getter(AccessLevel.PUBLIC)
public class NestedDtoGenerator implements Generator<Object> {

    private final DtoGeneratorBuildersTree.Node dtoGeneratorBuilderTreeNode;

    public NestedDtoGenerator(NestedConfig config) {
        dtoGeneratorBuilderTreeNode = config.getDtoGeneratorBuilderTreeNode();
        try {
            BoundaryConfig boundaryValue = (BoundaryConfig) config.getRuleRemark();
            if (boundaryValue != BoundaryConfig.NOT_DEFINED) {
                dtoGeneratorBuilderTreeNode
                        .getDtoGeneratorBuilder()
                        .setBoundaryConfig(boundaryValue);
            }
        } catch (DtoGeneratorException e) {
            if (e.getMessage().contains("Attempt to overwrite remark")) {
                log.debug("Rule remark wasn't overridden for NestedDtoGenerator, because it defined in root DtoGeneratorBuilder.");
            } else {
                throw e;
            }
        }
    }

    @Override
    public Object generate() {
        Supplier<?> dtoInstanceSupplier = dtoGeneratorBuilderTreeNode
                .getFieldGeneratorsProvider()
                .getDtoInstanceSupplier();
        if (dtoInstanceSupplier instanceof DtoInstanceSupplier) {
            ((DtoInstanceSupplier) dtoInstanceSupplier).updateInstance();
        }
        return dtoInstanceSupplier.get();
    }

}
