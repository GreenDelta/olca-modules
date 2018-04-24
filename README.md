# olca-ipc
This is a package for inter process communication with openLCA using a simple
[JSON-RPC](http://www.jsonrpc.org/specification) based protocol over HTTP. It
is currently under development and will be moved into the
[openLCA core modules](https://github.com/GreenDelta/olca-modules) when it is
stable.

## Principles
* the protocol always communicates with a single database (when starting the
  IPC server in openLCA it is the currently active database)
* ...

## Protocol

### Insert a model / a data set
The example below shows the request for inserting a data set in the database:

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

The `params` attribute directly contains the data set that should be inserted.
Note that other data sets that are referenced from the data set to be inserted
need to be already present in the database. If everything went well the server
will respond with:

```json
{
  "jsonrpc": "2.0",
  "result": "ok"
}
```

### Get a model / a data set

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

The server will respond with the requested data set as result:

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

If there is no such model in the database an error will returned:

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

### Get all models/data sets of a type

```json
{
  "method": "get/models",
  "params": {
    "@type": "Flow"
  }
}
```

### Update a model / a data set
The request for updating a model and also the corresponding response is the same
as for inserting a model, just the method name is `update/model` in this case:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "update/model",
  "params": {
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow",
    "name": "electricity, high voltage, at grid",
    "description": "..." 
  }
}
```

### Delete a model / a data set

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "delete/model",
  "params": {
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow"
  }
}
```
