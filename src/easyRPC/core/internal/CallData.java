package easyRPC.core.internal;

import java.util.ArrayList;
import java.util.List;

import easyRPC.EasyRPC.CallKind;

public class CallData {

    public final List<Object> Args;
    public final int TargetHash;
    public final CallKind CallKind;

    public CallData(final int hash, final CallKind kind, final Object... rargs)
    {
        CallKind = kind;
        TargetHash = hash;
        List<Object> ar = new ArrayList<>();
        for (Object arg : rargs) {
            ar.add(arg);
        }
        Args = ar;
    }
    
    public CallData(final List<Object> rargs, final int hash, final CallKind kind)
    {
        CallKind = kind;
        TargetHash = hash;
        Args = new ArrayList<>(rargs);
    }    
}
