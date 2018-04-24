# olca-ipc
This is a package for inter process communication with openLCA using a
[JSON-RPC](http://www.jsonrpc.org/specification) protocol over HTTP.

* the protocol always communicates with a single database (when starting the
  IPC server in openLCA it is the currently active database)

## Methods

### Insert a model / a data set
The example below shows the request for inserting a data set in the database:

```javascript
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "insert/model",
  "params": {
    "@id": "4a40cb39-e306-3649-b6da-ca061e384e23",
    "@type": "Flow",
    "name": "electricity, high voltage, at grid",
    // ...    
  }
}
```

The `params` attribute directly contains the data set that should be inserted.
Note that other data sets that are referenced from the data set to be inserted
need to be already present in the database.