package easyRPC;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import easyRPC.annotations.RemoteProcedureCall;
import easyRPC.annotations.ReplicatedClass;
import easyRPC.core.IRPCH;
import easyRPC.core.Pair;
import easyRPC.core.Param;
import easyRPC.core.Param.ParamType;
import easyRPC.core.RPCHandler;


public final class EasyRPC {

    public final static double Version = 0.35;
    public final static int ProtocolMagic = 0xABD7FA00;
    public final static int ProtocolSize = Param.IntSize*2;
    public final static int HeadSize = Param.IntSize*2 + Param.DoubleSize;

    private Map<String, RPCHandler> rpcMap;
    
    private static EasyRPC instance;
        
    public enum CallKind
    {
        CALL,
        CALLBACK,
    }

    public static EasyRPC GetInstance()
    {
        if(instance == null)
        {
            instance = new EasyRPC();
        }

        return instance;
    }
    
    public void RegisterClass(Class<?> clazz) throws Exception
    {
        if(!clazz.isAnnotationPresent(ReplicatedClass.class))
        {
            throw new InvalidParameterException("Object must have @ReplicatedClass annotation");
        }
        
        for (Method method : clazz.getDeclaredMethods()) {
            
            if(method.isAnnotationPresent(RemoteProcedureCall.class))
            {
                method.setAccessible(true);
                RemoteProcedureCall rpc = (method.getAnnotation(RemoteProcedureCall.class));

                if(!Modifier.isStatic(method.getModifiers())) throw new InvalidParameterException("RPC method must be static mebmer");

                Method callback;
                RPCHandler handler;

                if(rpc.withCallBack())
                {
                    if(rpc.callbackName().isEmpty()) throw new InvalidParameterException("Callback name can't be emtpy.");

                    callback = clazz.getDeclaredMethod(rpc.callbackName(), boolean.class);
                    callback.setAccessible(true);
                    if(!Modifier.isStatic(callback.getModifiers())) throw new InvalidParameterException("Callback method must be static member");

                    handler = new RPCHandler(method.getName(), new IRPCH() {
                        @Override
                        public void Handle(Object... args) { 
                            try {
                                method.invoke(null, args); 
                                
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        @Override 
                        public void CallBack(final boolean result) {
                            try {
                                callback.invoke(null, result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, true);
                }
                else
                {
                    handler = new RPCHandler(method.getName(), new IRPCH() {
                        @Override
                        public void Handle(Object... args) { 
                            try {
                                method.invoke(null, args); 
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        @Override 
                        public void CallBack(final boolean result) {
                        }
                    }, true);
                }

                rpcMap.put(method.getName(), handler);
            }
        }
    }

    public boolean Receive(Socket sock)
    {
        InputStream in;
        ByteBuffer message;
        try {
            in = sock.getInputStream();
            ByteBuffer protocolBuff = ByteBuffer.allocate(ProtocolSize);
            protocolBuff.put(in.readNBytes(ProtocolSize));
            protocolBuff.position(0);
            int protocol = protocolBuff.getInt();
            if(protocol != ProtocolMagic)
            {
                System.err.println("[ERROR]: Failed to read message, invalid protocol magic " + protocol);
                return false;
            }
            int size = protocolBuff.getInt();
            message = ByteBuffer.wrap(in.readNBytes(size));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        List<Object> args = new ArrayList<>();
        Pair<RPCHandler, CallKind> pair = deserializeCallData(message.array(), args);
        if(pair != null)
        {
            switch (pair.Second) {
                case CALLBACK:
                    pair.First.CallBack(true);
                    break;
                case CALL:
                    pair.First.Handle(args.toArray());
                    if(pair.First.WithCallBack)
                    {
                        callRPC(pair.First, sock, CallKind.CALLBACK);
                    }
                    break;
            }
            return true;
        }

        return false;
    }

    public boolean Call(Socket sock, final String rpcName, Object... args) 
    {
        if(!rpcMap.containsKey(rpcName))
        {
            throw new InvalidParameterException("RPC Handler with name \"" + rpcName + "\" called but not registered");
        }
        return callRPC(rpcMap.get(rpcName), sock, CallKind.CALL, args);        
    }

    private boolean callRPC(final RPCHandler rpc, Socket sock, CallKind callas, Object... args)
    {
        byte[] data = serializeCallData(rpc, callas, args);
        
        OutputStream out;
        try {
            out = sock.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            out.write(data);
        } catch (IOException e) {
            System.err.println("[ERROR]: Failed to call RPC, error: " + e.getMessage());
            return false;
        }
        return true;
    }

    private byte[] serializeCallData(final RPCHandler rpc, final CallKind callas, Object... args)
    {
        int argsSize = 0;
        List<Param> params = new ArrayList<>();
        for (Object arg : args) {
            Param p = new Param(arg);
            params.add(p);
            argsSize += p.Size + 6;
        }

        int size = rpc.Name.length() + Param.IntSize*4 + Param.DoubleSize + argsSize;
        ByteBuffer buff = ByteBuffer.allocate(size);

        buff.putInt(ProtocolMagic);
        buff.putInt(size - ProtocolSize); // body size
        buff.putDouble(EasyRPC.Version);
        buff.putInt(callas.ordinal());
        buff.putInt(rpc.Name.length());
        buff.put(rpc.Name.getBytes());

        for (Param param : params) {
            buff.put(param.Bytes);
        }

        return buff.array();
    }

    private Pair<RPCHandler, CallKind> deserializeCallData(final byte[] data, List<Object> args)
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

        if(buff.remaining() >= 0)
        {
            while(buff.remaining() > 0)
            {
                char typeCode = buff.getChar();
                ParamType type = ParamType.values()[typeCode];
                int paramsize = buff.getInt();
                byte[] param = new byte[paramsize];
                buff.get(param);
                Param p = new Param(type, paramsize, param);
                args.add(p.Value);
            }
        } 

        if(rpcMap.containsKey(name))
        {
            return new Pair<RPCHandler, CallKind>(rpcMap.get(name), callas);
        }
        else
        {
            System.err.println("[ERROR]: RPC with name \"" + name + "\" was not found");
        }

        return null;
    }

    private EasyRPC()
    {
        rpcMap = new HashMap<String, RPCHandler>();
    }
}
