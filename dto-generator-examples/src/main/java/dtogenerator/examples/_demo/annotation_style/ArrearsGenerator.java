package dtogenerator.examples._demo.annotation_style;

import dtogenerator.examples.Arrears;
import dtogenerator.api.markup.IRulesDependentCustomGenerator;
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
