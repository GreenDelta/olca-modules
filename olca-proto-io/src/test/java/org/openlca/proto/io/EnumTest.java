package org.openlca.proto.io;

import org.junit.Assert;
import org.junit.Test;

import com.google.protobuf.util.JsonFormat;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowType;

public class EnumTest {

  @Test
  public void testFlowType() throws Exception {
    var flow = ProtoFlow.newBuilder()
        .setName("CO2")
        .setFlowType(ProtoFlowType.ELEMENTARY_FLOW)
        .build();
    var json = JsonFormat.printer().print(flow);
    Assert.assertTrue(json.contains("ELEMENTARY_FLOW"));

    var clone = ProtoFlow.newBuilder();
    JsonFormat.parser().merge(json, clone);
    Assert.assertEquals("CO2", clone.getName());
    Assert.assertEquals(ProtoFlowType.ELEMENTARY_FLOW, clone.getFlowType());
  }
}
