# olca-ipc
This module implements a [JSON-RPC](http://www.jsonrpc.org/specification) based
[protocol](./protocol.md) for inter-process communication (IPC) with openLCA.
With this, it is possible to call functions in openLCA and processing their
results from outside of the Java Runtime in which openLCA is executed. A
reference implementation of [this protocol](./protocol.md) for standard Python
is provided with the [olca-ipc.py](https://github.com/GreenDelta/olca-ipc.py)
package.

## Building the server app

```bash
mvn package -P server-app
```

## Principles
On the openLCA side, an IPC server is started which accepts function calls
and returns results. Function calls and results are encoded in JSON-RPC where
a function just has a method name and optional parameters and returns a result
or error. The function calls and return values are just plain JSON objects
send over a transport protocol. Currently, the openLCA IPC server uses HTTP
and accepts `POST` requests for method calls. Thus, you could even use
[curl](https://curl.haxx.se/) to call functions:

```bash
curl -X POST http://localhost:8080 -d @file.json -H "Content-Type: application/json"
```

For Parameters and results the types as defined in the
[olca-schema](https://github.com/GreenDelta/olca-schema) format are used. This
is the same format that is used for the `Linked Data` export and import in
openLCA. However, not everything that is defined in the `olca-schema` format
can be imported or exported (like calculation setups for example).


## API
Using the IPC server via the API looks like this:

```java
Server server = new Server(8080);
server.withDefaultHandlers(aDatabase, aMatrixSolver);
server.start();
```

This will start the server at port 8080 with the default protocol (see below).
However, it is also possible to configure the server protocol by registering
specific method handlers (instead of calling `withDefaultHandlers`):

```java
server.register(aHandler1);
server.register(aHandler2);
// ...
```

A handler is a plain object of which methods are registered to handle method
calls if they fulfill the following requirements:

* they are declared as `public` and annotated with the `@Rpc` annotation
  providing a unique method name
* they take a single parameter of type `RpcRequest`
* they return a result of type `RpcResponse`

For example, an instance of the following class could be used as handler:

```java
public class MyHandler {

  @Rpc("my/method")
  public RpcResponse myMethod(RpcRequest req) {
    return Responses.of("Works!", req);
  }
}
```
