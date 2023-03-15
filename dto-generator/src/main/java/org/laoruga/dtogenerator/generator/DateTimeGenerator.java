package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.DateTimeGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.DateTimeConfigDto;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.time.temporal.Temporal;
import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class DateTimeGenerator implements IGenerator<Temporal> {

    private final List<DateTimeConfigDto.ChronoConfig> chronoUnitConfigList;
    private final IRuleRemark ruleRemark;
    private final Class<? extends Temporal> generatedType;

    @Override
    public Temporal generate() {
        Temporal now = ReflectionUtils.callStaticMethod("now", generatedType, Temporal.class);

        if (chronoUnitConfigList != null) {
            for (DateTimeConfigDto.ChronoConfig chronoUnitConfig : chronoUnitConfigList) {
                now = chronoUnitConfig.adjust(now, ruleRemark);
            }
        }

        return now;
    }

    public static DateTimeGeneratorBuilder builder() {
        return new DateTimeGeneratorBuilder();
    }

}
