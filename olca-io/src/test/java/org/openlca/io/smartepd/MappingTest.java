package org.openlca.io.smartepd;

import static org.junit.Assert.*;

import org.junit.Test;

public class MappingTest {

	@Test
	public void testMethodMappings() {
		var mappings = MethodMapping.getDefault();
		assertFalse(mappings.isEmpty());
		for (var m : mappings) {
			assertNotNull(m.method());
			assertNotNull(m.ref());
			assertFalse(m.indicators().isEmpty());
		}
	}

	@Test
	public void testIndicatorMappings() {
		var mappings = IndicatorMapping.getDefault();
		assertFalse(mappings.isEmpty());
		for (var m : mappings) {
			assertNotNull(m.indicator());
			assertFalse(m.refs().isEmpty());
		}
	}

}
