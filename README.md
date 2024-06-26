# EasyRPC 
This is a simple Remote Procedure Call library, that implements easy to understand intarface for registering, calling and handling two-sided networking with RPCs. 
# How to use
1. Create a RPC handler object
```java     
RPCHandler testHandler = new RPCHandler("test", // Name the RPC
// name must be unique
// an interface for handling calls and callbacks of this RPC
new IRPCH() { 
    @Override
    public void Handle() { System.out.println("Hello, world!"); }
    @Override 
    public void CallBack() { System.out.println("Call back!"); }
}, 
true); // Indicates wether RPC should call the callback
```
3. Register a RPC in the system
``` java
EasyRPC sys = EasyRPC.GetInstance(); // Singleton implementation
sys.RegisterHandler(testHandler);
```
2. Open sockets using Sockets class
```java
Socket socket = serverSocket.accept(); // Server machine

Socket sock = new Socket(host, port); // Client machine
```
3. Call the RPC
```java
EasyRPC sys = EasyRPC.GetInstance();
sys.Call(testHandler, socket);

// wait for the call-back, if needed
sys.Recieve(cli);     
```
4. Recieve the RPC on another machine
```java
EasyRPC sys = EasyRPC.GetInstance();
sys.Recieve(anotherSocket);
```
Done! You succesfully did the remote procedure call using EasyRPC.
