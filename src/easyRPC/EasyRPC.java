package easyRPC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import easyRPC.annotations.RemoteProcedureCall;
import easyRPC.annotations.ReplicatedClass;
import easyRPC.annotations.RemoteProcedureCall.RPCFlag;
import easyRPC.core.interfaces.ICallSerializator;
import easyRPC.core.internal.CallData;
import easyRPC.core.internal.Param;
import easyRPC.core.internal.RPCHandler;

public final class EasyRPC {

    public final static int ProtocolMagic = 0x32ABAFF0;
    public final static int ProtocolSize = Param.IntSize*2;
    public final static double Version = 0.36;

    private static EasyRPC instance;

    private Map<String, RPCHandler> rpcMap;

    private final ICallSerializator serializator;
        
    public enum CallKind
    {
        CALL,
        CALLBACK,
    }

    public static EasyRPC Start(ICallSerializator serializator) throws InvalidAlgorithmParameterException
    {
        if(instance == null)
        {
            instance = new EasyRPC(serializator);
            return instance;
        }
        throw new InvalidAlgorithmParameterException("Easy RPC is already running");
    }

    public static EasyRPC GetInstance()
    {
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

                boolean withCallBack = false;

                for (RPCFlag flag : rpc.flags()) {
                    switch (flag) {
                        case WithCallBack:
                            withCallBack = true;
                            break;
                        default:
                            break;
                    }
                }

                if(withCallBack)
                {
                    if(rpc.callbackName().isEmpty()) throw new InvalidParameterException("Callback name can't be emtpy.");

                    callback = clazz.getDeclaredMethod(rpc.callbackName(), boolean.class);
                    callback.setAccessible(true);
                    if(!Modifier.isStatic(callback.getModifiers())) throw new InvalidParameterException("Callback method must be static member");
                    
                    handler = new RPCHandler(clazz.getName() + "." + method.getName(), method, callback, true);
                }
                else
                {
                    handler = new RPCHandler(clazz.getName() + "." +  method.getName(), method, null, false);
                }

                rpcMap.put(handler.Name, handler);
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

        CallData co = serializator.deserializeCallData(message.array());
        if(co != null && rpcMap.containsKey(co.Name))
        {
            RPCHandler handler = rpcMap.get(co.Name);
            switch (co.CallKind) {
                case CALLBACK:
                    try {
                        handler.CallBack(null ,true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case CALL:
                    try {
                        handler.Handle(null, co.Args.toArray());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(handler.WithCallBack)
                    {
                        callRPC(new CallData(handler.Name, CallKind.CALLBACK), sock);
                    }
                    break;
            }
            return true;
        }

        return false;
    }

    public boolean Call(Socket sock, Class<?> clazz, final String rpcName, Object... args) 
    {
        String fullName = clazz.getName() + "." + rpcName;
        if(!rpcMap.containsKey(fullName))
        {
            throw new InvalidParameterException("RPC Handler with name \"" + fullName + "\" called but not registered");
        }
        return callRPC(new CallData(fullName, CallKind.CALL, args), sock);        
    }

    private boolean callRPC(final CallData rpc, Socket sock)
    {
        byte[] data = serializator.serializeCallData(rpc);
        
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

    private EasyRPC(ICallSerializator ser)
    {
        if(ser == null) throw new InvalidParameterException("Serializtor is null.");

        rpcMap = new HashMap<String, RPCHandler>();
        serializator = ser;
    }
}
