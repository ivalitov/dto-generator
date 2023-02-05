package org.laoruga.dtogenerator.functional.data.customgenerator;

import org.laoruga.dtogenerator.api.generators.AbstractCustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWithArgs;
import org.laoruga.dtogenerator.functional.data.dto.dtoclient.*;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.functional.data.customgenerator.ClientRemark.CLIENT_TYPE;
import static org.laoruga.dtogenerator.functional.data.customgenerator.ClientRemark.DOCUMENT;

/**
 * @author Il'dar Valitov
 * Created on 03.05.2022
 */

public class ClientInfoGenerator extends AbstractCustomGeneratorRemarkable<ClientInfoDto> implements
        ICustomGeneratorArgs<ClientInfoDto>,
        ICustomGeneratorDtoDependent<ClientInfoDto, ClientDto> {

    Supplier<ClientDto> generatedDto;
    private String[] generatorArgs;

    @Override
    public void setArgs(String[] args) {
        this.generatorArgs = args;
    }

    @Override
    public ClientInfoDto generate() {
        ClientType clientType;
        DocType docType;

        String prefix = generatorArgs.length == 0 ? "" : RandomUtils.getRandomItem(generatorArgs);

        Optional<CustomRuleRemarkWithArgs> maybeClientTypeRemark = getWrappedRemark(CLIENT_TYPE);
        clientType = maybeClientTypeRemark
                .map(customRuleRemarkWrapper -> ClientType.valueOf(
                        String.valueOf(customRuleRemarkWrapper.getArgs()[0]).toUpperCase()))
                .orElseGet(() -> RandomUtils.getRandomItem(ClientType.values()));

        Optional<CustomRuleRemarkWithArgs> maybeDocTypeRemark = getWrappedRemark(DOCUMENT);
        docType = maybeDocTypeRemark
                .map(customRuleRemarkWrapper -> DocType.valueOf(
                        String.valueOf(customRuleRemarkWrapper.getArgs()[0]).toUpperCase()))
                .orElseGet(() -> DocType.values()[RandomUtils.nextInt(0, DocType.values().length - 1)]);

        ClientInfoDto clientInfo;

        switch (clientType) {
            case ORG:
                clientInfo = new OrgInfoDto(prefix + RandomUtils.nextString(10));
                break;
            case PERSON:
            case LEGAL_PERSON:
                clientInfo = new PersonInfoDto(
                        prefix + RandomUtils.nextString(10),
                        prefix + RandomUtils.nextString(10),
                        prefix + RandomUtils.nextString(10),
                        new Document(
                                docType,
                                String.valueOf(RandomUtils.nextInt(100000, 999999)),
                                String.valueOf(RandomUtils.nextInt(1000, 9999)),
                                LocalDate.of(
                                        RandomUtils.nextInt(1999, 2022),
                                        RandomUtils.nextInt(1, 12),
                                        RandomUtils.nextInt(1, 28)
                                ),
                                LocalDate.of(
                                        RandomUtils.nextInt(1999, 2022),
                                        RandomUtils.nextInt(1, 12),
                                        RandomUtils.nextInt(1, 28)
                                )
                        )
                );
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + clientType);
        }
        clientInfo.setClientType(clientType);
        clientInfo.setId(generatedDto.get().getStringRequiredForClient());
        return clientInfo;
    }

    @Override
    public void setDtoSupplier(Supplier<ClientDto> generatedDto) {
        this.generatedDto = generatedDto;
    }

    @Override
    public boolean isDtoReady() {
        return generatedDto.get().getStringRequiredForClient() != null;
    }
}
