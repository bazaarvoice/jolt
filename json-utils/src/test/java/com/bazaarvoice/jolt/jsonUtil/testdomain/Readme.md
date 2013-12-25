# What is going on here?

This started out was a unit test for the ability to create a JsonUtil with a custom configured ObjectMapper, but
morphed into fancy Jackson Serialization and Deserialization experimentation.

# Fancy Jackson

The end goal was, I wanted to have Json that looks sort of like ElasticSearch's,
 but that could serialize and deserialize into "nice" concrete Java Objects.

Start by looking at the Json on /resources/jsonUtils/testdomain/*.

## TestDomain One : Simple Recursive Polymorphic JSON deserialization in Jackson 2.2

The Filter classes all have simple constructors and just use basic Jackson features.
As such, the JSON representation is overly wordy.

In the MappingTest class a JsonDeserializer is defined and registerd as a Module to the Jackson
ObjectMapper.   It is the thing that differentiates Real and Logical filters.

The "secret sause" was to create a "sub" JsonParser with the "codec" of the parent.

    ObjectNode root = jp.readValueAsTree();

    // Examine the root node and figure out what it is.

    // Build a JsonParser passing in our objectCodec so that the subJsonParser
    //  knows about our configured Modules and Annotations
    JsonParser subJsonParser = root.traverse( jp.getCodec() );

    // Now have the subParser read the value as the appropriate type
    subJsonParser.readValueAs( LogicalFilter1.class );
    OR
    subJsonParser.readValueAs( RealFilter.class );

## TestDomain Two

Simplified the Json by adding complexity to the JsonSerializer and JsonDeserializer Module in the MappingTest2 class.

## TestDomain Three

Uses the same JSON structure as TestDomain Two, but moves some of the code out of the Module
in MappingTest2 to the LogicalFilter3 class, via class level @JsonSerialize and @JsonDeserialize.

## TestDomain Four

In the previous formulations, the "value" of a RealFilter was always a String.  Here I make it typed (String, Integer,
Boolean).

## TestDomain Five

Make the "value" of a RealFilter be a typed List of values.   This allowed for more overlap between the Real and
Logical QueryFilters as they can share a getValues( List<T> list ) interface, which is kinda nice.

Tried again to avoid needed a custom JacksonModule for the ObjectMapper, but still could not get it to work.

Was able to remove the custom @JsonSerialize and @JsonDeserialize inner classes from LogicalFilter5 by making
it "extend Map".   Works but is kunky and I would not use it in practice.
