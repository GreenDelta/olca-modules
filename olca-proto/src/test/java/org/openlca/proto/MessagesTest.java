package org.openlca.proto;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessagesTest {

  @Test
  public void testEmpty() {
    var builder = Proto.Flow.newBuilder();
    assertTrue(Messages.isEmpty(builder));
    assertFalse(Messages.isNotEmpty(builder));
    assertTrue(Messages.isEmpty(builder.build()));
    assertFalse(Messages.isNotEmpty(builder.build()));
  }

  @Test
  public void testNotEmpty() {
    var builder = Proto.Flow.newBuilder()
      .setName("Something");
    assertFalse(Messages.isEmpty(builder));
    assertTrue(Messages.isNotEmpty(builder));
    assertFalse(Messages.isEmpty(builder.build()));
    assertTrue(Messages.isNotEmpty(builder.build()));
  }

  @Test
  public void testRepeated() {
    var builder = Proto.Flow.newBuilder()
      .addFlowProperties(Proto.FlowPropertyFactor.newBuilder());
    assertFalse(Messages.isEmpty(builder));
    assertTrue(Messages.isNotEmpty(builder));
    assertFalse(Messages.isEmpty(builder.build()));
    assertTrue(Messages.isNotEmpty(builder.build()));
  }

}
