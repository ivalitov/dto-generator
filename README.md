[![Maven Central](https://img.shields.io/maven-central/v/org.laoruga/dto-generator.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.laoruga%22%20AND%20a:%22dto-generator%22)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ivalitov_dto-generator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ivalitov_dto-generator)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ivalitov_dto-generator&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ivalitov_dto-generator)
[![badge-jdk](https://img.shields.io/badge/jdk-8-green.svg)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![License badge](https://img.shields.io/badge/license-Apache2-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
***

# DTO Generator

## Description

DTO Generator is a Java library that makes it easy to get Java objects filled with random data generated according to
given rules.

There are various ways to provide configuration:

- directly on the DTO fields via annotations ;
- by configuring DtoGenerator instance;
- by updating common default configuration;
- do not set anything relying on default configuration;
- use all the above approaches together.

This may be useful for a test automation when high variability of data is required.

## Documentation

Description of all features, examples, configurations in progress.

## Generation API

Various `@Rule` annotations are used to provide configuration on the field:

- `@StringRule`
- `@IntegralRule`
- `@DecimalRule`
- `@DateTimeRule`
- `@BooleanRule`
- `@EnumRule`
- `@CollectionRule`
- `@ArrayRule`
- `@MapRule`

If a DTO class contains within its fields another DTOs, which fields must be generated as well,
following annotation is used to tell to the generator to process nested DTO fields too. [see more below](#nested_dto):

- `@NestedDtoRule`

If you want to use your own generator for specific field, next annotation is used [see more below](#custom_rule):

- `@CustomRule`

## Usage

1. [Annotation Based Configuration](#annotation_based_config)
2. [Grouping of Rules](#rules_grouping)
3. [Known Type's Generation](#known_types_generation)
4. [Configuration management](#config_management)
5. [Rule's Remarks](#boundary)
6. [User's Generators](#user_generators)
7. [Nested DTO](#nested_dto)
8. [Custom Rules](#custom_rule)
9. [Requirements for POJO classes](#pojo_requirements)
10. [More Examples](#more_examples)

<a name="annotation_based_config"></a>

### 1. Annotation Based Configuration

For example, if we're using next rules:

```java
public class Dto {

    @StringRule(words = {"Kate", "John", "Garcia"})
    public String name;

    @IntegralRule(minInt = 18, maxInt = 45)
    public Integer age;

    @EnumRule(possibleEnumNames = {"DRIVER_LICENCE", "PASSPORT"})
    public DocType docType;

    @DateTimeRule(chronoUnitShift = @ChronoUnitShift(unit = DAYS, leftBound = -100, rightBound = -10))
    public LocalDateTime lastVisit;

}
```

In order to generate an object filled with random data, just create a **DtoGeneratorBuilder** instance and call  **
generateDto()** method:

```java
public class Foo {
    void bar() {
        // 1. You can pass a DTO class
        // and every call of generateDto() method will provide new instance with random data
        DtoGenerator<Dto> dtoGenerator = DtoGenerator.builder(Dto.class).build();
        Dto dtoInstance = dtoGenerator.generateDto();

        // 2. Or you can pass instance of DTO to be filled with random data
        // In this case, every call of generateDto method will generate new data in the same DTO
        Dto dto = DtoGenerator.builder(new Dto()).build().generateDto();
    }
}
```

As the result we'll have an object containing random data, generated based on generation rules:

```json
{
  "name": "John",
  "age": 33,
  "docType": "PASSPORT",
  "lastVisit": [
    2023, 2, 7, 18, 32, 55, 209000000
  ]
}
```

<a name="rules_grouping"></a>

### 2. Grouping of Rules

You may put multiple `@Rule` annotations on the one field, each one has to belong to different group.
Then, you may select group or groups that you prefer to use in DTO generation.

For example, next config:

```java
public class Example7 {

    public static class Dto7 {

        @StringRule(words = {"Alice", "Maria"}, group = "GIRL")
        @StringRule(words = {"Peter", "Clint"}, group = "BOY")
        public String name;

        @IntegralRule(minInt = 18, maxInt = 30, group = "YOUNG")
        @IntegralRule(minInt = 31, maxInt = 60, group = "MATURE")
        public Integer age;

    }

    public static void main(String[] args) {
        Dto7 dto = DtoGenerator.builder(Dto7.class)
                .includeGroups("MATURE", "BOY")
                .build()
                .generateDto();
    }
}
```

will produce something like:

```json
{
  "name": "Clint",
  "age": 45
}
```

<a name="known_types_generation"></a>

### 3. Known Types Generation

Configuration parameter `generateAllKnownTypes` allows to generate field values of known types, without the need
to put *@Rules* annotation on the fields.
Known types are:

- types for which `@Rule` annotations exists;
- types for which generator was specified via `setGenerator(...)` methods of `DtoGeneratorBuilder`;

There are several ways to configure generators, one of them is - static configuration:

```java
        ...
        // static configuration instance is accessible this way 
        DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().setGenerateAllKnownTypes(true);

        // and the same static configuration instance is available directly from DtoGeneratorBuilder
        DtoGenerator.builder(Dto.class).getStaticConfig().setGenerateAllKnownTypes(true);
        ...
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

As the result we'll have an object containing random data, based on default or overridden generation rules
configuration:

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

There are 4 configuration levels, each next configuration level overrides previous ones:

1. Annotation config -
    1. parameters provided from `@Rule` annotation of the DTO field
    2. when known type is generating without `@Rule` annotation, default values from corresponding `@Rule` annotation
       are using
2. Static config - one static configuration for all and every `DtoGeneratorBuilder`
3. User's config - each `DtoGeneratorBuilder` instance has its own configuration:
    1. Config related to type
    2. Config related to field by its name

For example:

```java
public class Example3 {

    public static class Dto3 {

        // annotation config
        @StringRule(minLength = 1)
        public String exampleString;

    }

    public static void main(String[] args) {

        DtoGeneratorBuilder<Dto3> builder = DtoGenerator.builder(Dto3.class);

        // static config
        builder.getStaticConfig().getTypeGeneratorsConfig().getStringConfig().setMaxLength(5);

        // one of the ways to override instance config of "String" type - directly set a config instance
        builder.setTypeGeneratorConfig(
                String.class,
                StringConfig.builder().minLength(5).chars("z").build());

        // each known type also has the lazy getter for retrieving config instance
        // if config for this type has already been set, we are updating it, otherwise getting config instance lazily
        builder.getConfig().getTypeGeneratorsConfig()
                .getStringConfig()
                .setChars("x");

        // instance config for the field
        builder.setTypeGeneratorConfig("exampleString", StringConfig.builder().minLength(5).build());

        Dto3 dto = builder.build().generateDto();
    }
}
```

The result string due to configuration will always be the next:

```json
{
  "exampleString": "xxxxx"
}
```

<a name="boundary"></a>

### 5. Boundary

Boundary is used to generate boundary values.
It is possible to assign boundary parameter either to the every or only to certain DTO fields.

Boundary values:

- MIN_VALUE
- MAX_VALUE
- RANDOM_VALUE
- NULL_VALUE

For example:

```java
public class Example4 {

    static class Dto4 {

        @StringRule(minLength = 1, maxLength = 150)
        public String name;

        @IntegralRule(minInt = 18, maxInt = 45)
        public Integer age;

        @CollectionRule(minSize = 1, maxSize = 20, element = @Entry(stringRule =
        @StringRule(minLength = 5, maxLength = 10)))
        public List<String> children;

    }

    public static void main(String[] args) {

        Dto4 dto = DtoGenerator.builder(Dto4.class)
                .setBoundary(MIN_VALUE)
                .setBoundary("age", MAX_VALUE)
                .build()
                .generateDto();
    }
}
```

The result dto will look like:

```json
{
  "name": "d",
  "age": 45,
  "children": [
    "i,+Ct"
  ]
}
```

<a name="user_generators"></a>

### 6. User Generator of Any Type

You may override generators of known types or set generator for any type you want:

1. Override/set generator of specified type
2. Override/set for specified field by field name

In order to have more control on your custom generators, your generator may implement CustomGenerator
interfaces [see more below](#custom_rule).

```java
public class Example5 {

    static class Dto5 {

        @StringRule(minLength = 1, maxLength = 150)
        String name;

        @StringRule
        String secondName;

        @CollectionRule(minSize = 1, maxSize = 1)
        List<String> children;

        WhoKnowsWhatType whoKnowsWhatType;
    }

    // Any type, for example
    static class WhoKnowsWhatType {

        public WhoKnowsWhatType(int mysteryNumber) {
            this.mysteryNumber = mysteryNumber;
        }

        int mysteryNumber;
    }

    // Generator have to implement `Generator` interface or be lambda expression
    static class MyStringGenerator implements Generator<String> {
        @Override
        public String generate() {
            return "Here can be your generation logic";
        }
    }

    public static void main(String[] args) {

        Dto5 dto = DtoGenerator.builder(Dto5.class)
                .generateKnownTypes()
                // default type's generator builder is used to override generator for all fields annotated with @StringRule
                .setGenerator(String.class, new MyStringGenerator())
                // lambda expressions can be used
                .setGenerator(WhoKnowsWhatType.class, () -> new WhoKnowsWhatType(RandomUtils.nextInt(-23, 23)))
                // generator may be set for specific field
                .setGenerator("secondName", () -> RandomUtils.getRandomItem("Smith", "Claus"))
                .build()
                .generateDto();
    }
}
```

The result dto may look like:

```json
{
  "name": "Here can be your generation logic",
  "secondName": "Claus",
  "children": [
    "Here can be your generation logic"
  ],
  "whoKnowsWhatType": {
    "mysteryNumber": -10
  }
}
```

<a name="nested_dto"></a>

### 7. Nested DTO

Put ```@NestedDtoRule``` on the DTO field to tell DtoGenerator to process its fields also as DTO fields.
In order to override configuration or generators of fields within nested DTO, use "path" to the field separated by dots.

Inheritance is supported and if you want to get access to field of parent class, use exactly the same "path"
as there is no inheritance and the field belongs to the child class itself.

For example:

```java
public class Example6 {

    static class Dto6 {

        @NestedDtoRule
        DtoNested nestedDto;
    }

    static class DtoNested {

        @StringRule(regexp = "[+]89 [(]\\d{3}[)] \\d{3}[-]\\d{2}-\\d{2}")
        String phoneNumber;

        @StringRule(words = {"Home", "Work"})
        String phoneType;

        @StringRule
        String comment;
    }

    public static void main(String[] args) {
        Dto6 dto = DtoGenerator.builder(Dto6.class)
                .setBoundary("nestedDto.comment", NULL_VALUE)
                .setGenerator("nestedDto.phoneType", () -> "Other")
                .build()
                .generateDto();
    }
}
```

Will result something like:

```json
{
  "nestedDto": {
    "phoneNumber": "+89 (082) 948-90-10",
    "phoneType": "Other",
    "comment": null
  }
}
```

<a name="custom_rule"></a>

### 8. Custom Rules

You may design your custom generator for any type you want and use it with `@CustomRule` annotation.
To do this, you need to implement one or more `CustomGenerator*` interfaces:

| Interface                     | Feature                                                                                                                                                   |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CustomGenerator`             | base interface, allows to create non configurable type generators                                                                                         |
| `CustomGeneratorArgs`         | allows to pass array of arguments to generator via `setGeneratorArgs(...)` methods or `@CustomRule` annotation                                            |
| `CustomGeneratorConfigMap`    | allows to pass key-value parameters to generator via `addGeneratorParameter(...)` methods or `@CustomRule` annotation                                     |
| `CustomGeneratorBoundary `    | allows to pass boundary param to generator via `setBoundary(...)` methods or `@CustomRule` annotation                                                     |
| `CustomGeneratorDtoDependent` | provides a reference to the generating DTO instance to the generator<br/>(to check if the DTO fields required for generation have been already filled in) |

Custom generators can also be set with `setGenerator(...)` method of `DtoGeneratorBuilder`
, [linkig generator directly for the generated type](#user_generators)

More usage examples you may see in the project with
examples: [Dto Generator Examples project](dto-generator-examples/README.md)

Example of custom generator with args:

```java
public class Example8 {

    /**
     * Dto to generate
     */
    static class Dto8 {

        @CustomRule(generatorClass = FlowerGenerator.class, args = {"10", "15", "50"})
        private Flower flower;
    }

    /**
     * Type we want to generate via custom generator
     */
    static class Flower {

        int petalsNumber;

        public Flower(int petalsNumber) {
            this.petalsNumber = petalsNumber;
        }
    }

    /**
     * Custom generator requires of passing of arguments
     */
    static class FlowerGenerator implements CustomGeneratorArgs<Flower> {

        private int[] docType;

        @Override
        public void setArgs(String... args) {
            this.docType = Arrays.stream(args).mapToInt(Integer::parseInt).toArray();
        }

        @Override
        public Flower generate() {
            return new Flower(RandomUtils.getRandomItem(docType));
        }
    }
}
```

<a name="pojo_requirements"></a>

### 9. Requirements for DTO classes

When DTO instantiated by class:

``` DtoGenerator.builder(Foo.class).build().generateDto(); ```

1. DTO class must have declared no-args constructor with any or default constructor (don't have any constructors);
2. DTO class cannot be an inner class (non-static nested class).

Supported:

1. DTO classes may extend abstract or concrete classes via inheritance
2. DTO class may be static nested class

<a name="more_examples"></a>

### 10. More Examples

More examples you can find in the: [Dto Generator Examples project](dto-generator-examples/README.md)
