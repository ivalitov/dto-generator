package laoruga.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
