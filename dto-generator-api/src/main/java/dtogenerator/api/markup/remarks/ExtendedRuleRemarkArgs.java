package dtogenerator.api.markup.remarks;


import dtogenerator.api.markup.generators.IGenerator;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
@Value
public class ExtendedRuleRemarkArgs implements IExtendedRuleRemark{

    IExtendedRuleRemark wrappedRule;
    Class<? extends IGenerator<?>> generatorClass;
    String[] args;

    public String[] getArgs() {
        return args;
    }

}