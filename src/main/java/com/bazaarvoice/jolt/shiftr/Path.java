package com.bazaarvoice.jolt.shiftr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bazaarvoice.jolt.shiftr.PathElement.*;
import org.apache.commons.lang.StringUtils;

/**
 * Base generic Path.
 */
public class Path<T> {

    protected List<T> elements = Collections.<T>emptyList();

    public Path() {};

    public Path(List<T> elements) {
        this.elements = elements;
    }

    protected List<T> appendList( T toAppend ) {
        List<T> newElements = new ArrayList<T>( elements );
        newElements.add( toAppend );
        return newElements;
    }

    protected List<T> prependList( T toPrepend ) {
        List<T> newElements = new ArrayList<T>( elements.size() + 1 );
        newElements.add( toPrepend );
        newElements.addAll( elements );
        return newElements;
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


    /**
     * Convenience class for a Path of LiteralPathElement
     */
    public static class LiteralPath extends Path<LiteralPathElement> {
        public LiteralPath() {
            super();
        }

        private LiteralPath( List<LiteralPathElement> elements ) {
            super(elements);
        }

        public LiteralPath append( LiteralPathElement toAppend ) {
            return new LiteralPath( this.appendList( toAppend ) );
        }
    }

    /**
     * Convenience class for a String based Paths
     */
    public static class StringPath extends Path<String> {
        public StringPath(List<String> elements) {
            super(elements);
        }

        public StringPath prepend( String toPrepend ) {
            return new StringPath( this.prependList( toPrepend ) );
        }
    }

    /**
     * Convenience class for path based off a single dot notation String,
     *  like "rating.&1(2).&.value".
     */
    public static class DotNotationPath extends Path<PathElement> {

        private DotNotationPath( List<PathElement> elements ) {
            super(elements);
        }

        public static DotNotationPath parseDotNotation( String dotNotation ) {

            if ( dotNotation.contains("@") || dotNotation.contains("*") || dotNotation.contains("$")) {
                throw new IllegalArgumentException("DotNotation (output key) can not contain '@', '*', or '$'.");
            }

            if ( StringUtils.isNotBlank( dotNotation ) ) {
                String[] split = dotNotation.split( "\\." );
                return new DotNotationPath( PathElement.parse( split ) );
            }

            return new DotNotationPath( new ArrayList<PathElement>() );
        }

        /**
         * Use the supplied LiteralPath, to evaluate each PathElement in this Path
         * @param walkedPath reference LiteralPath used to lookup reference values like "&1(2)"
         * @return StringPath fully evaluated Strings, possibly with concrete array references like "photos[3]"
         */
        public StringPath evaluate( LiteralPath walkedPath ) {

            List<String> strings = new ArrayList<String>();
            for ( PathElement pathElement : elements ) {
                String evaledLeafOutput = pathElement.evaluate( walkedPath );
                strings.add( evaledLeafOutput );
            }

            return new StringPath( strings );
        }
    }
}
