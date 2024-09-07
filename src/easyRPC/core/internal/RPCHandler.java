package easyRPC.core.internal;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;


public final class RPCHandler {

    public final String Name; 

    public final boolean WithCallBack;
    public final Method callMethod;
    public final Method callbackMethod;

    public RPCHandler(final String name, Method callMethod, Method callbackMethod, final boolean callback)
    {
        if(name.length() <= 0)
        {
            throw new InvalidParameterException("RPCHandler: Name must not be emtpy!");
        }
        this.Name = name;
        this.WithCallBack = callback;
        this.callMethod = callMethod;
        this.callbackMethod = callbackMethod;
    }

    final public void Handle(Object targ, Object... args) throws IllegalAccessException, InvocationTargetException
    {
        callMethod.invoke(targ, args);
    }

    final public void CallBack(Object targ, final boolean result) throws IllegalAccessException, InvocationTargetException
    {
        callbackMethod.invoke(targ, result);
    }
} 
