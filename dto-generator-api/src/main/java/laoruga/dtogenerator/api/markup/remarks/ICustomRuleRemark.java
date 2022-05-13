package laoruga.dtogenerator.api.markup.remarks;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

/**
 * The interface is designed to create remarks for custom generation rules.
 */
public interface ICustomRuleRemark {

    /**
     * Call this method for passing 'remark' to {@link laoruga.dtogenerator.api.DtoGeneratorBuilder}
     *
     * @param args params that need to be passed with this 'remark', it may be empty
     * @return wrapper of 'remark', containing params and generator's class
     */
    default CustomRuleRemarkWrapper wrap(String... args) {
        if (args.length != requiredArgsNumber()) {
            throw new DtoGeneratorException("Remark '" + this + "' expected '" + requiredArgsNumber() +
                    "' args. Passed '" + args.length + " args'");
        }
        return new CustomRuleRemarkWrapper(this, getGeneratorClass(), args);
    }

    /**
     * @return class of custom generator for which is intended this 'remark'
     */
    Class<? extends IGenerator<?>> getGeneratorClass();

    /**
     * A method for checking that the correct number of args
     * have been passed with {@link ICustomRuleRemark#wrap(java.lang.String...)}
     *
     * @return required number of args
     */
    int requiredArgsNumber();
}
