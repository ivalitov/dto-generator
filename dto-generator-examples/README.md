# DTO Generator Examples

## Description

This project contains examples of the implementation of custom generators.

## Content

**1 -** The package `org.laoruga.dtogenerator.examples.generators.custom` contains examples of the implementation of
annotations:

| Interface                     | Feature                                                                                                                                                   |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CustomGenerator`             | base interface, allows to create non configurable type generators                                                                                         |
| `CustomGeneratorArgs`         | allows to pass array of arguments to generator                                                                                                            |
| `CustomGeneratorConfigMap`    | allows to pass key-value parameters to generator                                                                                                          |
| `CustomGeneratorRemark `      | allows to pass remarks to generator                                                                                                                       |
| `CustomGeneratorDtoDependent` | provides a reference to the generating DTO instance to the generator<br/>(to check if the DTO fields required for generation have been already filled in) |


Please note that a custom generator can implement any number of annotations, any one or all of them at once.
In examples, each of custom generators implements only one annotation, in order to emphasize their functionality.

**2 -** The usage of generators you can find in tests `laoruga.dto.generator.examp les.Examp lesTest`. 
Each test logs the body of generated DTO in the JSON format.