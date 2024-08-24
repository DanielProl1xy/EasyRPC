package easyRPC.core;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public final class Param {

    public final static int IntSize = 4;
    public final static int DoubleSize = 8;
    public final static int LongSize = 8;

    public final int size;
    public final byte[] value;
    
    public Param(Object val)
    {
        // TODO: more types
        if(val.getClass().getSuperclass() == Number.class)
        {
            size = LongSize;            
        }
        else
        {
            size = 0;
            throw new InvalidParameterException("Invalid parametr type.");
        }

        ByteBuffer b = ByteBuffer.allocate(size);
        b.putLong((Long)val);
        value = b.array();
    }

}
