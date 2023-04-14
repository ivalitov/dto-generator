# DTO Generator Examples

## Description

This project contains examples of the implementation of custom generators.

```diff
- This is an experimental feature, it will be significantly changed in future releases
```

## Content

**1 -** The package `org.laoruga.dtogenerator.examples.generators.custom` contains examples of the implementation of
annotations:

| Interface                     | Feature                                                                                                                                        |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `CustomGenerator`             | base annotation, allows to create non configurable type generators                                                                             |
| `CustomGeneratorArgs`         | allows to pass array of arguments to type generator                                                                                            |
| `CustomGeneratorDtoDependent` | provides a reference to the DTO instance to the generator<br/>(to check if the DTO fields required for generation have already been filled in) |
| `CustomGeneratorRemarkable`   | allows to pass objects to a custom type generator as remarks                                                                                   |


Please note that a custom generator can implement any number of annotations, any one or all of them at once.
In examples, each of custom generators implements only one annotation, in order to emphasize their functionality.

**2 -** The usage of generators you can find in tests `laoruga.dto.generator.examp les.Examp lesTest`. 
Each test logs the body of generated DTO in the JSON format.