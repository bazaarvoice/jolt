Jolt
========

Jolt is a JSON to JSON transformation library written in Java.

The "specification" for the transformation is itself a JSON document.

Useful For
-------
1. Taking JSON data from ElasticSearch, MonogoDb, Cassandra, etc and transforming it before sending it off to the world.
1. Extracting data from a large JSON document for your component's consumption.

Overview
------------

Jolt operates on and produces "hydrated" JSON : in-memory tree of Maps, Lists, Strings, etc.

Jolt lets Jackson serialize and deserialize the JSON text which offloads the work of handling commas and closing brackets.

Jolt provides a set transform components, that can be "chained" together to form the overall JSON to JSON transform.

Each transform component covers a specific transform task with a unique JSON format DSL for that task.

The provided transforms are:

    shift   : copy data from the input tree and put it the output tree
    default : apply default values to the tree
    remove  : remove data from the tree
    sort    : sort the Map key values alphabetically ( for debugging and human readability )
    java    : run any Java class that implements the `Transform` interface

The out-of-the-box Jolt transforms should be able to do 90% of what you need, with the `java` component giving you a way to implement the last 10%.

Jolt Components
-----------------

This project produces two maven artifacts.

1. `jolt-core` : only one dependency on apache.commons for StringUtils
    * The goal is for the `jolt-core` artifact to be pure Java, so that it does not cause any dependency issues.
2. `json-utils` : Jackson wrapper and testing utilities.   Used by jolt-core as a test dependency.
    * If you are willing to pull in Jackson 2, this artifact provides nice utility methods.

Maven Dependency
```
<dependency>
    <groupId>com.bazaarvoice.jolt</groupId>
    <artifactId>jolt-core</artifactId>
    <version>${jolt.version}</version>
</dependency>
<dependency>
    <groupId>com.bazaarvoice.jolt</groupId>
    <artifactId>json-utils</artifactId>
    <version>${jolt.version}</version>
</dependency>
```

Documentation
--------------

Jolt [Slide Deck](https://docs.google.com/presentation/d/1sAiuiFC4Lzz4-064sg1p8EQt2ev0o442MfEbvrpD1ls/edit?usp=sharing) : covers motivation, development, and transforms.

Javadoc explaining each transform DSL :

* [shift](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Shiftr.java)
* [default](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Defaultr.java)
* [remove](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Removr.java)
* [sort](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Sortr.java)
* java : Implement the Transform or SpecTransform interfaces
    * [Transform](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Transform.java) interface
    * [SpecTransform](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/SpecTransform.java)

Running a Jolt transform means instantiating an instance of the `Chainr` class with its [Json specification](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Chainr.java) file that lists the transform steps to perform.

JSON Spec side look like : example from Chainr [unit test](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/test/resources/json/chainr/firstSample.json).

The Java side looks like :
```
Chainr chainr = new Chainr( ...getResourceAsStream( "/path/to/chainr/spec.json" ) );

Object input = elasticSearchHit.getSource(); // ElasticSearch already returns hydrated JSon

Object output = chainr.transform( input );

return output;
```


Shiftr DSL Syntax
---------------

Shiftr is transform that does the most "heavy lifting".   To learn the Shiftr DSL, please look at our unit tests, ([shiftr tests](https://github.com/bazaarvoice/jolt/tree/master/jolt-core/src/test/resources/json/shiftr)), for nice bite sized transform examples, and read the Javadoc.
The unit tests all follow the pattern :
```
{
    "input": {
        // sample input
    },

    "spec": {
        // transform spec
    },

    "expected": {
        // what the output of the transform looks like
    }
}
```

Look at the "input" and "output" json, get an understanding of how data is moving, and then look at the transform spec to see how it facilitates it.

For reference, [this](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/test/resources/json/shiftr/firstSample.json) was the very first test we wrote.

Alternatives
-------------

Prior to Jolt, there were two approaches to doing Json to Json transforms.

1) JSON -> XML -> XSLT or STX -> XML -> JSON

Aside from being a Rube Goldberg approach, XSLT is more complicated than Jolt because it is trying to do the whole transform with a single DSL.

2) Write a Template (Velocity, FreeMarker, etc) that take hydrated JSON input and write textual JSON output

With this approach you are having to work from the output format backwards to the input, which is complex for any non-trivial transform.
Eg, the structure of your template will be dictated by the output JSON format, and you will end up coding a parallel tree walk of the input data and the output format in your template.
Jolt works forward from the input data to the output format which is simpler, and it does the parallel tree walk for you.

Performance
-----------

The primary goal of Jolt was to improve "developer speed" by providing the ability to have a declarative rather than imperative transform.
That said, Jolt should have a better runtime than the alternatives listed above.

Work has been done to make the stock Jolt transforms performant :

1. Transforms can be initialized once with their spec, and re-used many times in a mult-threaded environment.
    * We reuse initialized Jolt transforms to service multiple web requests from a DropWizard service.
2. "*" wildcard logic was redone to reduce the use of Regex in the common case, which was a dramatic speed improvement.
3. The parallel tree walk performed by Shiftr was optimized.

Two things to be aware of :

1. Jolt is not "stream" based, so if you have a very large Json document to transform you need to have enough memory to hold it.
2. The transform process will create and discard a lot of objects, so the garbage collector will have work to do.

# Code Coverage

Cobertura

    mvn cobertura:cobertura
    open jolt-core/target/site/cobertura/index.html

Currently code coverage is at 89% line, and 83% branch.