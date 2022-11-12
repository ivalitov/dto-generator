package dtogenerator.examples._demo.second_style;

import dtogenerator.examples.DtoVer1;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorDtoDependent;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.ICustomRuleRemark;
import laoruga.dtogenerator.api.util.RandomUtils;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.time.LocalDateTime;
import java.util.List;

import static dtogenerator.examples._demo.second_style.CustomRuleRemark.CLOSED;
import static dtogenerator.examples._demo.second_style.CustomRuleRemark.OPEN;


@NoArgsConstructor
public class ClosedDateGenerator implements ICustomGeneratorArgs<LocalDateTime>, ICustomGeneratorDtoDependent<LocalDateTime, DtoVer1>, ICustomGeneratorRemarkable<LocalDateTime> {

    DtoVer1 dtoVer1;
    String[] args;
    ICustomRuleRemark[] ruleRemarks;

    @Override
    public LocalDateTime generate() {

        if (ruleRemarks == null) {
            ruleRemarks = new ICustomRuleRemark[1];
            ruleRemarks[0] = RandomUtils.getRandom().nextInt(2) == 1 ? OPEN : CLOSED;
        }

        if (ruleRemarks.length > 1) {
            throw new IllegalStateException();
        }

        ICustomRuleRemark ruleRemark = ruleRemarks[0];
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
            throw new DtoGeneratorException("Unexpected rule remark " + ruleRemark);
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
    public ClosedDateGenerator setArgs(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public void setRuleRemarks(List<CustomRuleRemarkWrapper> ruleRemarks) {
        System.out.println(ruleRemarks);
    }
}
