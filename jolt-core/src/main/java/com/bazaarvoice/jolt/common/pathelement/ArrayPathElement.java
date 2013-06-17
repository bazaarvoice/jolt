package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.reference.AmpReference;
import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.common.reference.HashReference;
import com.bazaarvoice.jolt.common.reference.PathAndGroupReference;
import com.bazaarvoice.jolt.common.reference.PathReference;

public class ArrayPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    public enum ArrayPathType { AUTO_EXPAND, REFERENCE, HASH, EXPLICIT_INDEX }

    private final ArrayPathType arrayPathType;
    private final PathReference ref;

    private final String canonicalForm;
    private final String arrayIndex;

    public ArrayPathElement( String key ) {
        super(key);

        if ( key.charAt( 0 ) != '[' || key.charAt( key.length() - 1 ) != ']') {
            throw new SpecException( "Invalid ArrayPathElement key:" + key );
        }

        ArrayPathType apt;
        PathReference r = null;
        String aI = "";

        if ( key.length() == 2 ) {
            apt = ArrayPathType.AUTO_EXPAND;
            canonicalForm = "[]";
        }
        else {
            String meat = key.substring( 1, key.length() - 1 );  // trim the [ ]

            if ( AmpReference.TOKEN.equals( meat.charAt( 0 ) ) ) {
                r = new AmpReference( meat );
                apt = ArrayPathType.REFERENCE;

                canonicalForm = "[" + r.getCanonicalForm() + "]";
            }
            else if ( HashReference.TOKEN.equals( meat.charAt( 0 ) ) ) {
                r = new HashReference( meat );
                apt = ArrayPathType.HASH;

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

            case HASH:
                LiteralPathElement element = walkedPath.elementFromEnd( ref.getPathIndex() );
                Integer index = element.getHashCount();
                return index.toString();

            case REFERENCE:
                LiteralPathElement lpe = walkedPath.elementFromEnd( ref.getPathIndex() );
                String keyPart;

                if ( ref instanceof PathAndGroupReference ) {
                    keyPart = lpe.getSubKeyRef( ( (PathAndGroupReference) ref).getKeyGroup() );
                }
                else {
                    keyPart = lpe.getSubKeyRef( 0 );
                }
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
