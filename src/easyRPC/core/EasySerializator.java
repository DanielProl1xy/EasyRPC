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
    public byte[] SerializeCallData(final CallData call) 
    {
        int argsSize = 0;
        List<byte[]> params = new ArrayList<>();
        for (Object arg : call.Args) {
            final byte[] paramData = typeSerializtor.Serlialize(arg);
            params.add(paramData);
            argsSize += paramData.length;
        }
        
        int size = Param.IntSize*4 + Param.DoubleSize + argsSize;
        ByteBuffer buff = ByteBuffer.allocate(size);
        
        buff.putInt(EasyRPC.ProtocolMagic);
        buff.putInt(size - EasyRPC.ProtocolSize); // body size
        buff.putDouble(EasyRPC.Version);      
        buff.putInt(call.CallKind.ordinal());
        buff.putInt(call.TargetHash);
        for (byte[] bs : params) {
            buff.put(bs);
        }
        return buff.array();
    }

    @Override
    public CallData DeserializeCallData(byte[] data)
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
        if(icall < 0 || CallKind.values().length < icall)
        {
            System.err.println("[ERROR]: Invalid call type");
            return null;
        }
        CallKind callas =  CallKind.values()[icall];
        int hash = buff.getInt();

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

        return new CallData(args, hash, callas);
    }
    
}
