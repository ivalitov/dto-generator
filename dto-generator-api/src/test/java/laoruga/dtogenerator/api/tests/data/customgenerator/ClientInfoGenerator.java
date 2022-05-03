package laoruga.dtogenerator.api.tests.data.customgenerator;

import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapper;
import laoruga.dtogenerator.api.tests.data.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.text.RandomStringGenerator;

import java.time.LocalDate;
import java.util.Random;

import static laoruga.dtogenerator.api.tests.data.DocType.DRIVER_LICENCE;
import static laoruga.dtogenerator.api.tests.data.DocType.PASSPORT;

public class ClientInfoGenerator implements
        ICustomGeneratorArgs<ClientInfoDto>,
        ICustomGeneratorRemarkable<ClientInfoDto> {

    @Override
    public void setArgs(String[] args) {
    }

    @Override
    public void setRuleRemarks(ExtendedRuleRemarkWrapper... iRuleRemarks) {
    }

    @Override
    public ClientInfoDto generate() {
        Random random = new Random();
        RandomDataGenerator randomGen = new RandomDataGenerator();

        ClientType type = ClientType.values()[randomGen.nextInt(0, ClientType.values().length - 1)];
        DocType docType = random.nextBoolean() ? PASSPORT : DRIVER_LICENCE;

        ClientInfoDto clientInfo;

        switch (type) {
            case ORG:
                clientInfo = new OrgInfoDto(new RandomStringGenerator.Builder().build().generate(10));
                break;
            case PERSON:
            case LEGAL_PERSON:
                clientInfo = new PersonInfoDto(
                        new RandomStringGenerator.Builder().build().generate(10),
                        new RandomStringGenerator.Builder().build().generate(10),
                        new RandomStringGenerator.Builder().build().generate(10),
                        new Document(
                                docType,
                                String.valueOf(randomGen.nextInt(100000, 999999)),
                                String.valueOf(randomGen.nextInt(1000, 9999)),
                                LocalDate.of(
                                        randomGen.nextInt(1999, 2022),
                                        randomGen.nextInt(1, 12),
                                        randomGen.nextInt(1, 31)
                                ),
                                LocalDate.of(
                                        randomGen.nextInt(1999, 2022),
                                        randomGen.nextInt(1, 12),
                                        randomGen.nextInt(1, 31)
                                )
                        )
                );
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        clientInfo.setClientType(type);
        return clientInfo;
    }
}
