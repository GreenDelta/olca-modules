package org.openlca.proto.io.input;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.proto.ProtoCurrency;

public class NullSafetyTest {

	@Test
	public void testRefId() {
		var proto = ProtoCurrency.newBuilder().build();
		var refId = proto.getRefCurrency().getId();
		assertEquals("", refId);
	}
}
