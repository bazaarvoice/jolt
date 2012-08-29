package com.bazaarvoice.jolt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Shifter is a kind of JOLT transform that specifies where in some output JSON to put data
 * from an input JSON. Instances of this class execute Shiftr transformations given a JSON
 * input and transform spec both in Jackson-style maps of maps.
 *
 * Each entry in a Shiftr transform says where to put some data in an output object, if encountered
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
 *         "value": "SecondaryRatings.&1.Value",       // rating.[*-match].value from the input goes to output.SecondaryRatings.[*-match].Value
 *         "max": "SecondaryRatings.&1.Range",         // rating.[*-match].max from the input goes to output.SecondaryRatings.[*-match].Range
 *         "&": "SecondaryRatings.&1.Id"               // [*-match] goes to output.SecondaryRatings.[*-match].Id
 *     }
 *   }
 * }
 * </pre>
 *
 * Shiftr walks the input document, looking up each node in the transform spec and
 * making the specified updates in the output document. This is best explained with an example input file:
 *
 * <pre>
 * {                            // no rules for all input
 *   "rating": {                // no rules for rating
 *       "primary": {           // no rules for rating.primary
 *           "value": 3,        // 3 (rating.primary.value) goes to output.Rating
 *           "max": 5           // 5 (rating.primary.max) goes to output.RatingRange
 *       },
 *       "quality": {           // "quality" (rating.*.&) goes to output.SecondaryRatings.quality.Id
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
 * - "&[index]" within a path is a zero-major reference to the keys in the input document starting with current. thus &0 evaluates to the key
 *   for the current attribute, &1 evaluates to the key for the parent attribute, and so on.
 */
public class Shiftr implements Chainable {

    private static final String SPEC_KEY_REFERENCES_INPUT_KEY = "&";
    private static final String SPEC_KEY_REFERENCES_INPUT_VALUE = "@";

    private static final String OUTPUT_PATH_PREFIX = "output";

    // TODO construction option that takes a warning/info listener

    // TODO support for lists in mappings

    /**
     * Applies a Shiftr transform for Chainr
     *
     * @param input the JSON object to transform
     * @param operationEntry the JSON object from the Chainr spec containing
     *  the rest of the details necessary to carry out the transform (specifically,
     *  in this case, a shiftr spec)
     * @return the output object with data shifted to it
     * @throws JoltException for a malformed spec or if there are issues during
     * the transform
     */
    @Override
    public Object process( Object input, Map<String, Object> operationEntry ) throws JoltException {
        Object spec = operationEntry.get( "spec" );
        if (spec == null) {
            throw new JoltException( "JOLT Shiftr expected a spec in its operation entry, but instead got: "+operationEntry.toString() );
        }
        return this.xform( input, spec );
    }

    public Object xform( Object input, Object spec ) {

        // TODO defense

        Map output = new HashMap();
        this.applySpec( input, spec, output, new Path() );
        return output.get( OUTPUT_PATH_PREFIX );
    }


    private void applySpec(Object input, Object spec, Map<String,Object> output, Path inputPath) {

        // TODO defense

        if (spec instanceof Map) {
            this.applyMapSpec( input, (Map<String, Object>) spec, output, inputPath );
        }
        else if (spec instanceof List) {
            this.applyListSpec( input, (List) spec, output, inputPath );
        }
        else if (spec instanceof String) {
            this.applyStringSpec( input, (String) spec, output, inputPath );
        }
        // TODO else warn
    }



    private void applyMapSpec(Object input, Map<String, Object> spec, Map<String,Object> output, Path inputPath) {
        // 1) apply special keys
        this.handleSpecialKey( spec.get( SPEC_KEY_REFERENCES_INPUT_KEY ), inputPath, output, inputPath.itemFromEnd( 0 ) );
        this.handleSpecialKey( spec.get( SPEC_KEY_REFERENCES_INPUT_VALUE ), inputPath, output, input );

        // 2) do input type specific stuff
        if (input instanceof  Map) {
            Map<String, Object> inputMap = (Map<String, Object>) input;
            for (String key: inputMap.keySet()) {
                Object subInput = inputMap.get( key );
                this.applySubSpec( spec, key, inputPath, subInput, output );
            }
        }
        else if (input instanceof List) {
            List inputList = (List) input;
            for (int i=0; i<inputList.size(); i++) {
                Object subInput = inputList.get( i );
                String key = Integer.toString( i );
                this.applySubSpec( spec, key, inputPath, subInput, output );
            }
        }
    }

    private void applySubSpec(Map<String, Object> spec, String key, Path inputPath, Object subInput, Map<String,Object> output) {
        String specKey = this.findMatchingSpecKey( spec, key );
        if (specKey != null) {
            Object subSpec = spec.get( specKey );
            Path subInputPath = new Path( inputPath, key );
            this.applySpec( subInput, subSpec, output, subInputPath );
        }
    }

    private String findMatchingSpecKey(Map<String, Object> spec, String key) {
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

    private void handleSpecialKey(Object pathSpec, Path pathToInputItem, Map<String, Object> output, Object value) {
        if ((pathSpec != null) && (pathSpec instanceof String)) {             // if present and mapped to a string, we use it to place the key above it as a value in the output
            Path idOutputPath = new Path( (String) pathSpec );                // this path tells us where to put it
            Path idInputPath = new Path( pathToInputItem, SPEC_KEY_REFERENCES_INPUT_KEY );    // this path tells us where we are in the input for reference
            putInOutput(output, idOutputPath, value, idInputPath);            // put the key as a value in the output
        }
    }

    private void applyListSpec(Object input, List spec, Map<String,Object> output, Path inputPath) {
        for (Object subSpec: spec) {
            this.applySpec( input, subSpec, output, inputPath );
        }
    }

    private void applyStringSpec(Object input, String spec, Map<String,Object> output, Path inputPath) {
        String specStr = spec.toString();
        Path specPath = new Path( specStr );
        this.putInOutput( output, specPath, input, inputPath );
    }

    private void putInOutput( Map<String, Object> output, Path where, Object value, Path from) {

        // TODO defense

        where = new Path( OUTPUT_PATH_PREFIX, where );

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

        Path(String toPrepend, Path other) {
            this.items = new ArrayList<String>( other.items.size() + 1);
            this.items.add( toPrepend );
            this.items.addAll( other.items );
        }

        Path(String dotNotation) {
            if ((dotNotation == null) || ("".equals( dotNotation ))) {   // TODO blank?
                this.items = new ArrayList<String>( 0 );
            }
            else {
                String[] split = dotNotation.split( "\\." );
                this.items = Arrays.asList( split );
            }
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
            if (fromIdx >= 0) {                              // there was &[index], let's use that index to reference the input path
                // TODO defense
                item = reference.itemFromEnd( fromIdx );     // reference is 0-major from the end of the path
            }
            else {                                           // no &[index]
                item = fromItem;                             // just use the key supplied in the spec
            }
            return item;
        }

        private String itemAt(int idx) {
            return this.items.get( idx );
        }

        private String itemFromEnd(int idxFromEnd) {
            if (this.items.isEmpty()) {
                return null;
            }
            return this.items.get( this.items.size() - 1 - idxFromEnd );
        }

        private int indexAt(int idx) {
            return this.indexHelper( this.itemAt( idx ) );
        }

        private int indexFromEnd(int idx) {
            return this.indexHelper( this.itemFromEnd( idx ) );
        }

        private int indexHelper(String item) {
            if (item.startsWith( "&" )) {
                String indexStr = item.substring( 1 );
                if ("".equals( indexStr )) {
                    return 0;
                }
                return Integer.parseInt( indexStr );
            }
            return -1;
        }
    }
}