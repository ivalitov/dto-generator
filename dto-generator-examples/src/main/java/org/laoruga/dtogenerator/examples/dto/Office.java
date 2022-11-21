package org.laoruga.dtogenerator.examples.dto;

import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.ListRule;
import org.laoruga.dtogenerator.examples.generators.custom.CustomGeneratorRemarkable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@Data
@NoArgsConstructor
public class Office {

    @ListRule(minSize = 2, maxSize = 2)
    @CustomRule(generatorClass = CustomGeneratorRemarkable.class)
    List<Person> people;
}
