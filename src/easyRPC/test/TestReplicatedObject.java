package easyRPC.test;

import easyRPC.annotations.RemoteCallBack;
import easyRPC.annotations.RemoteProcedureCall;
import easyRPC.annotations.ReplicateObject;
import easyRPC.annotations.RemoteProcedureCall.RPCFlag;

@ReplicateObject
public class TestReplicatedObject {
    
    @RemoteProcedureCall(flags = {RPCFlag.WithCallBack}, callbackName="HandleCallBack")
    private void Handle()
    {
        System.out.println("Hello, world!"); 
    }

    @RemoteCallBack
    private void HandleCallBack(final boolean result)
    {
        System.out.println("CallBack " + result); 
    }

    @RemoteProcedureCall
    private void WithParam(float id, String str, boolean val)
    {
        System.out.println("Got parameters: " + id + ", " + str + " " + val);
    }
}
