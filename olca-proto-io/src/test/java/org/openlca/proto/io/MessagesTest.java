package org.openlca.proto.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowPropertyFactor;

public class MessagesTest {

  @Test
  public void testEmpty() {
    var builder = ProtoFlow.newBuilder();
    assertTrue(Messages.isEmpty(builder));
    assertFalse(Messages.isNotEmpty(builder));
    assertTrue(Messages.isEmpty(builder.build()));
    assertFalse(Messages.isNotEmpty(builder.build()));
  }

  @Test
  public void testNotEmpty() {
    var builder = ProtoFlow.newBuilder()
      .setName("Something");
    assertFalse(Messages.isEmpty(builder));
    assertTrue(Messages.isNotEmpty(builder));
    assertFalse(Messages.isEmpty(builder.build()));
    assertTrue(Messages.isNotEmpty(builder.build()));
  }

  @Test
  public void testRepeated() {
    var builder = ProtoFlow.newBuilder()
      .addFlowProperties(ProtoFlowPropertyFactor.newBuilder());
    assertFalse(Messages.isEmpty(builder));
    assertTrue(Messages.isNotEmpty(builder));
    assertFalse(Messages.isEmpty(builder.build()));
    assertTrue(Messages.isNotEmpty(builder.build()));
  }
}
