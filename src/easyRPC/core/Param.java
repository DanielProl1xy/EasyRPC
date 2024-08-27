package easyRPC.core;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public final class Param {

    public enum ParamType
    {
        INT,
        FLOAT,
        DOUBLE,
        LONG,
        CHAR,
        SHORT,
        STRING,
        INVALID
    }

    public final static int IntSize = 4;
    public final static int DoubleSize = 8;
    public final static int LongSize = 8;
    public final static int FloatSize = 4;
    public final static int CharSize = 1;
    public final static int ShortSize = 2;

    public final int Size;
    public final byte[] Bytes;
    public final Object Value;
    public final ParamType Type;

    public Param(final ParamType ftype, final int fsize,final byte[] from)
    {
        ByteBuffer b = ByteBuffer.wrap(from);
        Size = fsize;
        Type = ftype;
        Bytes = from;
    
        switch (Type) {
            case INT:
                Value = b.getInt();
                break;
            case FLOAT:
                Value = b.getFloat();
                break;
            case DOUBLE:
                Value = b.getDouble();
                break;
            case LONG:
                Value = b.getLong();
                break;
            case CHAR:
                Value = b.getChar();
                break;
            case SHORT:
                Value = b.getShort();
                break;
            case STRING:
                Value = new String(from);
                break;
            default:
                Value = null;
            break;
        }
    }
    
    public Param(Object val)
    {
        if (val instanceof Integer) {
            Size = LongSize;
            Type = ParamType.INT;
        } else if (val instanceof Float) {
            Size = FloatSize;
            Type = ParamType.FLOAT;
        } else if (val instanceof Double) {
            Size = DoubleSize;
            Type = ParamType.DOUBLE;
        } else if (val instanceof Long) {
            Size = LongSize;
            Type = ParamType.LONG;
        } else if (val instanceof Character) {
            Size = CharSize;
            Type = ParamType.CHAR;
        } else if (val instanceof Short) {
            Size = ShortSize;
            Type = ParamType.SHORT;
        } else if (val instanceof String) {
            String s = (String)val;
            Size = s.length();
            Type = ParamType.STRING;
        } else {
            Size = 0;
            Type = ParamType.INVALID;
            throw new InvalidParameterException("Invalid argument Type");
        }

        Value = val;

        ByteBuffer b = ByteBuffer.allocate(Size + 6);
        b.putChar((char)Type.ordinal());
        b.putInt(Size);
        switch(Type)
        {
            case INT:
                b.putInt((Integer)val);
            break;
            case FLOAT:
                b.putFloat((Float)val);
            break;
            case DOUBLE:
                b.putDouble((Double)val);
            break;
            case LONG:
                b.putLong((Long)val);
            break;
            case CHAR:
                b.putChar((Character)val);
            break;
            case SHORT:
                b.putShort((Short)val);
            break;
            case STRING:
                b.put(((String)val).getBytes());
            break;
            case INVALID:
                throw new InvalidParameterException("Invalid argument Type");
        }
        Bytes = b.array();
    }
}
