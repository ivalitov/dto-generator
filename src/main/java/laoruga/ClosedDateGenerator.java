package laoruga;

import laoruga.dto.DtoVer1;
import laoruga.markup.ICustomGenerator;
import laoruga.markup.IObjectDependentCustomGenerator;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
public class ClosedDateGenerator implements
        ICustomGenerator<LocalDateTime>,
        IObjectDependentCustomGenerator<LocalDateTime, DtoVer1> {
    DtoVer1 dtoVer1;
    String[] args;

    @Override
    public LocalDateTime generate() {
        return dtoVer1.getOpenDate().plusYears(1);
    }

    @Override
    public void setDependentObject(DtoVer1 dtoVer1) {
        this.dtoVer1 = dtoVer1;
    }

    @Override
    public boolean isObjectReady() {
        return false;
//        return dtoVer1.getOpenDate() != null;
    }

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }
}
