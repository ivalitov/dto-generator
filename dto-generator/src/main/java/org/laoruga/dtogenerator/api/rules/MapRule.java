package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.util.dummy.DummyMapClass;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Put on the {@code Map} field or field of type extending {@code Map}
 * to generate random map containing random data.
 *
 * @author Il'dar Valitov
 * Created on 17.03.2023
 * @see Entry
 * @see Boundary
 * @see Group
 */
@Retention(RUNTIME)
@Target(FIELD)
@Rule(RuleType.MAP)
@Repeatable(MapRules.class)
public @interface MapRule {

    Class<?> GENERATED_TYPE = Map.class;

    /**
     * You may specify the generation {@link Rule} for map keys generation.
     * If not defined, map keys will be generated if one of the following conditions met:
     * <ul>
     *     <li>map key type corresponds to one of the rules listed in {@link Entry}</li>
     *     <li>there is user's generator exists for key's type</li>
     * </ul>
     * Otherwise, exception will be thrown.
     *
     * @return {@link Entry} containing rule for generating of map keys
     * @see Entry
     */
    Entry key() default @Entry;

    /**
     * You may specify the generation {@link Rule} for map values generation.
     * If not defined, map values will be generated if one of the following conditions met:
     * <ul>
     *     <li>map value type corresponds to one of the rules listed in {@link Entry}</li>
     *     <li>there is user's generator exists for value's type</li>
     * </ul>
     * Otherwise, exception will be thrown.
     *
     * @return {@link Entry} containing rule for generating of map values
     * @see Entry
     */
    Entry value() default @Entry;

    /**
     * You may specify a concrete map class to instantiation.
     * If class not specified and field's type is concrete class
     * - this class will be instantiated using no-args constructor.
     * Otherwise, if field's type is {@code interface} or {@code abstract class}, concrete map class to
     * instantiate will be chosen depending on field's type:
     * <ul>
     *     <li>{@link java.util.TreeMap TreeMap}
     *     - if field's type is {@link java.util.SortedMap SortedMap}, extends or implements {@link java.util.SortedMap SortedMap}</li>
     *     <li>{@link java.util.Map HashMap}
     *     - if field's type is {@link java.util.Map Map}, extends or implements {@link java.util.Map Map}</li>
     * </ul>
     *
     * @return concrete class of map to instantiate
     */
    Class<? extends Map> mapClass() default DummyMapClass.class;

    /**
     * @return maximum size of generating map
     */
    int maxSize() default 10;

    /**
     * @return minimum size of generating map
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
