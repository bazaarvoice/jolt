Jolt
========

JSON to JSON transformation library written in Java where the "specification" for the transform is itself a JSON document.

### Useful For

1. Transforming JSON data from ElasticSearch, MonogoDb, Cassandra, etc before sending it off to the world
1. Extracting data from a large JSON documents for your own consumption

## Table of Contents

   * [1 Overview](#Overview)
   * [2 Documentation](#Documentation)
   * [3 Shiftr Transform DSL](#Shiftr_Transform_DSL)
   * [4 Getting Started](#Getting_Started)
   * [5 Getting Transform Help](#Getting_Transform_Help)
   * [6 Alternatives](#Alternatives)
   * [7 Performance](#Performance)
   * [8 CLI] (#CLI)
   * [9 Code Coverage](#Code_Coverage)

## <a name="Overview"></a> Overview

Jolt provides a set transform components, that can be "chained" together to form the overall JSON to JSON transform.

Jolt consumes and produces "hydrated" JSON : in-memory tree of Maps, Lists, Strings, etc.

Jolt lets Jackson serialize and deserialize the JSON text which offloads the work of handling commas and closing brackets.

Each transform component covers a specific transform task with a unique JSON format DSL for that task.

The provided transforms are:

    shift   : copy data from the input tree and put it the output tree
    default : apply default values to the tree
    remove  : remove data from the tree
    sort    : sort the Map key values alphabetically ( for debugging and human readability )
    java    : run any Java class that implements the `Transform` interface

The out-of-the-box Jolt transforms should be able to do 90% of what you need, with the `java` component giving you a way to implement that last 10%.

## <a name="Documentation"></a> Documentation

Jolt [Slide Deck](https://docs.google.com/presentation/d/1sAiuiFC4Lzz4-064sg1p8EQt2ev0o442MfEbvrpD1ls/edit?usp=sharing) : covers motivation, development, and transforms.

Javadoc explaining each transform DSL :

* [shift](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Shiftr.java)
* [default](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Defaultr.java)
* [remove](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Removr.java)
* [sort](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Sortr.java)
* java : Implement the Transform or SpecTransform interfaces
    * [Transform](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Transform.java) interface
    * [SpecTransform](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/SpecTransform.java)

Running a Jolt transform means creating an instance of [Chainr](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Chainr.java)  with a list of transforms.

The JSON spec for Chainr looks like : [unit test](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/test/resources/json/chainr/firstSample.json).

The Java side looks like :
```
Chainr chainr = new Chainr( ...getResourceAsStream( "/path/to/chainr/spec.json" ) );

Object input = elasticSearchHit.getSource(); // ElasticSearch already returns hydrated JSon

Object output = chainr.transform( input );

return output;
```

### <a name="Shiftr_Transform_DSL"></a> Shiftr Transform DSL

The Shiftr transform generally does most of the "heavy lifting" in the transform chain.
To see the Shiftr DSL in action, please look at our unit tests ([shiftr tests](https://github.com/bazaarvoice/jolt/tree/master/jolt-core/src/test/resources/json/shiftr)) for nice bite sized transform examples, and read the Shiftr [docs](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Shiftr.java).

Our unit tests follow the pattern :
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

We read in "input", apply the "spec", and [Diffy](https://github.com/bazaarvoice/jolt/blob/master/json-utils/src/main/java/com/bazaarvoice/jolt/Diffy.java) it against the "expected".

To learn the Shiftr DSL, examine "input" and "output" json, get an understanding of how data is moving, and *then* look at the transform spec to see how it facilitates the transform.

For reference, [this](https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/test/resources/json/shiftr/firstSample.json) was the very first test we wrote.


## <a name="Getting_Started"></a> Getting Started

Has it's [own doc](gettingStarted.md).

## <a name="Getting_Transform_Help"></a> Getting Transform Help

If you can't get a transform working and you need help, create and Issue in Jolt (for now).

Make sure you include what your "input" is, and what you want your "output" to be.

## <a name="Alternatives"></a> Alternatives

Aside from writing your own custom code to do a transform, there are two general approaches to doing Json to Json transforms in Java.

1) JSON -> XML -> XSLT or STX -> XML -> JSON

Aside from being a Rube Goldberg approach, XSLT is more complicated than Jolt because it is trying to do the whole transform with a single DSL.

2) Write a Template (Velocity, FreeMarker, etc) that take hydrated JSON input and write textual JSON output

With this approach you are working from the output format backwards to the input, which is complex for any non-trivial transform.
Eg, the structure of your template will be dictated by the output JSON format, and you will end up coding a parallel tree walk of the input data and the output format in your template.
Jolt works forward from the input data to the output format which is simpler, and it does the parallel tree walk for you.

## <a name="Performance"></a> Performance

The primary goal of Jolt was to improve "developer speed" by providing the ability to have a declarative rather than imperative transforms.
That said, Jolt should have a better runtime than the alternatives listed above.

Work has been done to make the stock Jolt transforms performant :

1. Transforms can be initialized once with their spec, and re-used many times in a mult-threaded environment.
    * We reuse initialized Jolt transforms to service multiple web requests from a DropWizard service.
2. "*" wildcard logic was redone to reduce the use of Regex in the common case, which was a dramatic speed improvement.
3. The parallel tree walk performed by Shiftr was optimized.

Two things to be aware of :

1. Jolt is not "stream" based, so if you have a very large Json document to transform you need to have enough memory to hold it.
2. The transform process will create and discard a lot of objects, so the garbage collector will have work to do.

## <a name="CLI"></a> Diffy CLI

The bin/ directory contains a command-line tool for using Diffy. To use it,

#### Install/Update
1. clone the project (e.g., to $JOLT_CHECKOUT)
1. cd $JOLT_CHECKOUT; git pull; mvn clean package
1. add $JOLT_CHECKOUT/bin/ to your PATH.

#### Using Curl
The Diffy tool has the ability to accept input from standard in.
This can be useful if you want to compare output from curl to data in a file without having to go through the trouble of piping the curl output into a file and then subsequently calling diffy.

For example, instead of doing this:

    curl -s "http://some.host.com/stuff/data.json" > data.json
    diffy data.json moreData.json
You can do this:

    curl -s "http://some.host.com/stuff/data.json" | diffy -i moreData.json

## <a name="Code_Coverage"></a> Code Coverage

For the moment we jave Cobertura configured in our poms.
When we move to a proper open source CI build, this can go away.

    mvn cobertura:cobertura
    open jolt-core/target/site/cobertura/index.html

Currently code coverage is at 89% line, and 83% branch.
