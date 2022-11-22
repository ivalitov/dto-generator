package org.laoruga.dtogenerator.util;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                                ReflectionUtils.createCollectionFieldInstance(ArrayList.class).getClass())),

                DynamicTest.dynamicTest("Create set instance",
                        () -> assertEquals(HashSet.class,
                                ReflectionUtils.createCollectionFieldInstance(HashSet.class).getClass())),

                DynamicTest.dynamicTest("Error when interface passed",
                        () -> assertThrows(DtoGeneratorException.class,
                                () -> ReflectionUtils.createCollectionFieldInstance(Set.class))),

                DynamicTest.dynamicTest("Error when abstract passed",
                        () -> assertThrows(DtoGeneratorException.class,
                                () -> ReflectionUtils.createCollectionFieldInstance(AbstractList.class)))
        );
    }

    @DisplayName("Assert type compatibility")
    void assertTypeCompatibility() {
        assertThrows(DtoGeneratorException.class,
                () -> ReflectionUtils.assertTypeCompatibility(Set.class, ArrayList.class));
    }


}
