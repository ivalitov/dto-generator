package org.laoruga.dtogenerator.examples.dto;

import lombok.Getter;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.examples.generators.custom.CustomGeneratorDtoDependent;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
@Getter
public class TwoFields {

    @CustomRule(generatorClass = CustomGeneratorDtoDependent.class)
    String firstField;

    @StringRule(maxLength = 10)
    String secondField;
}
