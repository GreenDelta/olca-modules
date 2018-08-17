package org.openlca.core.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UncertaintyTest {

	@Test
	public void testToFromString() {
		Uncertainty u = Uncertainty.normal(4.2, 1.1);
		u = Uncertainty.fromString(u.toString());
		assertEquals(u.distributionType, UncertaintyType.NORMAL);
		assertEquals(u.parameter1, 4.2, 1e-4);
		assertEquals(u.parameter2, 1.1, 1e-4);

		u = Uncertainty.logNormal(434.21E9, 1.3E-12);
		u = Uncertainty.fromString(u.toString());
		assertEquals(u.distributionType, UncertaintyType.LOG_NORMAL);
		assertEquals(u.parameter1, 434.21E9, 1e-4);
		assertEquals(u.parameter2, 1.3E-12, 1e-4);

		u = Uncertainty.uniform(11111111, 22222222);
		u = Uncertainty.fromString(u.toString());
		assertEquals(u.distributionType, UncertaintyType.UNIFORM);
		assertEquals(u.parameter1, 11111100, 1e-4);
		assertEquals(u.parameter2, 22222200, 1e-4);

		u = Uncertainty.triangle(1, 2, 3);
		u = Uncertainty.fromString(u.toString());
		assertEquals(u.distributionType, UncertaintyType.TRIANGLE);
		assertEquals(u.parameter1, 1, 1e-4);
		assertEquals(u.parameter2, 2, 1e-4);
		assertEquals(u.parameter3, 3, 1e-4);
	}

}
