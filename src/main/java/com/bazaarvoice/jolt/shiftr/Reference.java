package com.bazaarvoice.jolt.shiftr;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses the & and Array syntax into useful programmatic constructs.
 *
 * Valid Syntax is :
 *
 *   Reference Syntax           &   &1   &(1)   &(1,1)
 *   Array Wrapped References  [&] [&1] [&(1)] [&(1,1)]
 *   Array only                 []
 */
public class Reference {

    @VisibleForTesting
    public static Pattern refPattern = Pattern.compile( "\\&(\\d)?(\\((\\d)(,(\\d))?\\)?)?" );

    public enum ReferenceType {
        NOT_ARRAY, // Not an array; just an '&' ref
        LITERAL_ARRAY_VALUE, // Array with literal array index value : 0 to infinity; no '&' ref
        AUTO_EXPAND_ARRAY, // Array; no '&'
                           // means we were told "[]" which means, just make sure there is an array there, and everytime it is accessed, just add to the array
        CONTAINS_REF // Array and contains an '&' reference : &  &1  &(1)  &(1,1)
    }

    private final ReferenceType referenceType;
    private final int arrayIndex;

    // these members track the "&" reference values if they exist
    private final int pathIndex;
    private final int keyGroup;

    public Reference( boolean isArray, String refStr ) {

        ReferenceType aT;
        int aI = -1;
        int pI = 0;
        int kG = 0;

        if ( isArray && StringUtils.isBlank( refStr ) ) {
            aT = ReferenceType.AUTO_EXPAND_ARRAY;
        }
        else if ( isArray && StringUtils.isNotBlank( refStr ) && refStr.charAt(0) != '&' ) {
            aT = ReferenceType.LITERAL_ARRAY_VALUE;
            aI = Integer.parseInt( refStr );
        }
        else{

            if ( isArray ) {
                aT = ReferenceType.CONTAINS_REF;
            }
            else {
                aT = ReferenceType.NOT_ARRAY;
            }

            Matcher matcher = refPattern.matcher( refStr );

            if ( matcher.find() ) {
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
            } else {
                throw new IllegalArgumentException( "Unable to parse reference key " + refStr );
            }
        }

        referenceType = aT;
        arrayIndex = aI;
        pathIndex = pI;
        keyGroup = kG;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public int getKeyGroup() {
        return keyGroup;
    }

    public boolean isArray() {
        return referenceType != ReferenceType.NOT_ARRAY;
    }

    /**
     * Builds the non-syntactic sugar / maximally expanded and unique form of this reference.
     * @return canonical form : aka "&" -> "&(0,0)
     */
    public String getCanonicalForm() {

        String innerRef;

        switch( referenceType ) {
            case LITERAL_ARRAY_VALUE:
                innerRef = Integer.toString( arrayIndex );
                break;
            case CONTAINS_REF:
            case NOT_ARRAY:
                innerRef = "&(" + pathIndex + "," + keyGroup + ")";
                break;
            case AUTO_EXPAND_ARRAY:
                innerRef = "";
                break;
            default :
                throw new IllegalStateException( "Reference.ReferenceType enum was expanded, without updating this logic." );
                // break;  // no break here as it is unreachable
        }

        if ( referenceType != ReferenceType.NOT_ARRAY ) {
            return "[" + innerRef + "]";
        }
        return innerRef;
    }



}