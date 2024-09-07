package easyRPC.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import easyRPC.EasyRPC;
import easyRPC.EasyRPC.CallKind;
import easyRPC.core.interfaces.ICallSerializator;
import easyRPC.core.interfaces.ITypeSerializator;
import easyRPC.core.internal.CallData;
import easyRPC.core.internal.Param;
import easyRPC.core.internal.Param.ParamType;

public class EasySerializator implements ICallSerializator {

    public final static int HeadSize = Param.IntSize*2 + Param.DoubleSize;

    public final ITypeSerializator typeSerializtor;

    public EasySerializator()
    {
        typeSerializtor = new EasyTypeSerializator();
    }

    @Override
    public byte[] serializeCallData(final CallData call) {
        int argsSize = 0;
        List<byte[]> params = new ArrayList<>();
        for (Object arg : call.Args) {
            final byte[] paramData = typeSerializtor.Serlialize(arg);
            params.add(paramData);
            argsSize += paramData.length;
        }

        int size = call.Name.length() + Param.IntSize*4 + Param.DoubleSize + argsSize;
        ByteBuffer buff = ByteBuffer.allocate(size);

        buff.putInt(EasyRPC.ProtocolMagic);
        buff.putInt(size - EasyRPC.ProtocolSize); // body size
        buff.putDouble(EasyRPC.Version);
        
        buff.putInt(call.CallKind.ordinal());
        buff.putInt(call.Name.length());
        buff.put(call.Name.getBytes());

        for (byte[] bs : params) {
            buff.put(bs);
        }

        return buff.array();
    }

    @Override
    public CallData deserializeCallData(byte[] data)
    {
        ByteBuffer buff = ByteBuffer.wrap(data);

        if(buff.limit() < HeadSize)
        {
            System.err.println("[ERROR]: Invalid message size");
            return null;
        }

        double ver = buff.getDouble();
        if(ver != EasyRPC.Version)
        {
            System.err.println("[ERROR]: Not matching Version (" + ver + " vs " + EasyRPC.Version + ")");
            return null;
        }

        int icall = buff.getInt();
        if(0 > icall || icall > CallKind.values().length)
        {
            System.err.println("[ERROR]: Invalid call type");
            return null;
        }
        CallKind callas =  CallKind.values()[icall];

        int size = buff.getInt();
        byte[] nameBuff = new byte[size];
        if(buff.remaining() < size)
        {
            System.err.println("[ERROR]: Invalid size value in head");
            return null;
        }
        buff.get(nameBuff, 0, size);
        String name = new String(nameBuff);

        List<Object> args = new ArrayList<>();
        while(buff.remaining() > 0)
        {
            char typeCode = buff.getChar();
            ParamType type = ParamType.values()[typeCode];
            int paramsize = buff.getInt();
            byte[] param = new byte[paramsize];
            buff.get(param);
            args.add(typeSerializtor.Deserialize(param, type));
        }

        return new CallData(args, name, callas);
    }
    
}
