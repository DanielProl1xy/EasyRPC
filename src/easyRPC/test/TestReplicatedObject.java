package easyRPC.test;

import easyRPC.annotations.RemoteCallBack;
import easyRPC.annotations.RemoteProcedureCall;
import easyRPC.annotations.ReplicatedClass;
import easyRPC.annotations.RemoteProcedureCall.RPCFlag;

@ReplicatedClass
public class TestReplicatedObject {
    
    @RemoteProcedureCall(flags = {RPCFlag.WithCallBack}, callbackName="HandleCallBack")
    private static void Handle()
    {
        System.out.println("Hello, world!"); 
    }

    @RemoteCallBack
    private static void HandleCallBack(final boolean result)
    {
        System.out.println("CallBack"); 
    }

    @RemoteProcedureCall
    private static void WithParam(float id, String str, boolean val)
    {
        System.out.println("Got parameters: " + id + ", " + str + " " + val);
    }
}
