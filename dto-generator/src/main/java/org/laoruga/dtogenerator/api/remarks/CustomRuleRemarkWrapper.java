package org.laoruga.dtogenerator.api.remarks;

import lombok.NonNull;
import lombok.Value;
import org.laoruga.dtogenerator.api.generators.ICustomGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Value
public class CustomRuleRemarkWrapper {

    ICustomRuleRemark wrappedRuleRemark;
    Class<? extends ICustomGenerator<?>> generatorClass;
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