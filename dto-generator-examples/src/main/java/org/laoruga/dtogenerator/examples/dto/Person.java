package org.laoruga.dtogenerator.examples.dto;

import org.laoruga.dtogenerator.api.rules.IntegerRule;
import org.laoruga.dtogenerator.api.rules.StringRule;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@Data
@NoArgsConstructor
public class Person {

    @StringRule(regexp = "[A-Z]{1}[a-z]{5} [A-Z]{1}[a-z]{5} [A-Z]{1}[a-z]{5}")
    private String fio;
    @IntegerRule(minValue = 1, maxValue = 101)
    private Integer age;
    @StringRule(words = {"red", "yellow", "black", "gray", "white"})
    private String hairColor;
    private List<String> pets;
    private Gender gender;
    private int growth;
    private int weight;

}
