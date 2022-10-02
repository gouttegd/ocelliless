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


Copying
-------
Ocelliless is distributed under the terms of the GNU General Public
License, version 3 or higher. The full license is included in the
[COPYING file](COPYING) of the source distribution.


Repository
----------
The latest source code is available in a Git repository at
<https://git.incenp.org/damien/ocelliless>.
