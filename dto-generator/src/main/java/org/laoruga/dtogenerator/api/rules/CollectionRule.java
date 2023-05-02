package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.util.dummy.DummyCollectionClass;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Put on the field of type extending {@code Collection} interface (or {@code Collection} itself)
 * to generate random collection containing random data.
 *
 * @see Entry
 * @see Boundary
 * @see Group
 */
@Retention(RUNTIME)
@Target(FIELD)
@Rule(RuleType.COLLECTION)
@Repeatable(CollectionRules.class)
public @interface CollectionRule {

    Class<?> GENERATED_TYPE = Collection.class;

    /**
     * You may specify the generation {@link Rule} for collection's elements generation.
     * If not defined collection's elements will be generated if one of the following conditions met:
     * <ul>
     *     <li>collection's type corresponds to one of the rules listed in {@link Entry}</li>
     *     <li>there is user's generator exists for collection's element type</li>
     * </ul>
     * Otherwise, exception will be thrown.
     *
     * @return {@link Entry} containing rule for generating collection's elements
     * @see Entry
     */
    Entry element() default @Entry;

    /**
     * You may specify a concrete collection class to instantiation.
     * If class not specified and field's type is concrete class
     * - this class will be instantiated using no-args constructor.
     * Otherwise, if field's type is {@code interface} or {@code abstract class}, concrete collection class to
     * instantiate will be chosen depending on field's type:
     * <ul>
     *     <li>{@link java.util.ArrayList ArrayList}
     *     - if field's type is {@link java.util.List List}, extends or implements {@link java.util.List List}</li>
     *     <li>{@link java.util.HashSet HashSet}
     *     - if field's type is {@link java.util.Set Set}, extends or implements {@link java.util.Set Set}</li>
     *     <li>{@link java.util.PriorityQueue PriorityQueue}
     *     - if field's type is {@link java.util.Queue Queue}, extends or implements {@link java.util.Queue Queue}</li>
     *     <li>{@link java.util.ArrayList ArrayList}
     *     - if field's type is {@link java.util.Collection Collection}, extends or implements {@link java.util.Collection Collection}</li>
     * </ul>
     *
     * @return concrete class of collection to instantiate
     */
    Class<? extends Collection> collectionClass() default DummyCollectionClass.class;

    /**
     * @return maximum size of generating collection
     */
    int maxSize() default 10;

    /**
     * @return minimum size of generating collection
     */
    int minSize() default 1;

    /**
     * @return boundary parameter
     * @see Boundary
     */
    Boundary boundary() default Boundary.RANDOM_VALUE;

    /**
     * @return group of the generators
     * @see Group
     */
    String group() default Group.DEFAULT;

}
