package easyRPC.core;

public interface IRPCH {
    public void Handle(Object... args);
    public void CallBack(final boolean result);
}
