package laoruga.dtogenerator.examples.dtos;

import laoruga.dtogenerator.api.markup.rules.CustomRule;
import laoruga.dtogenerator.api.markup.rules.StringRule;
import laoruga.dtogenerator.examples.generators.CustomGeneratorDtoDependent;
import lombok.Getter;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
@Getter
public class TwoFields {

    @CustomRule(generatorClass = CustomGeneratorDtoDependent.class)
    String firstField;

    @StringRule(maxSymbols = 10)
    String secondField;
}
