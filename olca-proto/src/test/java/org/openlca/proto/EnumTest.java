package org.openlca.proto;

import com.google.protobuf.util.JsonFormat;

import org.junit.Assert;
import org.junit.Test;

public class EnumTest {

  @Test
  public void testFlowType() throws Exception {
    var flow = Proto.Flow.newBuilder()
        .setName("CO2")
        .setFlowType(Proto.FlowType.ELEMENTARY_FLOW)
        .build();
    var json = JsonFormat.printer().print(flow);
    Assert.assertTrue(json.contains("ELEMENTARY_FLOW"));

    var clone = Proto.Flow.newBuilder();
    JsonFormat.parser().merge(json, clone);
    Assert.assertEquals("CO2", clone.getName());
    Assert.assertEquals(Proto.FlowType.ELEMENTARY_FLOW, clone.getFlowType());
  }
}
