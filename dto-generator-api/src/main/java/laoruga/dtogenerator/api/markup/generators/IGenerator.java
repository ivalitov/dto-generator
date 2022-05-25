package laoruga.dtogenerator.api.markup.generators;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

public interface IGenerator<GENERATED_TYPE> {

    GENERATED_TYPE generate();
}
