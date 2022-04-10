package dtogenerator.examples;

import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Data
public class Arrears {

    List<Arrear> arrearsList = new ArrayList<>();

    public void addArrear(int year) {
        arrearsList.add(new Arrear(year));
    }

    @Value
    static class Arrear {
        int year;
    }

}
