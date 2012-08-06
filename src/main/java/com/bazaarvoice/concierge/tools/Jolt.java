package com.bazaarvoice.concierge.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JOLT is the JSON Object Language for Transformations (silly contrived name). JOLT encodes
 * JSON to JSON object transformations in a declarative JSON-based language. Instances of this
 * class execute JOLT transformations given a JSON input and transform spec both in Jackson-style
 * maps of maps.
 *
 * Each entry in a JOLT transform says where to put some data in an output object, if encountered
 * in the input object. Here is an example transform spec (annotated with JSON-illegal comments):
 *
 * <pre>
 * {
 *   "rating": {
 *     "primary": {
 *         "value": "Rating",                           // rating.primary.value from the input goes to output.Rating
 *         "max": "RatingRange"                         // rating.primary.max from the input goes to output.RatingRange
 *     },
 *     "*": {                                           // rating.[anything-but-primary]
 *         "value": "SecondaryRatings.$$1.Value",       // rating.[*-match].value from the input goes to output.SecondaryRatings.[*-match].Value
 *         "max": "SecondaryRatings.$$1.Range",         // rating.[*-match].max from the input goes to output.SecondaryRatings.[*-match].Range
 *         "&": "SecondaryRatings.$$1.Id"               // [*-match] goes to output.SecondaryRatings.[*-match].Id
 *     }
 *   }
 * }
 * </pre>
 *
 * A JOLT processor walks the input document, looking up each node in the transform spec and
 * making the specified updates in the output document. This is best explained with an example input file:
 *
 * <pre>
 * {                            // no rules for all input
 *   "rating": {                // no rules for rating
 *       "primary": {           // no rules for rating.primary
 *           "value": 3,        // 3 (rating.primary.value) goes to output.Rating
 *           "max": 5           // 5 (rating.primary.max) goes to output.RatingRange
 *       },
 *       "quality": {           // "quality" (rating.*.$$) goes to output.SecondaryRatings.quality.Id
 *           "value": 3,        // 3 (rating.*.value) goes to output.SecondaryRatings.quality.Value
 *           "max": 7           // 7 (rating.*.max) goes to output.SecondaryRatings.quality.Range
 *       }
 *   }
 * }
 * </pre>
 *
 * Thus, applying the transform to the above input yields the following output:
 *
 * <pre>
 * {
 *   "Rating":3,
 *   "RatingRange":5,
 *   "SecondaryRatings":{
 *      "quality":{
 *         "Range":7,
 *         "Value":3,
 *         "Id":"quality"
 *      }
 *   }
 * }
 * </pre>
 *
 * Here are the rules for applying the transform:
 * - if the transform has a scalar attribute for the input attribute, that scalar contains the path for putting it in the output
 * - paths contained at the scalar transform attributes are in dot-notation for referencing JSON
 * - "*" as a key matches any key that does not have its own entry
 * - "&" as a key within an object evaluates to the key that references the object
 * - "$$[index]" within a path is a zero-major reference to the keys in the input document starting with current. thus $$0 evaluates to the key
 *   for the current attribute, $$1 evaluates to the key for the parent attribute, and so on.
 */
public class Jolt {

    private static final String SPEC_KEY_REFERENCES_INPUT_KEY = "&";
    private static final String SPEC_KEY_REFERENCES_INPUT_VALUE = "@";

    // TODO construction option that takes a warning/info listener

    // TODO support for lists in mappings

    public Map<String, Object> xform(Map<String, Object> input, Map<String, Object> spec) {

        // TODO defense

        Map output = new HashMap();
        this.xform( input, spec, output, new Path() );
        return output;
    }

    public void xform(Map<String, Object> input, Map<String, Object> spec, Map<String, Object> output, Path inputPath) {

        // TODO defense

        for( String key: input.keySet() ) {

            // process the next top-level item in the input:
            Path pathToInputItem = new Path( inputPath, key );
            Object inputItem = input.get( key );

            // find the part in the spec that applies to
            // the next top-level item to process:
            String specKey = this.findMatchingSpecKey( spec, key );
            if (specKey == null) {      // no spec for this item
                continue;               // that's okay--omit it and keep going.
            }
            Object subSpec = spec.get( specKey );

            // the spec either treats the current input item as a map or as a scalar
            if (subSpec instanceof Map) {                                                   // sub-spec treats this like a map. let's see if we can recurse.
                Map<String, Object> subSpecMap = (Map<String, Object>) subSpec;            // if the input item is also a map, we can proceed

                this.handleSpecialKey( subSpecMap.get( SPEC_KEY_REFERENCES_INPUT_KEY ), pathToInputItem, output, key );
                this.handleSpecialKey( subSpecMap.get( SPEC_KEY_REFERENCES_INPUT_VALUE ), pathToInputItem, output, input.get( key ) );

                if (inputItem instanceof Map) {
                    xform((Map<String, Object>) inputItem, subSpecMap, output, pathToInputItem);               // recurse to the sub input/spec
                }
            }
            else if (subSpec instanceof List) {
                for (Object subSpecElement: (List) subSpec) {
                    // TODO: what if spec is a map
                    // TODO: what if spec is a list
                    putInOutput( output, new Path( (String) subSpecElement ), input.get( key ), pathToInputItem );   // put the value in the output
                }
            }
            else if (subSpec instanceof String) {
                putInOutput( output, new Path( (String) subSpec ), input.get( key ), pathToInputItem );   // put the value in the output
            }
            // else TODO when there's a warning listener, warn here
        }
    }

    void handleSpecialKey(Object pathSpec, Path pathToInputItem, Map<String, Object> output, Object value) {
        if ((pathSpec != null) && (pathSpec instanceof String)) {             // if present and mapped to a string, we use it to place the key above it as a value in the output
            Path idOutputPath = new Path( (String) pathSpec );                // this path tells us where to put it
            Path idInputPath = new Path( pathToInputItem, SPEC_KEY_REFERENCES_INPUT_KEY );    // this path tells us where we are in the input for reference
            putInOutput(output, idOutputPath, value, idInputPath);            // put the key as a value in the output
        }
    }

    String findMatchingSpecKey(Map<String, Object> spec, String key) {
        if (spec.containsKey( key )) {                          // could be an exact match
            return key;                                         // just return it
        }
        for (String candidate: spec.keySet()) {                 // look for an enum match
            if (candidate.indexOf( "|" ) >= 0) {                // any enum will contain a '|'
                String[] splits = candidate.split( "\\|" );     // find the terms
                for (String split: splits) {                    // check each of them
                    if (key.equals( split )) {                  // and on a match
                        return candidate;                       // return the entire key
                    }
                }
            }
        }
        if (spec.containsKey( "*" )) {                          // check for a catchall match
            return "*";
        }
        return null;
    }

    void putInOutput( Map<String, Object> output, Path where, Object value, Path from) {

        // TODO defense

        // we're going to drill down into the output via the path specified in the where argument
        // current is the variable that holds our current location in the output
        Map<String, Object> current = output;               // we start at the overall output

        // drill down for each item in the path above the last
        for (int i=0; i<where.size()-1; i++) {

            // figure out key name from paths
            String keyname = where.itemAt( i, from );

            // make sure there's a map there and drill down
            // TODO handle the case where next is a list/value better
            Object next = current.get( keyname );               // grab the next value in the path
            if ((next == null) || !(next instanceof Map)) {     // we expect it to be there and a map
                next = new HashMap<String, Object>();           // make the missing map
                current.put( keyname, next );                   // put it in the output
            }
            current = (Map<String, Object>) next;               // drill down the next level
        }

        // defensive clone, in case the spec points to a map or list in the input doc
        value = JsonUtils.cloneJson( value );

        // now we're at the very bottom of our path.
        // time to insert our value
        String keyname = where.itemFromEnd( 0, from );          // the last item on the path
        Object alreadyThere = current.get( keyname );           // see if it's occupied
        if (alreadyThere == null) {                             // nothing there
            current.put( keyname, value );                      // just put the value
        }
        else if (alreadyThere instanceof List) {                // there's a list there
            ( (List) alreadyThere ).add( value );               // add the value
        }
        else {                                                  // there's a non-list there
            List toPut = new ArrayList();                       // make one to put there
            toPut.add( alreadyThere );                          // add what's already there
            toPut.add( value );                                 // add our new value
            current.put( keyname, toPut );                      // put the list in place
        }
    }

    static class Path {
        private List<String> items;

        Path() {
            this.items = new ArrayList<String>( 0 );
        }

        Path(Path other, String toAppend) {
            this.items = new ArrayList<String>( other.items.size() + 1);
            this.items.addAll( other.items );
            this.items.add( toAppend );
        }

        Path(String dotNotation) {
            String[] split = dotNotation.split( "\\." );
            this.items = Arrays.asList( split );
        }

        public String toString() {
            return this.items.toString();
        }

        String itemAt(int idx, Path reference) {
            // TODO defense
            return this.referenceIndexHelper( this.indexAt( idx ), this.itemAt( idx ), reference );
        }

        String itemFromEnd(int idx, Path reference) {
            // TODO defense
            return this.referenceIndexHelper( this.indexFromEnd( idx ), this.itemFromEnd( idx ), reference );
        }

        int size() {
            return this.items.size();
        }

        private String referenceIndexHelper(int fromIdx, String fromItem, Path reference) {

            // TODO defense

            String item = null;
            if (fromIdx >= 0) {                              // there was $$[index], let's use that index to reference the input path
                // TODO defense
                item = reference.itemFromEnd( fromIdx );     // reference is 0-major from the end of the path
            }
            else {                                           // no $$[index]
                item = fromItem;                             // just use the key supplied in the spec
            }
            return item;
        }

        private String itemAt(int idx) {
            return this.items.get( idx );
        }

        private String itemFromEnd(int idxFromEnd) {
            return this.items.get( this.items.size() - 1 - idxFromEnd );
        }

        private int indexAt(int idx) {
            return this.indexHelper( this.itemAt( idx ) );
        }

        private int indexFromEnd(int idx) {
            return this.indexHelper( this.itemFromEnd( idx ) );
        }

        private int indexHelper(String item) {
            if (item.startsWith( "$$" )) {
                return Integer.parseInt( item.substring( 2 ) );
            }
            return -1;
        }
    }
}
