package easyRPC.test;

import easyRPC.annotations.RemoteProcedureCall;
import easyRPC.annotations.ReplicatedClass;

@ReplicatedClass
public class TestReplicatedObject {
    
    @RemoteProcedureCall(withCallBack = true, callbackName="HandleCallBack")
    private static void Handle()
    {
        System.out.println("Hello, world!"); 
    }

    private static void HandleCallBack()
    {
        System.out.println("CallBack"); 
    }
}
