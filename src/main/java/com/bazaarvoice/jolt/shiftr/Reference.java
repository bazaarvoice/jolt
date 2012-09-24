package com.bazaarvoice.jolt.shiftr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reference {

    public static Pattern refPattern = Pattern.compile( "\\&(\\d)?(\\((\\d)\\))?" );

    boolean isArray = false;
    int arrayIndex = -1;
    int pathIndex = 0;
    int keyGroup = 0;

    public static Reference newReference( boolean isArray, String refStr ) {
        Reference ref = new Reference();
        ref.isArray = isArray;

        if ( isArray && refStr.charAt(0) != '&' ) {
            ref.arrayIndex = Integer.parseInt( refStr.substring(1) );
        }
        else{

            Matcher matcher = refPattern.matcher( refStr );

            if ( matcher.find() ) {
                String pathRef = matcher.group( 1 );
                String keyRef = matcher.group( 3 );

                if ( pathRef != null ) {
                    ref.pathIndex = Integer.parseInt( pathRef );
                }
                if ( keyRef != null ) {
                    ref.keyGroup = Integer.parseInt( keyRef );
                }
            } else {
                throw new IllegalArgumentException( "Unable to parse reference key " + refStr );
            }
        }

        return ref;
    }
}