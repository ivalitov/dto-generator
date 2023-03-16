package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.RuleForCollection;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.util.DummyCollectionClass;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@RuleForCollection
@Repeatable(CollectionRules.class)
public @interface CollectionRule {

    Class<?> GENERATED_TYPE = Collection.class;

    Class<? extends Collection> collectionClass() default DummyCollectionClass.class;

    int maxSize() default 10;

    int minSize() default 1;

    RuleRemark ruleRemark() default RuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
