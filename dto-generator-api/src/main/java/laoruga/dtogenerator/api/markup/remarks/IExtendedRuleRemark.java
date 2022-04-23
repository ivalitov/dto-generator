package laoruga.dtogenerator.api.markup.remarks;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGenerator;

public interface IExtendedRuleRemark extends IRuleRemark {

    Class<? extends IGenerator<?>> getGeneratorClass();

    default ExtendedRuleRemarkWrapper wrap(String... args) {
        if (args.length != requiredArgsNumber()) {
            throw new DtoGeneratorException("Remark '" + this + "' expected '" + requiredArgsNumber() +
                    "' args. Passed '" + args.length + " args'");
        }
        return new ExtendedRuleRemarkWrapper(this, getGeneratorClass(), args);
    }

    int requiredArgsNumber();
}
