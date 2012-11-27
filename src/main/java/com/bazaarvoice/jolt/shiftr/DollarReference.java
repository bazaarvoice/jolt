package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.exception.SpecException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DollarReference {

    // Syntax     $  $0  $(0)  $(0,0)
    public static Pattern refPattern = Pattern.compile( "\\$(\\d)?(\\((\\d)(,(\\d))?\\)?)?" );

    int pathIndex = 0;
    int keyGroup = 0;

    public static DollarReference newReference( String refStr ) {
        DollarReference ref = new DollarReference();

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
            throw new SpecException( "Unable to parse reference key " + refStr );
        }

        return ref;
    }

    public String getCanonicalForm() {
        return "$(" + pathIndex + "," + keyGroup + ")";
    }
}