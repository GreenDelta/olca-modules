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