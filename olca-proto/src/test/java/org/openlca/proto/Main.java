package org.openlca.proto;

import java.util.UUID;

import com.google.protobuf.util.JsonFormat;

public class Main {

  public static void main(String[] args) throws Exception {
    var p = Proto.Ref.newBuilder()
        .setType("Process")
        .setId(UUID.randomUUID().toString())
        .setName("steel")
        .setDescription("some steel process")
        .addCategoryPath("materials")
        .addCategoryPath("metals")
        .build();
    var json = JsonFormat.printer().print(p);
    var clone = Proto.Ref.newBuilder();
    JsonFormat.parser().merge(json, clone);
    json = JsonFormat.printer().print(clone.build());
    System.out.println(json);
    genFlow();
  }

  private static void genFlow() throws Exception {
    var flow = Proto.Flow.newBuilder()
        .setType("Flow")
        .setId(UUID.randomUUID().toString())
        .setName("Steel")
        .setFlowType(Proto.FlowType.PRODUCT_FLOW)
        .build();
    var json = JsonFormat.printer().print(flow);
    System.out.println(json);
  }
}
