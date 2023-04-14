package org.laoruga.dtogenerator.generator.config.dto.datetime;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.RuleRemark;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoFieldShift;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;

import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 19.02.2023
 */
@Builder
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DateTimeConfig implements ConfigDto {

    private List<ChronoConfig> chronoUnitConfigList;
    private RuleRemark ruleRemark;
    private Class<? extends Temporal> generatedType;

    public DateTimeConfig(DateTimeRule rule) {
        this(rule, null);
    }

    public DateTimeConfig(DateTimeRule rule, Class<? extends Temporal> fieldType) {

        ChronoUnitShift[] chronoUnitShifts = rule.chronoUnitShift();
        if (chronoUnitShifts.length > 0) {
            chronoUnitConfigList = new LinkedList<>();
            Arrays.stream(chronoUnitShifts).forEach(it -> chronoUnitConfigList.add(
                    new ChronoUnitConfig(
                            it.shift(),
                            it.leftBound(),
                            it.rightBound(),
                            it.unit()
                    )
            ));
        }

        ChronoFieldShift[] chronoFieldShifts = rule.chronoFieldShift();
        if (chronoFieldShifts.length > 0) {
            chronoUnitConfigList = chronoUnitConfigList == null ? new LinkedList<>() : chronoUnitConfigList;
            Arrays.stream(chronoFieldShifts).forEach(it -> chronoUnitConfigList.add(
                    new ChronoFieldConfig(
                            it.shift(),
                            it.leftBound(),
                            it.rightBound(),
                            it.unit()
                    )
            ));
        }

        this.generatedType = fieldType;
        this.ruleRemark = rule.ruleRemark();
    }

    public DateTimeConfig addChronoConfig(ChronoUnitConfig config) {
        chronoUnitConfigList = chronoUnitConfigList == null ? new LinkedList<>() : chronoUnitConfigList;
        chronoUnitConfigList.add(config);
        return this;
    }

    public DateTimeConfig addChronoConfig(ChronoFieldConfig config) {
        chronoUnitConfigList = chronoUnitConfigList == null ? new LinkedList<>() : chronoUnitConfigList;
        chronoUnitConfigList.add(config);
        return this;
    }

    public void merge(ConfigDto from) {
        DateTimeConfig configDto = (DateTimeConfig) from;
        if (configDto.getChronoUnitConfigList() != null)
            this.chronoUnitConfigList = configDto.getChronoUnitConfigList();
        if (configDto.getChronoUnitConfigList() != null)
            this.chronoUnitConfigList = configDto.getChronoUnitConfigList();
        if (configDto.getChronoUnitConfigList() != null)
            this.chronoUnitConfigList = configDto.getChronoUnitConfigList();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }

    public static class DateTimeConfigBuilder {

        private List<ChronoConfig> chronoUnitConfigList;

        public DateTimeConfigBuilder addChronoConfig(ChronoUnitConfig config) {
            chronoUnitConfigList = chronoUnitConfigList == null ? new LinkedList<>() : chronoUnitConfigList;
            chronoUnitConfigList.add(config);
            return this;
        }

        public DateTimeConfigBuilder addChronoConfig(ChronoFieldConfig config) {
            chronoUnitConfigList = chronoUnitConfigList == null ? new LinkedList<>() : chronoUnitConfigList;
            chronoUnitConfigList.add(config);
            return this;
        }

    }

}
