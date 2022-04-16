package laoruga.dtogenerator.api.constants;

import lombok.Getter;

@Getter
public enum CharSet {

    NUM(new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}),
    ENG(new char[]{'q', 'w', 'e', 'r', 't', 'z', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'y', 'x', 'c', 'v', 'b', 'n', 'm'}),
    ENG_CAP(new char[]{'Q', 'W', 'E', 'R', 'T', 'Z', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Y', 'X', 'C', 'V', 'B', 'N', 'M'}),
    RUS(new char[]{'а', 'б', 'в', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'э', 'ъ', 'ь', 'ю', 'я'}),
    RUS_CAP(new char[]{'А', 'Б', 'В', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Э', 'Ъ', 'Ь', 'Ю', 'Я'});

    private char[] chars;

    CharSet(char[] chars) {
        this.chars = chars;
    }
}