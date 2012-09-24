package com.bazaarvoice.jolt.shiftr;

import java.util.ArrayList;
import java.util.List;

public class Path<T> {

    private List<T> elements;

    public Path(List<T> elements) {
        this.elements = elements;
    }

    Path( Path other, T toAppend ) {
        this.elements = new ArrayList<T>( other.elements.size() + 1 );
        this.elements.addAll( other.elements );
        this.elements.add( toAppend );
    }

    Path( T toPrepend, Path other ) {
        this.elements = new ArrayList<T>( other.elements.size() + 1 );
        this.elements.add( toPrepend );
        this.elements.addAll( other.elements );
    }

    public static Path<PathElement> parseDotNotation( String dotNotation ) {
        return new Path<PathElement>( PathElement.parseDotNotation( dotNotation ) );
    }


    public String toString() {
        return this.elements.toString();
    }

//    public PathElement elementAt( int idx, Path reference ) {
//        // TODO defense
//        return this.referenceIndexHelper( this.indexAt( idx ), this.elementAt( idx ), reference );
//    }
//
//    public PathElement elementFromEnd( int idx, Path reference ) {
//        // TODO defense
//        return this.referenceIndexHelper( this.indexFromEnd( idx ), this.elementFromEnd( idx ), reference );
//    }

    public int size() {
        return this.elements.size();
    }

    private String referenceIndexHelper( int fromIdx, String fromItem, Path reference ) {

        // TODO defense

        String item = null;
//        if ( fromIdx >= 0 ) {                              // there was &[index], let's use that index to reference the input path
//            // TODO defense
//            item = reference.elementFromEnd( fromIdx );     // reference is 0-major from the end of the path
//        } else {                                           // no &[index]
//            item = fromItem;                             // just use the key supplied in the spec
//        }
        return item;
    }

    public T elementAt( int idx ) {
        return this.elements.get( idx );
    }

    public T elementFromEnd( int idxFromEnd ) {
        if ( this.elements.isEmpty() ) {
            return null;
        }
        return this.elements.get( this.elements.size() - 1 - idxFromEnd );
    }

    public T lastElement() {
        return this.elements.get( this.elements.size() -1 );
    }

//    private int indexAt( int idx ) {
//        return this.indexHelper( this.elementAt( idx ) );
//    }
//
//    private int indexFromEnd( int idx ) {
//        return this.indexHelper( this.elementFromEnd( idx ) );
//    }

    private int indexHelper( String item ) {
        if ( item.startsWith( "&" ) ) {
            String indexStr = item.substring( 1 );
            if ( "".equals( indexStr ) ) {
                return 0;
            }
            return Integer.parseInt( indexStr );
        }
        return -1;
    }
}
