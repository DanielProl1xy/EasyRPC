package easyRPC;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import easyRPC.annotations.RemoteProcedureCall;
import easyRPC.annotations.ReplicatedClass;
import easyRPC.core.IRPCH;
import easyRPC.core.Pair;
import easyRPC.core.RPCHandler;

public final class EasyRPC {

    public final static double Version = 0.1;
    public final static int ProtocolMagic = 0x89179708;
    public final static int IntSize = 4;
    public final static int DoubleSize = 8;
    public final static int ProtocolSize = IntSize*2;
    public final static int HeadSize = IntSize*2 + DoubleSize;

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
            throw new InvalidParameterException("Object must be replicated object");
        }
        
        for (Method method : clazz.getDeclaredMethods()) {
            
            if(method.isAnnotationPresent(RemoteProcedureCall.class))
            {
                method.setAccessible(true);
                RemoteProcedureCall rpc = (method.getAnnotation(RemoteProcedureCall.class));

                if(!Modifier.isStatic(method.getModifiers())) throw new InvalidParameterException();

                Method callback;
                RPCHandler handler;

                if(rpc.withCallBack())
                {
                    callback = clazz.getDeclaredMethod(rpc.callbackName());
                    callback.setAccessible(true);
                    if(!Modifier.isStatic(callback.getModifiers())) throw new InvalidParameterException();

                    handler = new RPCHandler(method.getName(), new IRPCH() {
                        @Override
                        public void Handle() { 
                            try {
                                method.invoke(null); 
                                
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        @Override 
                        public void CallBack() {
                            try {
                                callback.invoke(null);
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
                        public void Handle() { 
                            try {
                                method.invoke(null); 
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        @Override 
                        public void CallBack() {
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

        Pair<RPCHandler, CallKind> pair = deserializeCallData(message.array());
        if(pair != null)
        {
            switch (pair.Second) {
                case CALLBACK:
                    pair.First.CallBack();
                    break;
                case CALL:
                    pair.First.Handle();
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

    public boolean Call(final String rpcName, Socket sock) 
    {
        if(!rpcMap.containsKey(rpcName))
        {
            throw new InvalidParameterException("RPC Handler with name \"" + rpcName + "\" called but not registered");
        }
        
        return callRPC(rpcMap.get(rpcName), sock, CallKind.CALL);        
    }

    private boolean callRPC(final RPCHandler rpc, Socket sock, CallKind callas)
    {
        byte[] data = serializeCallData(rpc, callas);

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

    private byte[] serializeCallData(final RPCHandler rpc, final CallKind callas)
    {
        int size = rpc.Name.length() + IntSize*4 + DoubleSize;
        ByteBuffer buff = ByteBuffer.allocate(size);

        buff.putInt(ProtocolMagic);
        buff.putInt(size - ProtocolSize); // body size

        buff.putDouble(Version);
        buff.putInt(callas.ordinal());
        buff.putInt(rpc.Name.length());
        buff.put(rpc.Name.getBytes());

        return buff.array();
    }

    private Pair<RPCHandler, CallKind> deserializeCallData(final byte[] data)
    {
        ByteBuffer buff = ByteBuffer.wrap(data);

        if(buff.limit() < HeadSize)
        {
            System.err.println("[ERROR]: Invalid message size");
            return null;
        }

        double Version = buff.getDouble();
        if(Version != Version)
        {
            System.err.println("[ERROR]: Not matching Version (" + Version + " vs " + Version + ")");
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

        if(rpcMap.containsKey(name))
        {
            return new Pair<RPCHandler, CallKind>(rpcMap.get(name), callas);
        }
        else
        {
            System.err.println("[ERROR]: RPC with name \"" + name + "\" not found");
        }

        return null;
    }

    private EasyRPC()
    {
        rpcMap = new HashMap<String, RPCHandler>();
    }
}
