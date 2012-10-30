package com.bazaarvoice.jolt.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DeepCopy {

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

        }
        catch ( ClassNotFoundException cnf ) {

        }
        return null;
    }
}
