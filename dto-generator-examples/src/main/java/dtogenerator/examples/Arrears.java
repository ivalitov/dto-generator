package dtogenerator.examples;

import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Data
public class Arrears {

    @IntegerRules(minValue = 1, maxValue = 5)
    private int id;

    List<Arrear> arrearsList = new ArrayList<>();

    public void addArrear(int year) {
        arrearsList.add(new Arrear(year));
    }

    @Value
    static class Arrear {
        int year;
    }

}
