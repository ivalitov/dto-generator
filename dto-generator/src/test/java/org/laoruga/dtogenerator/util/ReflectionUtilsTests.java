package org.laoruga.dtogenerator.util;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 22.11.2022
 */
@DisplayName("Reflection utils tests")
@Epic("UNIT_TESTS")
@Feature("REFLECTION_UTILS")
class ReflectionUtilsTests {

    @TestFactory
    @DisplayName("Create collection instance")
    Stream<DynamicTest> createCollectionFieldInstance() {
        return Stream.of(
                DynamicTest.dynamicTest("Create list instance",
                        () -> assertEquals(ArrayList.class,
                                ReflectionUtils.createInstance(ArrayList.class).getClass())),

                DynamicTest.dynamicTest("Create set instance",
                        () -> assertEquals(HashSet.class,
                                ReflectionUtils.createInstance(HashSet.class).getClass())),

                DynamicTest.dynamicTest("Error when interface passed",
                        () -> assertThrows(DtoGeneratorException.class,
                                () -> ReflectionUtils.createInstance(Set.class))),

                DynamicTest.dynamicTest("Error when abstract passed",
                        () -> assertThrows(DtoGeneratorException.class,
                                () -> ReflectionUtils.createInstance(AbstractList.class)))
        );
    }

    @DisplayName("Assert type compatibility")
    void assertTypeCompatibility() {
        assertThrows(DtoGeneratorException.class,
                () -> ReflectionUtils.assertTypeCompatibility(Set.class, ArrayList.class));
    }

    static class Father {

        Son son;

       static String introduce() {
            return Father.class.getSimpleName();
        }

    }

    static class Son {

        Grandson grandson;

    }

    static class Grandson {

        String wisdom;

    }

    @DisplayName("Get Field Type From POJO")
    @Test
    void getFieldTypeFromPOJO() {

        final String PATH = "son.grandson.wisdom";

        assertAll(

                () -> assertThat("Field From Second Nested Class",
                        ReflectionUtils.getFieldType(PATH.split("\\."), 0, Father.class),
                        equalTo(String.class)
                ),

                () -> assertThat("Field From Current Class",
                        ReflectionUtils.getFieldType(new String[]{"wisdom"}, 0, Grandson.class),
                        equalTo(String.class)
                ),

                () -> assertThat("Field From Second Nested Class from another IDX",
                        ReflectionUtils.getFieldType(("FOO." + PATH).split("\\."), 1, Father.class),
                        equalTo(String.class)
                ),

                () -> assertThat("Field From First Nested Class from another IDX",
                        ReflectionUtils.getFieldType((PATH).split("\\."), 1, Son.class),
                        equalTo(String.class)
                ),

                () -> assertThat("Field From Current Class FromAnother IFX",
                        ReflectionUtils.getFieldType(new String[]{"FOO", "wisdom"}, 1, Grandson.class),
                        equalTo(String.class)
                )

        );

    }

    static class Mark extends Father {

        TreeHouse treeHouse;

        TreeHouseOfHorror treeHouseHorror;

        Integer courage;

    }

    static class SpecificMark extends Mark {

        boolean answer;

    }

    static class House {

        Double bricks;

    }

    static class TreeHouse extends House {

        Integer branches;

    }

    static class TreeHouseOfHorror extends TreeHouse {

        Float danger;

    }

    @DisplayName("Get Field Type With Straight Inheritance")
    @Test
    void getFieldTypeWithStraightInheritance() {

        final String PATH = "son.grandson.wisdom";

        assertAll(

                () -> assertThat("Inner Field From Parent of Current Class",
                        ReflectionUtils.getFieldType(PATH.split("\\."), 0, Mark.class),
                        equalTo(String.class)
                ),

                () -> assertThat("Inner Field From Second Parent of Current Class",
                        ReflectionUtils.getFieldType(PATH.split("\\."), 0, SpecificMark.class),
                        equalTo(String.class)
                ),

                () -> assertThat("Inherited Field Of Nested Field",
                        ReflectionUtils.getFieldType("treeHouse.branches".split("\\."), 0, Mark.class),
                        equalTo(Integer.class)
                ),

                () -> assertThat("Parent Class of Nested Field",
                        ReflectionUtils.getFieldType("treeHouse.bricks".split("\\."), 0, Mark.class),
                        equalTo(Double.class)
                ),

                () -> assertThat("Second Parent Class of Nested Field of Parent Class",
                        ReflectionUtils.getFieldType("treeHouseHorror.bricks".split("\\."), 0, SpecificMark.class),
                        equalTo(Double.class)
                )
        );

    }

    @DisplayName("Call Static Method")
    @Test
    void callStaticMethod() {

        assertThat(
                ReflectionUtils.callStaticMethod("introduce", Father.class, String.class),
                equalTo(Father.class.getSimpleName())
        );

    }

}