
# SART Application Anatomy


In a nutshell SART Framework provides a way to apply important patterns of _Domain Driven Design_ in the data streaming world. The building blocks for the proposed data management application are the well known _Domain Aggregates_, _Domain Commands_, _Domain Events_, _Domain Queries_ and _Domain Results_ as a tangible manifestation of the use-cases captured from a given domain. It is NOT the goal of current document to justify or familiarize the reader with the domain driven software development philosophy but rather demonstrate the practical steps needed to build a SART based vertical application.  

Let us assume a hypothetical simulation field where the principal use-case is to process existing data structures named _Input Scenarios_ and to output derived data structures called _Simulation Results_. Additionally, a variable number of _Input Scenarios_ are generated from the original _Source Scenario_ by adjusting some of the parameterized aspects. 

```
Source Scenario  1 -> N  Input Scenario  1 -> 1 Simulation Result 
```

