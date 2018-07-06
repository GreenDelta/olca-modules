# The openLCA IPC protocol

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

## Runtime methods

### `runtime/shutdown`
Shutdown the server and database.

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "runtime/shutdown"
}
```
