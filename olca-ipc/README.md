# olca-ipc
This module implements a [JSON-RPC](http://www.jsonrpc.org/specification) based
protocol for inter-process communication (IPC) with openLCA. With this, it is
possible to call functions in openLCA and processing their results from outside
of the Java Runtime in which openLCA is executed. A reference implementation of
this protocol for standard Python is provided with
[olca-ipc.py](https://github.com/GreenDelta/olca-ipc.py).


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

## Protocol
The protocol below is provided by the default handlers of the server. 

### `get/model`
Get a full data set for a given `@type` and `@id`. The `@type` is the same as
used in the `olca-schema` format and the `@id` is the reference ID of the data
set in openLCA. For example, a flow can be retrieved with the following call:


```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "get/model",
  "params": {
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow"
  }
}
```

If the data set can be found, the result will contain the data set in the
`olca-schema` format:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result":{
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow",
    "name": "electricity, high voltage, at grid",
    "description": "..." 
  }
}
```

If the data set cannot be found, the server will return an error:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "error":{
    "code": 404,
    "message": "Not found"
  }
}
```

### `get/models`
Get all data sets of a specific type from a server:

```json
{
  "method": "get/models",
  "params": {
    "@type": "Flow"
  }
}
```

### `get/descriptors`
Get the descriptors of all data sets of a specific type from the server. This
is useful to browse that database content. This will return a list of
[Ref](http://greendelta.github.io/olca-schema/html/Ref.html) objects:

```json
{
  "method": "get/descriptors",
  "params": {
    "@type": "Flow"
  }
}
```


### `insert/model`
Insert a new data set which is provided as parameter: 

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "insert/model",
  "params": {
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow",
    "name": "electricity, high voltage, at grid",
    "description": "..." 
  }
}
```

**Note** that other data sets that are referenced from the data set to be
inserted need to be already present in the database. If everything went well
the server will respond with:

```json
{
  "jsonrpc": "2.0",
  "result": "ok"
}
```

### `update/model`
Similar like `insert/model` but for updating an existing data set in the
database:

```json
{
  "method": "update/model",
  "params": {
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow",
    "name": "electricity, high voltage, at grid",
    "description": "..." 
  }
}
```

### `delete/model`
Delete a model with the given type and ID from the database:

```json
{
  "method": "delete/model",
  "params": {
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow"
  }
}
```

### `calculate`
Calculates a product system. It takes a
[CalculationSetup](http://greendelta.github.io/olca-schema/html/CalculationSetup.html)
as parameter and currently returns a
[SimpleResult](http://greendelta.github.io/olca-schema/html/SimpleResult.html).
**Note** that the result is cached on the server for further result queries,
exports etc. and you need to call the `dispose` function with the result ID in
order to remove the cache.

### `dispose`
Remove the object with the given `@id` from the cache.
