package org.laoruga.dtogenerator.examples.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.examples.generators.custom.CustomGeneratorRemarkable;

import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@NoArgsConstructor
@Getter
public class Office {

    @CollectionRule(minSize = 2, maxSize = 2)
    @CustomRule(generatorClass = CustomGeneratorRemarkable.class)
    List<Person> people;
}
