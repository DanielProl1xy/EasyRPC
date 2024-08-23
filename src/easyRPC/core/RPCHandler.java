package easyRPC.core;
import java.security.InvalidParameterException;

public final class RPCHandler {

    public final String Name; 

    public final boolean WithCallBack;

    private final IRPCH hrpc;

    public RPCHandler(final String name, final IRPCH handler, final boolean callback)
    {
        if(name.length() <= 0)
        {
            throw new InvalidParameterException("RPCHandler: Name must not be emtpy!");
        }
        Name = name;
        hrpc = handler;
        WithCallBack = callback;
    }

    final public void Handle()
    {
        hrpc.Handle();
    }

    final public void CallBack()
    {
        hrpc.CallBack();
    }
} 
