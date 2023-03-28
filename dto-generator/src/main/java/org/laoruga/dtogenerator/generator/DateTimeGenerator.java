package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.configs.datetime.ChronoConfig;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.time.temporal.Temporal;
import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class DateTimeGenerator implements IGenerator<Temporal> {

    private final List<ChronoConfig> chronoUnitConfigList;
    private final IRuleRemark ruleRemark;
    private final Class<? extends Temporal> generatedType;

    public DateTimeGenerator(DateTimeConfigDto config) {
        chronoUnitConfigList = config.getChronoUnitConfigList();
        ruleRemark = config.getRuleRemark();
        generatedType = config.getGeneratedType();
    }

    @Override
    public Temporal generate() {
        Temporal now = ReflectionUtils.callStaticMethod("now", generatedType, Temporal.class);

        if (chronoUnitConfigList != null) {
            for (ChronoConfig chronoUnitConfig : chronoUnitConfigList) {
                now = chronoUnitConfig.adjust(now, ruleRemark);
            }
        }

        return now;
    }
}
