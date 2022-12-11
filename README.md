[![Maven Central](https://img.shields.io/maven-central/v/org.laoruga/dto-generator.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.laoruga%22%20AND%20a:%22dto-generator%22)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ivalitov_dto-generator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ivalitov_dto-generator)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ivalitov_dto-generator&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ivalitov_dto-generator)
[![badge-jdk](https://img.shields.io/badge/jdk-8-green.svg)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![License badge](https://img.shields.io/badge/license-Apache2-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
***

# DTO Generator

## Description

DTO Generator makes it easy to create POJOs containing random data generated according to given rules.
This can be useful for test automation, when high variability of data is required.

## Documentation

Description of all features, examples, configurations is still in progress.

## Generation API

So-called "@Rules" annotations is used to mark the fields of DTO that need to be generated.
These annotations are also source of the configuration for generation.

- *@StringRule*
- *@IntegerRule*
- *@LongRule*
- *@DoubleRule*
- *@LocalDateTimeRule*
- *@EnumRule*
- *@ListRule*
- *@SetRule*

If a DTO class contains a nested DTO as one of the fields, the following annotation is used to tell the generator to
process the field [see more below](#nested_dto):

- *@NestedDtoRule*

If you want to create your own generator of specific type, next annotation is used [see more below](#custom_rule):

- *@CustomRule*

## Usage

1. [Annotation Based Configuration](#annotation_based_config)
2. [Grouping of Rules](#rules_grouping)
3. [Known Type's Generation](#known_types_generation)
4. [Configuration management](#config_management)
5. [Rule's Remarks](#rules_remarks)
6. [User's Builders](#users_builder)
7. [Nested DTO](#nested_dto)
8. [Custom Rules](#custom_rule)
9. [Requirements for POJO classes](#pojo_requirements)
10. [More Examples](#more_examples)

<a name="annotation_based_config"></a>

### 1. Annotation Based Configuration

For example, if we're using next rules:

```java
import org.laoruga.dtogenerator.api.rules.*;

import java.time.LocalDateTime;

public class Dto {

    @StringRule(words = {"Kate", "John", "Garcia"})
    private String name;

    @IntegerRule(minValue = 18, maxValue = 45)
    private Integer age;

    @EnumRule(possibleEnumNames = {"DRIVER_LICENCE", "PASSPORT"})
    private DocType docType;

    @LocalDateTimeRule(rightShiftDays = 0)
    private LocalDateTime lastVisit;

}
```

To generate an object with random dada inside, just create a **DtoGeneratorBuilder** instance and call **generateDto()**
one of two methods:

```java
import org.laoruga.dtogenerator.DtoGenerator;

public class Foo {
    void bar() {
        // 1. You can pass class of DTO
        DtoGenerator<Dto> dtoGenerator = DtoGenerator.builder(Dto.class).build();
        // and every call of generateDto method will provide new instance with random data
        Dto dtoInstance = dtoGenerator.generateDto();

        // 2. Or you can pass instance of DTO to be filled with random data
        // In this case, every call of generateDto method will generate new data in the same DTO
        Dto dto = DtoGenerator.builder(new Dto()).build().generateDto();
    }
}
```

As the result we'll have an object containing random data, based on parameters of generation rules:

```json
{
  "name": "John",
  "age": 33,
  "docType": "DRIVER_LICENCE",
  "lastVisit": "2022-08-13T16:55:44.365"
}
```


<a name="rules_grouping"></a>

### 2. Grouping of Rules

You may put multiple **@Rule** annotations on the one field and mark them with different groups.
Then you need to select which group you prefer to use for DTO generation.

For example, next config:

```java
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.*;

public class Example7 {

    public static class Dto7 {

        @StringRule(words = {"Alice", "Maria"}, group = "GIRL")
        @StringRule(words = {"Peter", "Clint"}, group = "BOY")
        private String name;

        @IntegerRule(minValue = 18, maxValue = 30, group = "YOUNG")
        @IntegerRule(minValue = 31, maxValue = 60, group = "MATURE")
        private Integer age;

    }

    public static void main(String[] args) {
        Dto7 dto = DtoGenerator.builder(Dto7.class)
                .includeGroups("MATURE", "BOY")
                .build()
                .generateDto();
    }
}
```

will produce, for instance:

```json
{
  "name": "Clint",
  "age": 45
}
```

<a name="known_types_generation"></a>

### 3. Known Type's Generation

Configuration parameter **generateAllKnownTypes** allows to generate values of fields of known types, without the need
to put *@Rules* annotation no them.
Known type are those types for which *@Rules* annotations exists.

There are several ways to configuring of generation, one of them is use of static configuration:

```java
DtoGeneratorStaticConfig.getInstance().setGenerateAllKnownTypes(true);
```

For example, if we use next DTO class:

```java
public class Dto2 {

    String name;
    Integer age;
    DocType docType;
    LocalDateTime lastVisit;
    List<String> comments;
}
```

As the result we'll have an object containing random data, based on default or overridden parameters of generation
rules:

```json
{
  "productCode": "GjH8Ss7xF",
  "productNumber": 87168751,
  "productType": "DIGITAL",
  "lastCheck": "2020-08-05T18:16:10.433",
  "comments": [
    "gxcOU2WjK",
    "qfhBLC",
    "2IlUobdR"
  ]
}
```

<a name="config_management"></a>

### 4. Configuration management

There are 4 configuration levels, each next level of config overrides previous ones:

1. Annotation config -
    1. parameters provided from **@Rules** annotation of the DTO field
    2. for known types only, if DTO field not annotated with **@Rules**, is used default parameters provided from **
       @Rules**
2. Static config - one configuration for all **DtoGeneratorBuilders**
3. User's config - each **DtoGeneratorBuilder** instance's own configuration
4. User's type generator config - if generator field type is set by
   user [see below about builders overriding](#users_builder)

For example:

```java
import org.laoruga.dtogenerator.*;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;

public class Example3 {

    public static class Dto3 {

        // annotation config
        @StringRule(minLength = 5)
        String exampleString;

    }

    public static void main(String[] args) {

        DtoGeneratorBuilder<Dto3> builder = DtoGenerator.builder(Dto3.class);

        // user config
        builder.getUserConfig().getGenBuildersConfig().getStringConfig().setMaxLength(5);

        // static config
        DtoGeneratorStaticConfig.getInstance().getGenBuildersConfig().getStringConfig().setChars("x");

        Dto3 dtoInstance = builder.build().generateDto();
    }
}
```

The result sting will be next:

```json
{
  "exampleString": "xxxxx"
}
```

<a name="rules_remarks"></a>

### 5. Rule's Remarks

Rule's remarks - refinements for known type generators, to generate boundary values.
It is possible to assign remark either to the entire DTO or only to certain fields.

Rule's remarks:

- MIN_VALUE
- MAX_VALUE
- RANDOM_VALUE
- NULL_VALUE

For example:

```java
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.*;

import java.util.List;

import static org.laoruga.dtogenerator.constants.BasicRuleRemark.*;

public class Example4 {

    public static class Dto4 {

        @StringRule(minLength = 1, maxLength = 150)
        private String name;

        @IntegerRule(minValue = 18, maxValue = 45)
        private Integer age;

        @ListRule(minSize = 1, maxSize = 20)
        @StringRule(minLength = 2, maxLength = 15)
        private List<String> children;

    }

    public static void main(String[] args) {

        Dto4 dto = DtoGenerator.builder(Dto4.class)
                .setRuleRemark(MIN_VALUE)
                .setRuleRemark("age", MAX_VALUE)
                .build()
                .generateDto();
    }
}
```

The result dto will look like:

```json
{
  "name": "i",
  "age": 45,
  "children": [
    "lL"
  ]
}
```

<a name="users_builder"></a>

### 6. User's Builders of Known Types

You may override generators of known types:

1. Override generator linked with **@Rules** annotation by default (will apply to all fields within DTO)
2. Override generator of certain field

For overriding you can use:

1. default type's generator builders (in this case the only thing that may be changed is generator's params)
2. your own generator builder or even lambda function

```java
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.typegenerators.builders.GeneratorBuildersFactory;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.List;

public class Example5 {

    public static class Dto5 {

        @StringRule(minLength = 1, maxLength = 150)
        String name;

        @StringRule
        String secondName;

        @ListRule(minSize = 1, maxSize = 1)
        @StringRule
        List<String> children;
    }

    public static void main(String[] args) {

        Dto5 dto = DtoGenerator.builder(Dto5.class)
                // default type's generator builder is used to override generator for all fields annotated with @StringRule
                .setGeneratorBuilder(StringRule.class, GeneratorBuildersFactory.stringBuilder()
                        .minLength(5)
                        .maxLength(5))
                // lambda expression is used to override generator of the exact field
                .setGeneratorBuilder("secondName", () -> () -> RandomUtils.getRandomItem("Smith", "Claus"))
                .build()
                .generateDto();

        System.out.println(Main.toJson(dto));
    }
}
```

The result dto may look like:

```json
{
  "name": "QPvXB",
  "secondName": "Smith",
  "children": [
    "ZEeI7"
  ]
}
```

<a name="nested_dto"></a>

### 7. Nested DTO

If you want to override or remark fields within nested DTO, use path to DTO separated by dots.

For example:

```java
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;

public class Example6 {

    public static class Dto6 {

        @NestedDtoRule
        Dto7 nestedDto;
    }

    public static class Dto6 {

        @StringRule(regexp = "[+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2}")
        String phoneNumber;

        @StringRule(words = {"Home", "Work"})
        String phoneType;

        @StringRule
        String comment;
    }

    public static void main(String[] args) {
        Dto6 dto = DtoGenerator.builder(Dto6.class)
                .setRuleRemark("nestedDto.comment", NULL_VALUE)
                .setGeneratorBuilder("nestedDto.phoneType", () -> () -> "Other")
                .build()
                .generateDto();
    }
}
```

Will result something like:

```json
{
  "nestedDto": {
    "phoneNumber": "+89 (905) 545-60-48",
    "phoneType": "Other",
    "comment": null
  }
}
```

<a name="custom_rule"></a>

### 8. Custom Rules

```diff
- This is an experimental feature, it will be significantly changed in future releases
```

You may design your custom generator for any type you want. To do this, you need to implement one or more
`ICustomGenerator*` interfaces:

- `ICustomGenerator`             
- `ICustomGeneratorArgs`         
- `ICustomGeneratorDtoDependent` 
- `ICustomGeneratorRemarkable`   

Example of custom generator with args:

```java
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import java.util.Map;

public class Example8 {

    /**
     * Dto to generate
     */
    static class Dto8 {
        
        @CustomRule(generatorClass = DocumentGenerator.class, args = "PASSPORT")
        private Document document;
    }

    /**
     * Type that we want to generate via custom generator
     */
    static class Document {
        Map<String, String> attributes;
    }

    /**
     * Custom generator which requires of passing of arguments
     */
    static class DocumentGenerator implements ICustomGeneratorArgs<Document> {

        private String docType;

        @Override
        public void setArgs(String... args) {
            this.docType = args[0];
        }

        @Override
        public Document generate() {
            Document document = new Document();
            switch (docType) {
                case "PASSPORT":
                    document.attributes = generatePassportAttributes();
                case "DRIVER_LICENSE":
                    document.attributes = generateDriverLicenseAttributes();
            }
            return document;
        }

        public Map<String, String> generatePassportAttributes() {
            // generation logic
        }

        public Map<String, String> generateDriverLicenseAttributes() {
            // generation logic
        }
    }
}
```

More info and more examples of usage you may see in the project with examples: [Dto Generator Examples project](dto-generator-examples/README.md)

<a name="pojo_requirements"></a>

### 9. Requirements for DTO classes

Requirements:
1. DTO class must have declared no-args constructor with any visibility
2. DTO class shouldn't be a NOT-static nested class

Supported:
1. DTO classes may extend abstract or concrete classes via inheritance
2. DTO class may be static nested class

<a name="more_examples"></a>

### 10. More Examples
More examples you can find in the: [Dto Generator Examples project](dto-generator-examples/README.md)
