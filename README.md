# Java RPC Framework

This Java RPC (Remote Procedure Call) framework allows for easy creation and management of remote procedure calls using annotations. It supports callback methods for asynchronous processing.

## Features

- **Annotation-based**: Use custom annotations to define replicated classes and remote procedure calls.
- **Callback Support**: Easily specify callback methods for asynchronous handling.
- **Reflection**: Utilizes Java reflection to dynamically invoke methods.

## Annotations

### `@ReplicatedClass`

This annotation marks a class as a replicated that can contain remote procedure calls.

### `@RemoteProcedureCall`

This annotation marks a method as a remote procedure call. It has the following parameters:

- `withCallBack` (boolean): Indicates whether the method has an associated callback.
- `callbackName` (String): The name of the callback method (required if `withCallBack` is true).

**Note**: All methods annotated with `@RemoteProcedureCall` must be declared as `static`.

## Usage

### Step 1: Define a Replicated Class

Create a class and annotate it with `@ReplicatedClass`. Define your remote procedure call methods using `@RemoteProcedureCall`, ensuring that they are static.

```java
@ReplicateObject
public class TestReplicatedObject {
    
    @RemoteProcedureCall(flags = {RPCFlag.WithCallBack}, callbackName="HandleCallBack")
    private void Handle() {
        System.out.println("Hello, world!"); 
    }

    @RemoteCallBack
    private void HandleCallBack(final boolean result) {
        System.out.println("CallBack"); 
    }

    @RemoteProcedureCall 
    private void WithParam(float id, String str, boolean val) 
    {
        /* 
         * Supported parameter types:
         * String
         * Number -> int, long, float, double, char, short, boolean
        */
        System.out.println("Got parameters: " + id + " : " + str + " " + val);
    }
}
```

### Step 2: Register the Class
To register your replicated class with the RPC framework, use the EasyRPC singleton instance and call the RegisterClass method, passing the class you want to register.


```java
EasyRPC sys = EasyRPC.Start(new EasySerializator());
TestReplicatedObject object = new TestReplicatedObject();
sys.RegisterObject(object);
```

### Step 3: Invoke Remote Procedure Calls
Once registered, you can invoke the remote procedure calls defined in your replicated class. The framework will handle the invocation and any associated callbacks.
```java
EasyRPC sys = EasyRPC.GetInstance();
sys.Call(sock, TestReplicatedObject.class, "Handle");
sys.Call(cli, TestReplicatedObject.class, "WithParam", 35.5f, "boolean value is: ", true);

// wait for the call-back, if needed
sys.Recieve(socket);    
```

### Step 4: Receive Remote Procedure Calls
```java
EasyRPC sys = EasyRPC.GetInstance();
sys.Receive(anotherSocket);
```