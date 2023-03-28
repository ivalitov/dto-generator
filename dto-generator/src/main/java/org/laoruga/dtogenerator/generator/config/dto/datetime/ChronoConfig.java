package org.laoruga.dtogenerator.generator.config.dto.datetime;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;

import java.time.temporal.Temporal;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
public interface ChronoConfig {
    Temporal adjust(Temporal temporal, IRuleRemark ruleRemark);
}
