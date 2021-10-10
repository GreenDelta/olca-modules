package org.openlca.proto.io;

import java.util.UUID;

import com.google.protobuf.util.JsonFormat;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowType;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoType;

public class Main {

  public static void main(String[] args) throws Exception {
    var p = ProtoRef.newBuilder()
        .setType(ProtoType.Process)
        .setId(UUID.randomUUID().toString())
        .setName("steel")
        .setDescription("some steel process")
        .addCategoryPath("materials")
        .addCategoryPath("metals")
        .build();
    var json = JsonFormat.printer().print(p);
    var clone = ProtoRef.newBuilder();
    JsonFormat.parser().merge(json, clone);
    json = JsonFormat.printer().print(clone.build());
    System.out.println(json);
    genFlow();
  }

  private static void genFlow() throws Exception {
    var flow = ProtoFlow.newBuilder()
        .setType(ProtoType.Flow)
        .setId(UUID.randomUUID().toString())
        .setName("Steel")
        .setFlowType(ProtoFlowType.PRODUCT_FLOW)
        .build();
    var json = JsonFormat.printer().print(flow);
    System.out.println(json);
  }
}
