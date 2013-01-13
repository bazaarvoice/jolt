package com.bazaarvoice.jolt.shiftr.pathelement;

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.reference.AmpReference;
import com.bazaarvoice.jolt.shiftr.WalkedPath;

public class ArrayPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    public enum ArrayPathType { AUTO_EXPAND, REFERENCE, EXPLICIT_INDEX }

    private final ArrayPathType arrayPathType;
    private final AmpReference ref;

    private final String canonicalForm;
    private final String arrayIndex;

    public ArrayPathElement( String key ) {
        super(key);

        if ( key.charAt( 0 ) != '[' || key.charAt( key.length() - 1 ) != ']') {
            throw new SpecException( "Invalid ArrayPathElement key:" + key );
        }

        ArrayPathType apt;
        AmpReference r = null;
        String aI = "";

        if ( key.length() == 2 ) {
            apt = ArrayPathType.AUTO_EXPAND;
            canonicalForm = "[]";
        }
        else {
            String meat = key.substring( 1, key.length() - 1 );  // trim the [ ]

            if ( meat.contains( "&" ) ) {
                r = new AmpReference( meat );
                apt = ArrayPathType.REFERENCE;

                canonicalForm = "[" + r.getCanonicalForm() + "]";
            }
            else {
                try {
                    Integer.parseInt( meat );
                    apt = ArrayPathType.EXPLICIT_INDEX;

                    canonicalForm = "[" + meat + "]";
                    aI = meat;
                }
                catch ( NumberFormatException nfe ){
                    throw new SpecException( "Unable to parse explicit array index of:" + meat + " from key:" + key );
                }
            }
        }

        arrayPathType = apt;
        ref = r;
        arrayIndex = aI;
    }


    @Override
    public String getCanonicalForm() {
        return canonicalForm;
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {

        switch ( arrayPathType ) {
            case AUTO_EXPAND:
                return canonicalForm;

            case EXPLICIT_INDEX:
                return arrayIndex;

            case REFERENCE:
                LiteralPathElement lpe = walkedPath.elementFromEnd( ref.getPathIndex() );
                String keyPart = lpe.getSubKeyRef( ref.getKeyGroup() );
                try
                {
                    Integer.parseInt( keyPart );
                    return keyPart;
                }
                catch ( NumberFormatException nfe ) {
                    throw new RuntimeException( " Evaluating canonical ReferencePathElement:" + this.getCanonicalForm() + ", and got a non integer result for reference:" + ref.getCanonicalForm() );
                }
            default:
                throw new IllegalStateException( "ArrayPathType enum added two without updating this switch statement." );
        }
    }

    @Override
    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        String evaled = evaluate( walkedPath );
        if ( evaled.equals( dataKey ) ) {
            return new LiteralPathElement( evaled );
        }
        return null;
    }
}
