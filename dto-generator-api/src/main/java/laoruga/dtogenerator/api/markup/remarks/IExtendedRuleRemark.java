package laoruga.dtogenerator.api.markup.remarks;

import laoruga.dtogenerator.api.markup.generators.IGenerator;

public interface IExtendedRuleRemark extends IRuleRemark {

    Class<? extends IGenerator<?>> getGeneratorClass();

    default ExtendedRuleRemarkArgs wrapArgs(String arg, String... args){
        String[] joinedArgs;
        if (args != null && args.length != 0) {
            joinedArgs = new String[args.length + 1];
            System.arraycopy(args, 0, joinedArgs, 1, args.length);
        } else {
            joinedArgs = new String[1];
        }
        joinedArgs[0] = arg;
        return new ExtendedRuleRemarkArgs(this, getGeneratorClass(), joinedArgs);
    }

}
