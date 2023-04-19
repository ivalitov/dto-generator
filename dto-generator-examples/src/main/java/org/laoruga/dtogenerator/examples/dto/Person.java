package org.laoruga.dtogenerator.examples.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.laoruga.dtogenerator.api.rules.IntegralRule;
import org.laoruga.dtogenerator.api.rules.StringRule;

import java.util.List;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@NoArgsConstructor
@Getter
@Setter
public class Person {

    @StringRule(regexp = "[A-Z]{1}[a-z]{5} [A-Z]{1}[a-z]{5} [A-Z]{1}[a-z]{5}")
    private String fio;
    @IntegralRule(minInt = 1, maxInt = 101)
    private Integer age;
    @StringRule(words = {"red", "yellow", "black", "gray", "white"})
    private String hairColor;
    private List<String> pets;
    private Gender gender;
    private int growth;
    private int weight;

}
