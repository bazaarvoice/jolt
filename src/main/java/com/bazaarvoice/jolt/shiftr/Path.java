package com.bazaarvoice.jolt.shiftr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bazaarvoice.jolt.shiftr.PathElement.*;

public class Path<T> {

    protected List<T> elements = Collections.<T>emptyList();

    public Path() {};

    public Path(List<T> elements) {
        this.elements = elements;
    }

    public Path( Path other, T toAppend ) {
        this.elements = new ArrayList<T>( other.elements.size() + 1 );
        this.elements.addAll( other.elements );
        this.elements.add( toAppend );
    }

    public Path( T toPrepend, Path other ) {
        this.elements = new ArrayList<T>( other.elements.size() + 1 );
        this.elements.add( toPrepend );
        this.elements.addAll( other.elements );
    }

    public String toString() {
        return this.elements.toString();
    }

    public int size() {
        return this.elements.size();
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

    public static class LiteralPath extends Path<LiteralPathElement> {
        public LiteralPath(Path other, LiteralPathElement toAppend) {
            super(other, toAppend);
        }

        public LiteralPath() {
            super();
        }
    }

    public static class StringPath extends Path<String> {
        public StringPath(List<String> elements) {
            super(elements);
        }
    }

    public static class OutputPath extends Path<PathElement> {

        public OutputPath(List<PathElement> elements) {
            super(elements);
        }

        public static OutputPath parseDotNotation( String dotNotation ) {
            return new OutputPath( PathElement.parseDotNotation( dotNotation ) );
        }

        public StringPath build( LiteralPath walkedPath ) {

            List<String> strings = new ArrayList<String>();
            for ( PathElement pathElement : elements ) {
                String evaledLeafOutput = pathElement.evaluateAsOutputKey( walkedPath );
                strings.add( evaledLeafOutput );
            }

            return new StringPath( strings );
        }

    }
}
