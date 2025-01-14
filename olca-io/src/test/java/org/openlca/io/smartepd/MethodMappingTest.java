package org.openlca.io.smartepd;

import static org.junit.Assert.*;

import org.junit.Test;

public class MethodMappingTest {

	@Test
	public void testGetAll() {
		var mappings = MethodMapping.getDefault();
		assertFalse(mappings.isEmpty());
		for (var m : mappings) {
			assertNotNull(m.method());
			assertNotNull(m.ref());
			assertFalse(m.indicators().isEmpty());
		}
	}

}
