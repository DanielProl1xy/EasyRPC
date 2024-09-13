package easyRPC.core.interfaces;

import easyRPC.core.internal.CallData;

public interface ICallSerializator {

    public byte[] SerializeCallData(final CallData call);
    public CallData DeserializeCallData(final byte[] data);
    
}