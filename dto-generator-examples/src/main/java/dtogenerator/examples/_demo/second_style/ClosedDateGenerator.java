package dtogenerator.examples._demo.second_style;

import dtogenerator.examples.DtoVer1;
import dtogenerator.api.markup.generators.ICustomGenerator;
import dtogenerator.api.markup.remarks.IExtendedRuleRemark;
import dtogenerator.api.markup.generators.IDtoDependentCustomGenerator;
import dtogenerator.api.markup.generators.IRemarkableCustomGenerator;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.time.LocalDateTime;
import java.util.Random;

import static dtogenerator.examples._demo.second_style.CustomRuleRemark.CLOSED;
import static dtogenerator.examples._demo.second_style.CustomRuleRemark.OPEN;

@NoArgsConstructor
public class ClosedDateGenerator implements ICustomGenerator<LocalDateTime>, IDtoDependentCustomGenerator<LocalDateTime, DtoVer1>, IRemarkableCustomGenerator<LocalDateTime> {

    DtoVer1 dtoVer1;
    String[] args;
    IExtendedRuleRemark[] ruleRemarks;

    @Override
    public LocalDateTime generate() {

        if (ruleRemarks == null) {
            ruleRemarks = new IExtendedRuleRemark[1];
            ruleRemarks[0] = new Random().nextInt(2) == 1 ? OPEN : CLOSED;
        }

        if (ruleRemarks.length > 1) {
            throw new IllegalStateException();
        }

        IExtendedRuleRemark ruleRemark = ruleRemarks[0];
        if (ruleRemark == OPEN) {
            return null;
        } else if (ruleRemark == CLOSED) {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime closedDate = dtoVer1.getOpenDate().plusDays(
                    new RandomDataGenerator().nextInt(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
            if (closedDate.isAfter(yesterday)) {
                closedDate = yesterday;
            }
            return closedDate;
        } else {
            throw new RuntimeException("Unexpected rule remark " + ruleRemark);
        }
    }

    @Override
    public void setDto(DtoVer1 dtoVer1) {
        this.dtoVer1 = dtoVer1;
    }

    @Override
    public boolean isDtoReady() {
        return dtoVer1.getOpenDate() != null;
    }

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public void setRuleRemarks(IExtendedRuleRemark... ruleRemarks) {
        this.ruleRemarks = ruleRemarks;
    }
}