package laoruga.dtogenerator.api.markup.generators;

/**
 * @author Il'dar Valitov
 * Created on 18.04.2022
 */

public interface ICustomGeneratorArgs<GENERATED_TYPE> extends ICustomGenerator<GENERATED_TYPE> {

    ICustomGeneratorArgs setArgs(String... args);
}
