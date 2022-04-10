package laoruga.markup;

public interface IExtendedRuleRemark extends IRuleRemark {

    Class<? extends IGenerator<?>> getGeneratorClass();

}
