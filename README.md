Ocelliless - Tests driver for OWL ontologies
============================================

Ocelliless is a tool intended to facilitate “unit-testing” of OWL
ontologies. It attempts to test an ontology by repeatedly merging it
with a series of “test ontologies” and reasoning over the product of
each merge. A test is said to “pass” if it produces a consistent
ontology with no unsatisfiable classes when merged with the main
ontology.

The name comes the _Drosophila melanogaster_ gene _ocelliless_, which is
also known as _orthodenticle_ or _odt_. Here, Ocelliless is an “Ontology
Tests Driver” or “ODT”.


Installation
------------
Build the program with [Maven](https://maven.apache.org/) by running,
from the source directory:

```
$ mvn clean package
```

This will produce a file `ocelliless-X.Y.Z-jar-with-dependencies.jar` in
the `target` subdirectory.

Run the program with:

```
$ java -jar target/ocelliless-X.Y.Z-jar-with-dependencies.jar
```


Basic usage
-----------
A “unit test” looks like the following:

```
@prefix : <http://purl.obolibrary.org/obo/fbbt/tests/test2.ttl#test_> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix odt: <https://incenp.org/ocelliless/> .
@prefix obo: <http://purl.obolibrary.org/obo/> .

<http://purl.obolibrary.org/obo/fbbt/tests/test2.ttl> rdf:type owl:Ontology ;
                                                      odt:target obo:FBbt_00007004 .

:001 rdf:type owl:Class ;
     rdfs:subClassOf [ rdf:type owl:Restriction ;
                       owl:onProperty obo:BFO_0000050 ;
                       owl:someValuesFrom obo:FBbt_00004928
                     ] ,
                     [ rdf:type owl:Restriction ;
                       owl:onProperty obo:BFO_0000050 ;
                       owl:someValuesFrom obo:FBbt_00007004
                     ] ;
     rdfs:label "part_of testis is part_of male organism"^^xsd:string ;
     obo:IAO_0000115 "Something that is part of the testis should be part of a male organism."^^xsd:string .
```

This test checks the _Drosophila Anatomy Ontology_ (FBbt) to ensure that
any class that is `part_of` a male-specific structure (here, the testis,
FBbt:00004928) is automatically classified as `part_of` a male organism
(FBbt:00007004).

Here is another test (not repeating the prefixes, for brevity) that
checks that something cannot be simultaneously `part_of` the larva
(FBbt:00001727) and `part_of` the adult (FBbt:00003004) – this is an
example of a “expected failure” (_XFAIL_): merging this with the FBbt
ontology is _supposed_ to produce an unsatisfiable class (so the test
will actually _fail_ if it does not produce it):

```
<http://purl.obolibrary.org/obo/fbbt/tests/test1.ttl> rdf:type owl:Ontology ;
                                                      odt:target obo:FBbt_00001127 ;
                                                      odt:xfail "yes"^^xsd:string .

:001 rdf:type owl:Class ;
     rdfs:subClassOf [ rdf:type owl:Restriction ;
                       owl:onProperty obo:BFO_0000050 ;
                       owl:someValuesFrom obo:FBbt_00001727
                     ] ,
                     [ rdf:type owl:Restriction ;
                       owl:onProperty obo:BFO_0000050 ;
                       owl:someValuesFrom obo:FBbt_00003004
                     ] ;
     rdfs:label "larval - adult mix up"^^xsd:string ;
     obo:IAO_0000115 "A class that is both part_of some larva and part_of some adult - should be unsatisfiable."^^xsd:string .
```

Run those two tests against the released version of FBbt:

```sh
java -jar ocelliless-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  -c annotations.ofn -i fbbt-full.owl tests/test1.ttl tests/test2.ttl
ocelliless: Test 1/2 (tests/test1.ttl): PASS
ocelliless: Test 2/2 (tests/test2.ttl): PASS
ocelliless: fbbt.owl: 2/2 tests passed
ocelliless: Saving annotations component to annotations.ofn
```

Here, both tests have passed. The file `annotations.ofn` contains
annotations that can be merged into the tested ontology to add links
pointing to the test files (assuming they are published alongside the
ontology itself):

```
Prefix(owl:=<http://www.w3.org/2002/07/owl#>)
Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)
Prefix(xml:=<http://www.w3.org/XML/1998/namespace>)
Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)
Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)


Ontology(
Declaration(AnnotationProperty(<https://incenp.org/ocelliless/test>))

AnnotationAssertion(<https://incenp.org/ocelliless/test> <http://purl.obolibrary.org/obo/FBbt_00001127> <http://purl.obolibrary.org/obo/fbbt/tests/test1.ttl>)
AnnotationAssertion(<https://incenp.org/ocelliless/test> <http://purl.obolibrary.org/obo/FBbt_00007004> <http://purl.obolibrary.org/obo/fbbt/tests/test2.ttl>)
)
```


Copying
-------
Ocelliless is distributed under the terms of the GNU General Public
License, version 3 or higher. The full license is included in the
[COPYING file](COPYING) of the source distribution.


Repository
----------
The latest source code is available in a Git repository at
<https://git.incenp.org/damien/ocelliless>.
