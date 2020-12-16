# olca-proto
`olca-proto` is an experimental implementation of the
[olca-schema](https://github.com/GreenDelta/olca-schema) based on
[Protocol Buffers](https://developers.google.com/protocol-buffers). It supports
serialization in JSON(-LD) and in a fast binary format. In addition, it and
comes with a [gRPC server](https://grpc.io/) that may could replace the current
IPC implementation in openLCA. A Python client library which also contains a
test suite is currently developed here: https://github.com/msrocka/olca-grpc.py.

The proto3 definitions are located in the [./src/main/proto](./src/main/proto)
folder. We use the `protobuf-maven-plugin` as described in the
[grpc-java documentation](https://github.com/grpc/grpc-java#generated-code) to
generate the Java source code from the proto3 files. In Eclipse, you may have
to put the [os-maven-plugin](https://search.maven.org/artifact/kr.motd.maven/os-maven-plugin)
plugin into your `eclipse/dropins` folder as described in the
[os-maven-plugin documentation](https://github.com/trustin/os-maven-plugin#issues-with-eclipse-m2e-or-other-ides).

**Note** that the `olca.proto` file is automatically generated from the
`olca-schema` definitions using the [genproto tool](./genproto). Thus, you
should update the `olca-schema` first and then run the `genproto` tool when
you want to update the model.


__Building the standalone server__

The standalone server can be created via the `server-app` Maven profile:

```
$ mvn package -P server-app
```

This will generate the server application in the `target/olca-grpc-server`
folder. The server can be started via:

```
$ run -db <database> [-port <port>]
```

where database is the name of a database in the openLCA database folder
(`~/openLCA-1.4-data/databases/<database>`). The port number is optional,
`8080` is chosen by default if it is not specified.


__API Examples__

For Java, a single class `Proto` is generated:

```java
import org.openlca.generated.proto;
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
