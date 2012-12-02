package com.bazaarvoice.jolt.shiftr.reference;

import com.bazaarvoice.jolt.exception.SpecException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses the Jolt & syntax into useful programmatic constructs.
 *
 * Valid Syntax is :  &   &1   &(1)   &(1,1)
 */
// Todo extract base class for Amp and Dollar Reference
public class AmpReference implements Reference {

    // VisibleForTesting
    static final Pattern refPattern = Pattern.compile( "\\&(\\d)?(\\((\\d)(,(\\d))?\\)?)?" );

    // these members track the "&" reference values if they exist
    private final int pathIndex;
    private final int keyGroup;

    public AmpReference( String refStr ) {

        int pI = 0;
        int kG = 0;

        Matcher matcher = refPattern.matcher( refStr );

        if ( matcher.find() ) {

            try {
                String pathRefSugar = matcher.group( 1 );
                String pathRefCanonical = matcher.group( 3 );
                String keyRef = matcher.group( 5 );

                if ( pathRefSugar != null ) {
                    pI = Integer.parseInt( pathRefSugar );
                }
                else if ( pathRefCanonical != null ) {
                    pI = Integer.parseInt( pathRefCanonical );
                    if ( keyRef != null ) {
                        kG = Integer.parseInt( keyRef );
                    }
                }
            }
            catch( NumberFormatException nfe ) {
                throw new SpecException( "Unsable to parse '&' reference key:" + refStr, nfe );
            }
        } else {
            throw new SpecException( "Unable to parse reference key:" + refStr + " as it did not match the '&', '&0', or '&(0,0)' pattern." );
        }

        pathIndex = pI;
        keyGroup = kG;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public int getKeyGroup() {
        return keyGroup;
    }

    /**
     * Builds the non-syntactic sugar / maximally expanded and unique form of this reference.
     * @return canonical form : aka "&" -> "&(0,0)
     */
    public String getCanonicalForm() {
        return "&(" + pathIndex + "," + keyGroup + ")";
    }
}