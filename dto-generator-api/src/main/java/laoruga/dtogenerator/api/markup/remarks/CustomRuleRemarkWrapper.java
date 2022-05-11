package laoruga.dtogenerator.api.markup.remarks;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import lombok.NonNull;
import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Value
public class CustomRuleRemarkWrapper {

    ICustomRuleRemark wrappedRuleRemark;
    Class<? extends IGenerator<?>> generatorClass;
    @NonNull String[] args;

    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    public List<String> getArgsList() {
        if (getArgs() == null || getArgs().length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(getArgs());
    }
}
