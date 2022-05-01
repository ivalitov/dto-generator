package laoruga.dtogenerator.api;

import io.qameta.allure.Feature;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.ExtendedRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.rules.IntegerRules;
import laoruga.dtogenerator.api.markup.rules.ListRules;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Basic Type Generators Tests")
public class CustomDtoGenerationTests {

    @Getter
    @NoArgsConstructor
    static class Dto {
//        @IntegerRules
//        private Integer intDefaultRules;
//        @CustomGenerator()
//        private ClientInfo clientData;

//        @ListRules(list = ArrayList.class)
//        @CustomGenerator(generatorClass = ClientInfoGenerator.class)
//        private List<ClientInfo> clients;

//        private Example example;

        @ListRules(listClass = ArrayList.class)
        @IntegerRules
        private List<Integer> years;

//        @ListRules
//        private ArrayList<Integer> years_2;
    }

    static class Example implements ExtendsOne, Two<String>{

    }

    interface One {

    }

    interface ExtendsOne extends One{

    }

    interface Two<T> {

    }




    @Data
    static class ClientInfo {
        ClientType clientType;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class OrgInfo extends ClientInfo {
        String orgName;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class PersonInfo extends ClientInfo {
        String firstName;
        String secondName;
        String middleName;
        Document document;
    }

    @Data
    static class Document {
        DocType type;
        String series;
        String number;
        LocalDate issueDate;
        LocalDate validityDate;
    }

    enum ClientType {
        ORG,
        PERSON
    }

    enum DocType {
        PASSPORT,
        DRIVER_LICENCE
    }


    static class ClientInfoGenerator implements
            ICustomGeneratorArgs<ClientInfo>,
            ICustomGeneratorRemarkable<ClientInfo> {

        @Override
        public void setArgs(String[] args) {

        }

        @Override
        public void setRuleRemarks(ExtendedRuleRemarkWrapper... iRuleRemarks) {

        }

        @Override
        public ClientInfo generate() {
            return null;
        }
    }


    @Test
    @Feature("LIST_RULES")
    @DisplayName("Nested Dto Generation")
    public void simpleIntegerGeneration() {
        Dto dto = DtoGenerator.builder().build().generateDto(Dto.class);
        assertNotNull(dto);
//        assertThat(dto.getIntDefaultRules(), both(
//                greaterThanOrEqualTo(IntegerRules.DEFAULT_MIN)).and(lessThanOrEqualTo(IntegerRules.DEFAULT_MAX)));
//        simpleIntegerGenerationAssertions(dto.getDtoNested());

    }


}
