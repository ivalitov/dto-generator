package org.laoruga.dtogenerator.api.remarks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;

/**
 * @author Il'dar Valitov
 * Created on 08.02.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomGeneratorStub implements ICustomGenerator<Object> {

    @Override
    public Object generate() {
        throw new IllegalStateException("This object is only a stub.");
    }
}
