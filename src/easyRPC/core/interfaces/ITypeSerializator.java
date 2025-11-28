package easyRPC.core.interfaces;

import easyRPC.core.internal.Param.ParamType;

public interface ITypeSerializator {

    public byte[] Serlialize(Object tobj);
    public Object Deserialize(final byte[] data, final ParamType type, final int size);
}
