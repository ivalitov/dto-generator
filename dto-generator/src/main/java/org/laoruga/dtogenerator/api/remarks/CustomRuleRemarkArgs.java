package org.laoruga.dtogenerator.api.remarks;

import org.laoruga.dtogenerator.DtoGeneratorBuilder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

/**
 * @author Il'dar Valitov
 * Created on 05.02.2023
 */

public interface CustomRuleRemarkArgs extends CustomRuleRemark {
    /**
     * Call this method for passing 'remark' to {@link DtoGeneratorBuilder}
     *
     * @param args params that need to be passed with this 'remark', it may be empty
     * @return wrapper of 'remark', containing params and generator's class
     */
    default CustomRuleRemarkWithArgs setArgs(String... args) {
        if (args.length < minimumArgsNumber()) {
            throw new DtoGeneratorException("Remark '" + this + "' expected at least'" + minimumArgsNumber() +
                    "' args. Passed '" + args.length + " args'");
        }
        return new CustomRuleRemarkWithArgs(this, args);
    }

    default String[] getArgs() {
        return new String[0];
    }

    /**
     * A method for checking that the correct number of args
     * have been passed with {@link CustomRuleRemarkArgs#setArgs(java.lang.String...)}
     *
     * @return required number of args
     */
    int minimumArgsNumber();

    default CustomRuleRemark getRemarkInstance() {
        return this;
    }
}
