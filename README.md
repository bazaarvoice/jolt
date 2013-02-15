JOLT
========

Jolt is a JSON to JSON transformation library written in Java.

Jolt operates and produces "hydrated" JSON : an in-memory tree of Map<String,Object>, List<Object>, String, Integer, Double objects not text.
Jolt lets Jackon serialize and deserialize the JSON text, and worry about commas and closing brackets.

Jolt provides a set transform components, that can be chained together to form the overall JSON to JSON transform.

Each transform component covers a specific transform task with a Json format DSL for that task.

The provided transform components are

    shift : copy data from the input tree and put it the output tree
    default : apply default values to the tree
    remove : remove data from the tree
    sort : sort the Map key values alphabetically ( for debugging and human readability )
    java : run any Java class that implements the Transform interface

Jolt should do 90% of what you want, with the "java" component giving you a way to implement the last 10%.

Prior to Jolt, there were two approaches to doing Json to Json transforms.

1) JSON -> XML -> XSLT or STX -> XML -> JSON
Aside from being a Rube Goldberg approach, XSLT is more complicated than Jolt because it is trying to do the whole transform in a single shot with a single DSL.

2) Write a Template (Velocity, FreeMarker, etc) that take hydrated JSON input and write textual JSON output
With this approach you are having to work from the output format backwards to the input, which is complex for any non-trivial transform.
Eg, the structure of your template will be dictated by the output JSON format, and you will end up coding a parallel tree walk of the input data and the output format in your template.
Jolt works forward from the input data to the output format which is simpler, and it does the parallel tree walk for you.






