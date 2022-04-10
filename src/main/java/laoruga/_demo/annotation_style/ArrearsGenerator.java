package laoruga._demo.annotation_style;

import laoruga._demo.annotation_style.ArrearsBusinessRule;
import laoruga.dto.Arrears;
import laoruga.markup.IRulesDependentCustomGenerator;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ArrearsGenerator implements IRulesDependentCustomGenerator<Arrears, ArrearsBusinessRule> {

    int arrearsCount;

    @Override
    public void prepareGenerator(ArrearsBusinessRule rules) {
        arrearsCount = rules.arrearsCount();
    }

    @Override
    public Arrears generate() {
        Arrears arrears = new Arrears();
        for (int i = 0; i < arrearsCount; i++) {
            arrears.addArrear(i);
        }
        return arrears;
    }
}
