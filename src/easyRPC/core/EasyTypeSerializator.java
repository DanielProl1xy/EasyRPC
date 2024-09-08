package easyRPC.core;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

import easyRPC.core.interfaces.ITypeSerializator;
import easyRPC.core.internal.Param;
import easyRPC.core.internal.Param.ParamType;

public class EasyTypeSerializator implements ITypeSerializator {

    @Override
    public byte[] Serlialize(Object tobj) 
    {
        ParamType type;
        int size;
         if (tobj instanceof Integer) {
            size = Param.IntSize;
            type = ParamType.INT;
        } else if (tobj instanceof Float) {
            size = Param.FloatSize;
            type = ParamType.FLOAT;
        } else if (tobj instanceof Double) {
            size = Param.DoubleSize;
            type = ParamType.DOUBLE;
        } else if (tobj instanceof Long) {
            size = Param.LongSize;
            type = ParamType.LONG;
        } else if (tobj instanceof Character) {
            size = Param.CharSize;
            type = ParamType.CHAR;
        } else if (tobj instanceof Short) {
            size = Param.ShortSize;
            type = ParamType.SHORT;
        } else if (tobj instanceof Boolean) {
            size = Param.CharSize;
            type = ParamType.BOOL;
        } else if (tobj instanceof String) {
            String s = (String)tobj;
            size = s.length();
            type = ParamType.STRING;
        } else {
            size = 0;
            type = ParamType.INVALID;
            throw new InvalidParameterException("Invalid argument Type");
        }

        ByteBuffer buff = ByteBuffer.allocate(size + Param.IntSize + Param.CharSize);
        buff.putChar((char)type.ordinal());
        buff.putInt(size);
        switch(type)
        {
            case INT:
                buff.putInt((Integer)tobj);
            break;
            case FLOAT:
                buff.putFloat((Float)tobj);
            break;
            case DOUBLE:
                buff.putDouble((Double)tobj);
            break;
            case LONG:
                buff.putLong((Long)tobj);
            break;
            case CHAR:
                buff.putChar((Character)tobj);
            break;
            case SHORT:
                buff.putShort((Short)tobj);
            break;
            case BOOL:
                buff.putChar((char)((Boolean) tobj ? 1 : 0));
            break;
            case STRING:
                buff.put(((String)tobj).getBytes());
            break;
            case INVALID:
                throw new InvalidParameterException("Invalid argument Type");
        }
        return buff.array();
    }

    @Override
    public Object Deserialize(final byte[] data, ParamType type) 
    {
        ByteBuffer buff = ByteBuffer.wrap(data);
        Object val = null;

        switch (type) {
            case INT:
                val = buff.getInt();
            break;
            case FLOAT:
                val = buff.getFloat();
            break;
            case DOUBLE:
                val = buff.getDouble();
            break;
            case LONG:
                val = buff.getLong();
            break;
            case CHAR:
                val = buff.getChar();
            break;
            case SHORT:
                val = buff.getShort();
            break;
            case BOOL:
                val = (buff.getChar() != 0);
            break;
            case STRING:
                val = new String(data);
            break;
            default:
                val = null;
            break;
        }
        return val;
    }
    
}
