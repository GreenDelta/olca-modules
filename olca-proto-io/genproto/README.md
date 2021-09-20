# genproto

The `genproto` tool directly generates the [olca.proto](../src/main/proto/olca.proto)
definitions from the definitions of the YAML files of the
[olca-schema project](https://github.com/GreenDelta/olca-schema). It takes
the path to the `olca-schema` folder as first argument and the path to the
output file of the proto3 definitions as second argument:

```
$ genproto path/to/olca-schema path/to/olca.proto
```
