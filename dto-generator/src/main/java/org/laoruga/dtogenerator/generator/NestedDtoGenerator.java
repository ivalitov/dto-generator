package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.NestedConfig;

/**
 * @author Il'dar Valitov
 * Created on 23.04.2022
 */
@AllArgsConstructor
@Slf4j
public class NestedDtoGenerator implements Generator<Object> {

    private final DtoGenerator<?> dtoGenerator;

    public NestedDtoGenerator(NestedConfig config) {
        DtoGeneratorBuilder<?> dtoGeneratorBuilder = config.getDtoGeneratorBuilder();
        try {
            RuleRemark ruleRemark = (RuleRemark) config.getRuleRemark();
            if (ruleRemark != RuleRemark.NOT_DEFINED) {
                dtoGeneratorBuilder.setRuleRemark(ruleRemark);
            }
        } catch (DtoGeneratorException e) {
            if (e.getMessage().contains("Attempt to overwrite remark")) {
                log.debug("Rule remark wasn't overridden for NestedDtoGenerator, because it defined in root DtoGeneratorBuilder.");
            } else {
                throw e;
            }
        }
        this.dtoGenerator = dtoGeneratorBuilder.build();
    }

    @Override
    public Object generate() {
        return dtoGenerator.generateDto();
    }

}
