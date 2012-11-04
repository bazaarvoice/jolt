package com.bazaarvoice.jolt.shiftr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reference {

    // Original Syntax   &  &1  &(1)  &1(1)    "\\&(\\d)?(\\((\\d)\\))?"
    // New Syntax        &  &1  &(1)  &(1,1)
    public static Pattern refPattern = Pattern.compile( "\\&(\\d)?(\\((\\d)(,(\\d))?\\)?)?" );

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
                String pathRefSugar = matcher.group( 1 );
                String pathRefCanonical = matcher.group( 3 );
                String keyRef = matcher.group( 5 );

                if ( pathRefSugar != null ) {
                    ref.pathIndex = Integer.parseInt( pathRefSugar );
                }
                else if ( pathRefCanonical != null ) {
                    ref.pathIndex = Integer.parseInt( pathRefCanonical );
                    if ( keyRef != null ) {
                        ref.keyGroup = Integer.parseInt( keyRef );
                    }
                }
            } else {
                throw new IllegalArgumentException( "Unable to parse reference key " + refStr );
            }
        }

        return ref;
    }

    public String getCanonicalForm() {

        String innerRef;

        if ( arrayIndex != -1 ) {
            innerRef = Integer.toString( arrayIndex );
        }
        else {
            innerRef = "&(" + pathIndex + "," + keyGroup + ")";
        }

        if ( isArray ) {
            return "[" + innerRef + "]";
        }
        return innerRef;
    }
}