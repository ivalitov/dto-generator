package laoruga.dtogenerator.api.markup.remarks;


import laoruga.dtogenerator.api.markup.generators.IGenerator;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Value
public class ExtendedRuleRemarkWrapperWithArgs implements IExtendedRuleRemark {

    IExtendedRuleRemark wrappedRuleRemark;
    Class<? extends IGenerator<?>> generatorClass;
    String[] args;

    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public int requiredArgsNumber() {
        return wrappedRuleRemark.requiredArgsNumber();
    }

}
