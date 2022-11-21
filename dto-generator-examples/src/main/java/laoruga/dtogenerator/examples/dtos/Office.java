package laoruga.dtogenerator.examples.dtos;

import laoruga.dtogenerator.api.markup.rules.CustomRule;
import laoruga.dtogenerator.api.markup.rules.ListRule;
import laoruga.dtogenerator.examples.generators.CustomGeneratorRemarkable;
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
