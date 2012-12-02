package com.bazaarvoice.jolt.shiftr.reference;

import com.bazaarvoice.jolt.exception.SpecException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Todo extract base class for Amp and Dollar Reference
public class DollarReference implements Reference {

    // Visible for testing
    // Syntax     $  $0  $(0)  $(0,0)
    static final Pattern refPattern = Pattern.compile( "\\$(\\d)?(\\((\\d)(,(\\d))?\\)?)?" );

    private final int pathIndex;
    private final int keyGroup;

    public DollarReference( String refStr ) {

        Matcher matcher = refPattern.matcher( refStr );

        int pIndex = 0;
        int kGroup = 0;

        if ( matcher.find() ) {

            try {

                String pathRefSugar = matcher.group( 1 );
                String pathRefCanonical = matcher.group( 3 );
                String keyRef = matcher.group( 5 );


                if ( pathRefSugar != null ) {
                    pIndex = Integer.parseInt( pathRefSugar );
                }
                else if ( pathRefCanonical != null ) {
                    pIndex = Integer.parseInt( pathRefCanonical );
                    if ( keyRef != null ) {
                        kGroup = Integer.parseInt( keyRef );
                    }
                }
            }
            catch( NumberFormatException nfe ) {
                throw new SpecException( "Unsable to parse '$' reference key:" + refStr, nfe );
            }
        }
        else {
            throw new SpecException( "Unable to parse reference key:" + refStr + " as it did not match the '$', '$0', or '$(0,0)' pattern." );
        }

        pathIndex = pIndex;
        keyGroup = kGroup;
    }

    public String getCanonicalForm() {
        return "$(" + pathIndex + "," + keyGroup + ")";
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public int getKeyGroup() {
        return keyGroup;
    }
}