package laoruga.dtogenerator.api.markup.generators;

import org.apache.commons.lang3.NotImplementedException;

import java.lang.annotation.Annotation;

public interface IGeneratorBuilder {

    IGenerator<?> build();

//    default IGenerator<?> build(Annotation rules) {
//        throw new NotImplementedException();
//    }

}
