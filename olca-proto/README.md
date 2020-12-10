# olca-proto
`olca-proto` is an experimental implementation of the
[olca-schema](https://github.com/GreenDelta/olca-schema) based on
[Protocol Buffers](https://developers.google.com/protocol-buffers). It supports
serialization in JSON(-LD) and a fast binary format. In addition, it and comes
with a [gRPC server](https://grpc.io/) that may could replace the current IPC
implementation in openLCA.

__Generating the model__

The [genproto](./scripts/genproto/main.go) tool directly generates the
[olca.proto](./proto/olca.proto) definition from the YAML files of the
`olca-schema` project:

```
$ genproto path/to/olca-schema path/to/olca.proto
```

From the `olca.proto` definition we then generate the APIs with the `gen`
script:

```
$ scripts/gen
```

This requires that the Protocol Buffers and gRPC compiler is in your path (see
the `scripts/gen.bat` script).

__Building the server__

The standalone server can be created via the `server-app` Maven profile:

```
$ mvn package -P server-app
```

This will generate the server application in the `target/dist` folder. The
server can be started via:

```
$ run -db <database> [-port <port>]
```

Where database is the name of a database in the openLCA database folder
(`~/openLCA-1.4-data/databases/<database>`). The port number is optional,
`8080` is chosen by default if it is not specified.

__API Examples__

A Python package for client-side communication is in development here:
https://github.com/msrocka/olca-grpc.py

For Java, a single class `Proto` is generated:

```java
import org.openlca.proto;
import com.google.protobuf.util.JsonFormat;

var flow = Proto.Flow.newBuilder()
    .setType("Flow")
    .setId(UUID.randomUUID().toString())
    .setName("Steel")
    .setFlowType(Proto.FlowType.PRODUCT_FLOW)
    .build();
var json = JsonFormat.printer().print(flow);
System.out.println(json);
```

This will generate the following output:

```json
{
  "@type": "Flow",
  "@id": "481682dd-c2a2-4646-9760-b0fe3e242676",
  "name": "Steel",
  "flowType": "PRODUCT_FLOW"
}
```

To generate the `Go` package, you need put the
[Go plugin protoc-gen-go](https://github.com/protocolbuffers/protobuf-go) of the
protocol buffers compiler in your path.

```go
func main() {
  id, _ := uuid.NewRandom()
  flow := &proto.Flow{
    Type:     "Flow",
    Id:       id.String(),
    Name:     "Steel",
    FlowType: proto.FlowType_PRODUCT_FLOW,
  }
  json := protojson.Format(flow)
  fmt.Println(string(json))
}
```
