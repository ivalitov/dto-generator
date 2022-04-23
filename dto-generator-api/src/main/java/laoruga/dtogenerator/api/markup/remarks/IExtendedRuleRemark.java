package laoruga.dtogenerator.api.markup.remarks;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface IExtendedRuleRemark extends IRuleRemark {

    Class<? extends IGenerator<?>> getGeneratorClass();

    default String[] getArgs() {
        throw new NotImplementedException();
    }

    default List<String> getArgsList() {
        if (getArgs() == null || getArgs().length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(getArgs());
    }

    default ExtendedRuleRemarkWrapperWithArgs wrapArgs(String arg, String... args) {
        String[] joinedArgs;
        if (args != null && args.length != 0) {
            joinedArgs = new String[args.length + 1];
            System.arraycopy(args, 0, joinedArgs, 1, args.length);
        } else {
            joinedArgs = new String[1];
        }
        joinedArgs[0] = arg;
        return new ExtendedRuleRemarkWrapperWithArgs(this, getGeneratorClass(), joinedArgs);
    }

    int requiredArgsNumber();
}
