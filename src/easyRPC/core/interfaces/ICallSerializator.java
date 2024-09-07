package easyRPC.core.interfaces;

import easyRPC.core.internal.CallData;

public interface ICallSerializator {

    public byte[] serializeCallData(final CallData call);
    public CallData deserializeCallData(final byte[] data);
    
}