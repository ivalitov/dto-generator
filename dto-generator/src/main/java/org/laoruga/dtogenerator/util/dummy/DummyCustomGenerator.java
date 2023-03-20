package org.laoruga.dtogenerator.util.dummy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;

/**
 * @author Il'dar Valitov
 * Created on 08.02.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DummyCustomGenerator implements ICustomGenerator<Object> {

    @Override
    public Object generate() {
        throw new IllegalStateException("This class is not supposed to be instantiated!");
    }
}
