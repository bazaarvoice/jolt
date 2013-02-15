package com.bazaarvoice.jolt.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DeepCopy {

    /**
     * Simple deep copy, that leverages Java Serialization.
     * Supplied object is serialized to an in memory buffer (byte array),
     *  and then a new object is reconstituted from that byte array.
     *
     * This is meant for copying small objects or object graphs, and will
     *  probably do nasty things if asked to copy a large graph.
     *
     * @param object object to deep copy
     * @return deep copy of the object
     */
    public static Object simpleDeepCopy( Object object ) {

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            bos.close();

            byte [] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);

            return new ObjectInputStream(bais).readObject();
        }
        catch ( IOException ioe ) {
            throw new RuntimeException( "DeepCopy IOException", ioe );
        }
        catch ( ClassNotFoundException cnf ) {
            throw new RuntimeException( "DeepCopy ClassNotFoundException", cnf );
        }
    }
}
