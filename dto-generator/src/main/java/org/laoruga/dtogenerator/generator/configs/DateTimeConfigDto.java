package org.laoruga.dtogenerator.generator.configs;

import lombok.*;
import lombok.experimental.Accessors;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoFieldShift;
import org.laoruga.dtogenerator.api.rules.datetime.ChronoUnitShift;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
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
public class DateTimeConfigDto implements ConfigDto {

    private List<ChronoConfig> chronoUnitConfigList;
    private IRuleRemark ruleRemark;
    private Class<? extends Temporal> generatedType;

    public DateTimeConfigDto(DateTimeRule rule, Class<? extends Temporal> fieldType) {

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

    public DateTimeConfigDto addChronoConfig(ChronoFieldConfig config) {
        chronoUnitConfigList = chronoUnitConfigList == null ? new LinkedList<>() : chronoUnitConfigList;
        chronoUnitConfigList.add(config);
        return this;
    }

    public DateTimeConfigDto addChronoConfig(ChronoUnitConfig config) {
        chronoUnitConfigList = chronoUnitConfigList == null ? new LinkedList<>() : chronoUnitConfigList;
        chronoUnitConfigList.add(config);
        return this;
    }

    public void merge(ConfigDto from) {
        DateTimeConfigDto configDto = (DateTimeConfigDto) from;
        if (configDto.getChronoUnitConfigList() != null)
            this.chronoUnitConfigList = configDto.getChronoUnitConfigList();
        if (configDto.getChronoUnitConfigList() != null)
            this.chronoUnitConfigList = configDto.getChronoUnitConfigList();
        if (configDto.getChronoUnitConfigList() != null)
            this.chronoUnitConfigList = configDto.getChronoUnitConfigList();
        if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
    }

    public interface ChronoConfig {
        Temporal adjust(Temporal temporal, IRuleRemark ruleRemark);
    }

    public static class ChronoUnitConfig implements ChronoConfig {
        private final long shift;
        private final long leftBound;
        private final long rightBound;
        private final TemporalUnit unit;

        private ChronoUnitConfig(long shift, long leftBound, long rightBound, TemporalUnit unit) {
            this.shift = shift;
            this.leftBound = leftBound;
            this.rightBound = rightBound;
            this.unit = unit;
        }

        public ChronoUnitConfig(long shift, TemporalUnit unit) {
            this.shift = shift;
            this.unit = unit;
            this.leftBound = 0;
            this.rightBound = 0;
        }

        public ChronoUnitConfig(long leftBound, long rightBound, TemporalUnit unit) {
            this.unit = unit;
            this.leftBound = leftBound;
            this.rightBound = rightBound;
            this.shift = 0;
        }

        @Override
        public Temporal adjust(Temporal temporal, IRuleRemark ruleRemark) {
            if (shift != 0) {
                return temporal.plus(shift, unit);
            }
            long shiftValue;
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                shiftValue = leftBound;
            } else if (ruleRemark == RuleRemark.MAX_VALUE) {
                shiftValue = rightBound;
            } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                shiftValue = RandomUtils.nextLong(leftBound, rightBound);
            } else {
                throw new IllegalStateException("Unexpected value " + ruleRemark);
            }

            return temporal.plus(shiftValue, unit);
        }

    }

    public static class ChronoFieldConfig implements ChronoConfig {

        private final long shift;
        private final long leftBound;
        private final long rightBound;
        private final TemporalField field;

        private ChronoFieldConfig(long shift, long leftBound, long rightBound, TemporalField field) {
            this.shift = shift;
            this.leftBound = leftBound;
            this.rightBound = rightBound;
            this.field = field;
        }

        public ChronoFieldConfig(long shift, TemporalField field) {
            this.shift = shift;
            this.field = field;
            this.leftBound = 0;
            this.rightBound = 0;
        }

        public ChronoFieldConfig(long leftBound, long rightBound, TemporalField field) {
            this.field = field;
            this.leftBound = leftBound;
            this.rightBound = rightBound;
            this.shift = 0;
        }

        @Override
        public Temporal adjust(Temporal temporal, IRuleRemark ruleRemark) {
            if (shift != 0) {
                return temporal.with(field, shift);
            }
            long shiftValue;
            if (ruleRemark == RuleRemark.MIN_VALUE) {
                shiftValue = leftBound;
            } else if (ruleRemark == RuleRemark.MAX_VALUE) {
                shiftValue = rightBound;
            } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
                shiftValue = RandomUtils.nextLong(leftBound, rightBound);
            } else {
                throw new IllegalStateException("Unexpected value " + ruleRemark);
            }
            return temporal.with(field, shiftValue);
        }
    }

}
