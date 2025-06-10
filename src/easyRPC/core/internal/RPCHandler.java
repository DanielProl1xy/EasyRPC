package easyRPC.core.internal;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class RPCHandler {

    public final boolean WithCallBack;
    public final Method callMethod;
    public final Method callbackMethod;
    public final Object target;

    public RPCHandler(Method callMethod, Method callbackMethod, final boolean callback, Object target)
    {
        this.WithCallBack = callback;
        this.callMethod = callMethod;
        this.callbackMethod = callbackMethod;
        this.target = target;
    }

    final public void Handle(Object... args) throws IllegalAccessException, InvocationTargetException
    {
        callMethod.invoke(target, args);
    }

    final public void CallBack(final boolean result) throws IllegalAccessException, InvocationTargetException
    {
        callbackMethod.invoke(target, result);
    }
} 
