package easyRPC.core.internal;

import java.util.ArrayList;
import java.util.List;

import easyRPC.EasyRPC.CallKind;

public class CallData {

    public final List<Object> Args;
    public final String Name;
    public final CallKind CallKind;

    public CallData(final String n, final CallKind kind, final Object... rargs)
    {
        CallKind = kind;
        Name = n;
        List<Object> ar = new ArrayList<>();
        for (Object arg : rargs) {
            ar.add(arg);
        }
        Args = ar;
    }
    
    public CallData(final List<Object> rargs, final String n, final CallKind kind)
    {
        CallKind = kind;
        Name = n;
        Args = new ArrayList<>(rargs);
    }    
}
