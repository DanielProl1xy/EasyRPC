package easyRPC.test;

import easyRPC.annotations.RemoteCallBack;
import easyRPC.annotations.RemoteProcedureCall;
import easyRPC.annotations.ReplicatedClass;

@ReplicatedClass
public class TestReplicatedObject {
    
    @RemoteProcedureCall(withCallBack = true, callbackName="HandleCallBack")
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
    private static void WithParam(Long id, Long d)
    {
        System.out.println("Got parameters: " + id + ", " + d);
    }
}
