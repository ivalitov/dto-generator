package org.laoruga.dtogenerator.api.remarks;

import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGenerator;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

/**
 * The interface is designed to create remarks for custom generation rules.
 *
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface ICustomRuleRemark {

    /**
     * Call this method for passing 'remark' to {@link DtoGeneratorBuilder}
     *
     * @param args params that need to be passed with this 'remark', it may be empty
     * @return wrapper of 'remark', containing params and generator's class
     */
    default CustomRuleRemarkWithArgs setArgs(String... args) {
        if (args.length != requiredArgsNumber()) {
            throw new DtoGeneratorException("Remark '" + this + "' expected '" + requiredArgsNumber() +
                    "' args. Passed '" + args.length + " args'");
        }
        return new CustomRuleRemarkWithArgs(this, getGeneratorClass(), args);
    }

    /**
     * A method for checking that the correct number of args
     * have been passed with {@link ICustomRuleRemark#setArgs(java.lang.String...)}
     *
     * @return required number of args
     */
    int requiredArgsNumber();

    /**
     * @return class of custom generator for which is intended this 'remark'
     */
    Class<? extends ICustomGenerator<?>> getGeneratorClass();
}
