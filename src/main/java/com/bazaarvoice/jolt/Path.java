package com.bazaarvoice.jolt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Path {

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
            this.items = Arrays.asList(split);
        }
    }

    public String toString() {
        return this.items.toString();
    }

    String itemFromEnd(int idxFromEnd) {
        if (this.items.isEmpty()) {
            return null;
        }
        return this.items.get( this.items.size() - 1 - idxFromEnd );
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
