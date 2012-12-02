package com.bazaarvoice.jolt.shiftr.pathelement;

import com.bazaarvoice.jolt.shiftr.WalkedPath;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Non-greedy * based Path Element.
 */
public class StarPathElement extends PathElement {

    private final Pattern pattern;

    public StarPathElement( String key ) {
        super(key);

        pattern = makePattern( key );
    }

    // Visible for testing
    public static Pattern makePattern( String key ) {

        // "rating-*-*"  ->  "^rating-(.*?)-(.*?)$"
        String regex = "^" + key.replace("*", "(.*?)")  + "$";

        return Pattern.compile( regex );
    }

    public String evaluate( WalkedPath walkedPath ) {
        throw new UnsupportedOperationException("Don't call evaluate on the '*'");
    }

    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {

        Matcher matcher = pattern.matcher( dataKey );
        if ( ! matcher.find() ) {
            return null;
        }

        int groupCount = matcher.groupCount();
        List<String> subKeys = new ArrayList<String>(groupCount);
        for ( int index = 1; index <= groupCount; index++) {
            subKeys.add( matcher.group( index ) );
        }

        return new LiteralPathElement(dataKey, subKeys);
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }
}
