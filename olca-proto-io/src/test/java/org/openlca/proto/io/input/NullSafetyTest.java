package org.openlca.proto.io.input;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoFlowPropertyType;

public class NullSafetyTest {

	@Test
	public void testRefId() {
		var proto = ProtoCurrency.newBuilder().build();
		var refId = proto.getRefCurrency().getId();
		assertEquals("", refId);
	}

	@Test
	public void testEnumItem() {
		var proto = ProtoFlowProperty.newBuilder().build();
		var type = proto.getFlowPropertyType();
		assertNotNull(type);
		assertEquals(ProtoFlowPropertyType.UNDEFINED_FLOW_PROPERTY_TYPE, type);
	}
}
