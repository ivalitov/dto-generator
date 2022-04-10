package laoruga._demo.second_style;

import laoruga.dto.DtoVer1;
import laoruga.markup.ICustomGenerator;
import laoruga.markup.IExtendedRuleRemark;
import laoruga.markup.IObjectDependentCustomGenerator;
import laoruga.markup.IRemarkableCustomGenerator;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.time.LocalDateTime;
import java.util.Random;

import static laoruga._demo.second_style.CustomRuleRemark.CLOSED;
import static laoruga._demo.second_style.CustomRuleRemark.OPEN;

@NoArgsConstructor
public class ClosedDateGenerator implements ICustomGenerator<LocalDateTime>, IObjectDependentCustomGenerator<LocalDateTime, DtoVer1>, IRemarkableCustomGenerator<LocalDateTime> {

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
    public void setDependentObject(DtoVer1 dtoVer1) {
        this.dtoVer1 = dtoVer1;
    }

    @Override
    public boolean isObjectReady() {
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
