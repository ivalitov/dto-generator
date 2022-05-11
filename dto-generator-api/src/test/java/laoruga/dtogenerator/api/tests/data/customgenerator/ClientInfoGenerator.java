package laoruga.dtogenerator.api.tests.data.customgenerator;

import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorDtoDependent;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.tests.data.dtoclient.*;
import laoruga.dtogenerator.api.utils.RandomUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.text.RandomStringGenerator;

import java.time.LocalDate;
import java.util.List;

import static laoruga.dtogenerator.api.tests.data.customgenerator.ClientRemark.*;

public class ClientInfoGenerator implements
        ICustomGeneratorArgs<ClientInfoDto>,
        ICustomGeneratorRemarkable<ClientInfoDto>,
        ICustomGeneratorDtoDependent<ClientInfoDto, ClientDto> {

    ClientDto generatedDto;
    List<CustomRuleRemarkWrapper> remarks;
    private String[] generatorArgs;

    @Override
    public void setArgs(String[] args) {
        this.generatorArgs = args;
    }

    @Override
    public void setRuleRemarks(List<CustomRuleRemarkWrapper> iRuleRemarks) {
        remarks = iRuleRemarks;
    }

    @Override
    public ClientInfoDto generate() {
        RandomDataGenerator randomGen = new RandomDataGenerator();

        ClientType clientType;
        DocType docType;

        String prefix = RandomUtils.getRandomItemOrNull(generatorArgs);
        prefix = prefix == null ? "" : prefix;

        CustomRuleRemarkWrapper clientTypeRemark = ICustomGeneratorRemarkable.findWrappedRemarkOrReturnNull(CLIENT_TYPE, remarks);
        if (clientTypeRemark != null) {
            clientType = ClientType.valueOf(String.valueOf(clientTypeRemark.getArgs()[0]).toUpperCase());
        } else {
            clientType = ClientType.values()[randomGen.nextInt(0, ClientType.values().length - 1)];
        }

        CustomRuleRemarkWrapper docTypeRemark = ICustomGeneratorRemarkable.findWrappedRemarkOrReturnNull(DOCUMENT, remarks);
        if (docTypeRemark != null) {
            docType = DocType.valueOf(String.valueOf(docTypeRemark.getArgs()[0]).toUpperCase());
        } else {
            docType = DocType.values()[randomGen.nextInt(0, DocType.values().length - 1)];
        }

        ClientInfoDto clientInfo;

        switch (clientType) {
            case ORG:
                clientInfo = new OrgInfoDto(prefix + new RandomStringGenerator.Builder().build().generate(10));
                break;
            case PERSON:
            case LEGAL_PERSON:
                clientInfo = new PersonInfoDto(
                        prefix + new RandomStringGenerator.Builder().build().generate(10),
                        prefix + new RandomStringGenerator.Builder().build().generate(10),
                        prefix + new RandomStringGenerator.Builder().build().generate(10),
                        new Document(
                                docType,
                                String.valueOf(randomGen.nextInt(100000, 999999)),
                                String.valueOf(randomGen.nextInt(1000, 9999)),
                                LocalDate.of(
                                        randomGen.nextInt(1999, 2022),
                                        randomGen.nextInt(1, 12),
                                        randomGen.nextInt(1, 28)
                                ),
                                LocalDate.of(
                                        randomGen.nextInt(1999, 2022),
                                        randomGen.nextInt(1, 12),
                                        randomGen.nextInt(1, 28)
                                )
                        )
                );
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + clientType);
        }
        clientInfo.setClientType(clientType);
        clientInfo.setId(generatedDto.getStringRequiredForClient());
        return clientInfo;
    }

    @Override
    public void setDto(ClientDto generatedDto) {
        this.generatedDto = generatedDto;
    }

    @Override
    public boolean isDtoReady() {
        return generatedDto.getStringRequiredForClient() != null;
    }
}
