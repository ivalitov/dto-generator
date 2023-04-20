package org.laoruga.dtogenerator.util;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.*;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.Entry;
import org.laoruga.dtogenerator.api.rules.IntegralRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

    @DisplayName("Get Field But Field Not Found")
    @Test
    @Tag("NEGATIVE_TEST")
    void getFieldButFieldNotFound() {

        DtoGeneratorException exception = assertThrows(DtoGeneratorException.class,
                () -> ReflectionUtils.getFieldType(new String[]{"treeHouse", "absent"}, 0, Mark.class)
        );

        assertThat(exception.getMessage(),
                containsString("Field 'absent' not found in the class: '" + TreeHouse.class.getName() + "'"));
    }

    @DisplayName("Call Static Method")
    @Test
    void callStaticMethod() {

        assertThat(
                ReflectionUtils.callStaticMethod("introduce", Father.class, String.class),
                equalTo(Father.class.getSimpleName())
        );

    }

    List<String> listOfString;
    String string;
    Map<String, String> stringStringMap;

    @Test
    @DisplayName("Extract Singe Generic Type")
    void extractSingeGenericType() {

        Field field = ReflectionUtils.getField(this.getClass(), "listOfString");

        assertThat(
                ReflectionUtils.getSingleGenericType(field),
                equalTo(String.class)
        );

    }

    @Test
    @DisplayName("Get Array Element Type")
    void getArrayElementType() {

        assertAll(
                () -> assertThat(
                        ReflectionUtils.getArrayElementType(Integer[].class),
                        equalTo(Integer.class)
                ),
                () -> assertThat(
                        ReflectionUtils.getArrayElementType(double[].class),
                        equalTo(Double.TYPE)
                )
        );

    }

    @Test
    @DisplayName("Extract Singe Generic Type")
    @Tag("NEGATIVE_TEST")
    void unableToExtractSingeGenericTypeWithoutGeneric() {

        Field field = ReflectionUtils.getField(this.getClass(), "string");

        DtoGeneratorException exception = assertThrows(
                DtoGeneratorException.class,
                () -> ReflectionUtils.getSingleGenericType(field)
        );

        assertThat(exception.getMessage(), containsString("Next type must have single generic type"));
    }

    @Test
    @DisplayName("Unable to Get Array Element Type")
    @Tag("NEGATIVE_TEST")
    void unableToGetArrayElementType() {

        DtoGeneratorException exception = assertThrows(
                DtoGeneratorException.class,
                () -> ReflectionUtils.getArrayElementType(String.class)
        );

        assertThat(exception.getMessage(), containsString("Cannot find array element type using next regex pattern"));
    }

    static class Foo {
        Foo(String arg) {
            throw new IllegalArgumentException(arg);
        }

        Foo(String firstArg, String secondArg) {
        }
    }

    @Test
    @DisplayName("Unable to Create Instance (without args)")
    @Tag("NEGATIVE_TEST")
    void unableToCreateInstanceWithoutArgs() {

        DtoGeneratorException exception = assertThrows(
                DtoGeneratorException.class,
                () -> ReflectionUtils.createInstance(Foo.class)
        );

        assertThat(ExceptionUtils.getStackTrace(exception), containsString("Class must have no-args constructor"));
    }

    @Test
    @DisplayName("Unable to Create Instance (wrong args number)")
    @Tag("NEGATIVE_TEST")
    void unableToCreateInstanceWrongArgsNumber() {

        DtoGeneratorException exception = assertThrows(
                DtoGeneratorException.class,
                () -> ReflectionUtils.createInstance(Foo.class, 1, 2, 3)
        );

        assertThat(ExceptionUtils.getStackTrace(exception), containsString("Class must have constructor with params"));
    }

    @Test
    @DisplayName("Unable to Create Instance (unexpected error)")
    @Tag("NEGATIVE_TEST")
    void unableToCreateInstanceWrongUnexpectedError() {

        final String CONSTANT = "%ALWAYS_WRONG%";

        DtoGeneratorException exception = assertThrows(
                DtoGeneratorException.class,
                () -> ReflectionUtils.createInstance(Foo.class, CONSTANT)
        );

        assertThat(ExceptionUtils.getStackTrace(exception),
                allOf(
                        containsString("Failed to instantiate class"),
                        containsString(CONSTANT)
                ));
    }

    @CollectionRule(element = @Entry(
            stringRule = @StringRule,
            numberRule = @IntegralRule
    ))
    List<String> stringList;

    @Test
    @DisplayName("Get Single Rule From Entry")
    @Tag("NEGATIVE_TEST")
    void getSingleRuleFromEntry() {

        Field field = ReflectionUtils.getField(this.getClass(), "stringList");
        Entry element = field.getDeclaredAnnotation(CollectionRule.class).element();

        DtoGeneratorException exception = assertThrows(
                DtoGeneratorException.class,
                () -> ReflectionUtils.getSingleRuleFromEntryOrDefaultForType(element, String.class)
        );

        assertThat(ExceptionUtils.getStackTrace(exception), containsString("More than one annotation found"));
    }

    @CollectionRule(element = @Entry())
    List<String> stringList_2;

    @Test
    @DisplayName("Get Single Rule From Entry (wrong type)")
    @Tag("NEGATIVE_TEST")
    void getSingleRuleFromEntryWrongType() {

        Field field = ReflectionUtils.getField(this.getClass(), "stringList_2");
        Entry element = field.getDeclaredAnnotation(CollectionRule.class).element();

        DtoGeneratorException exception = assertThrows(
                DtoGeneratorException.class,
                () -> ReflectionUtils.getSingleRuleFromEntryOrDefaultForType(element, Foo.class)
        );

        assertThat(ExceptionUtils.getStackTrace(exception), containsString("failed to select @Rules annotation by type"));
    }

}