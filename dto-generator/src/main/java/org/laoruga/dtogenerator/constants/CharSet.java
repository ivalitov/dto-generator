package org.laoruga.dtogenerator.constants;

import lombok.Getter;

/**
 * @author Il'dar Valitov
 * Created on 04.04.2022
 */

@Getter
public class CharSet {

    public static final String NUM = "1234567890";
    public static final String ENG = "qwertzuiopasdfghjklyxcvbn";
    public static final String ENG_CAP = "QWERTZUIOPASDFGHJKLYXCVBNM";
    public static final String RUS = "абввгдеёжзиклмнопрстуфхцчшщэъьюя";
    public static final String RUS_CAP = "АБВВГДЕЁЖЗИКЛМНОПРСТУФХЦЧШЩЭЪЬЮЯ";
    public static final String SPECIAL_SYMBOLS_XML_SAFE = "~\"`'.,!?@#№$%^-+=*(){}[]/|\\_:; ";
    public static final String DEFAULT_CHARSET = NUM + ENG + ENG_CAP + SPECIAL_SYMBOLS_XML_SAFE;

}