package org.openlca.sd.xmile;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class XmileArraysTest {

	private Xmile xmile;

	@Before
	public void setup() throws IOException {
		try (var stream = getClass().getResourceAsStream("arrays.xml")) {
			xmile = Xmile.readFrom(stream).orElseThrow();
		}
	}

	@Test
	public void testDimensions() {
		var productDim = xmile.dims().getFirst();
		assertEquals("products", productDim.name());
		assertEquals(3, productDim.size());
		assertTrue(productDim.elems().isEmpty());

		var regionDim = xmile.dims().get(1);
		assertEquals("regions", regionDim.name());
		assertEquals(2, regionDim.size());
		assertEquals(2, regionDim.elems().size());
		assertEquals("north", regionDim.elems().getFirst().name());
		assertEquals("south", regionDim.elems().get(1).name());
	}

	@Test
	public void testSingleDimVarApplyToAll() {
		var aux = xmile.model().auxs().stream()
			.filter(a -> a.name().equals("single dim var apply to all"))
			.findFirst()
			.orElseThrow();
		assertEquals(1, aux.dimensions().size());
		assertEquals("products", aux.dimensions().getFirst().name());
		assertEquals("20", aux.eqn());
	}

	@Test
	public void testMultiDimVarApplyToAll() {
		var aux = xmile.model().auxs().stream()
			.filter(a -> a.name().equals("multi dim var apply to all"))
			.findFirst()
			.orElseThrow();
		assertEquals(2, aux.dimensions().size());
		assertEquals("products", aux.dimensions().get(0).name());
		assertEquals("regions", aux.dimensions().get(1).name());
		assertEquals("20", aux.eqn());
	}

	@Test
	public void testSingleDimVarNonApplyToAll() {
		var aux = xmile.model().auxs().stream()
			.filter(a -> a.name().equals("single dim var non apply to all"))
			.findFirst()
			.orElseThrow();
		assertEquals(1, aux.dimensions().size());
		assertEquals("products", aux.dimensions().getFirst().name());
		assertNull(aux.eqn);
		assertEquals(3, aux.elements().size());
		for (var e : aux.elements()) {
			assertEquals("20", e.eqn);
		}
	}

	@Test
	public void testGraphicalNotApplyToAll() {
		var aux = xmile.model().auxs().stream()
			.filter(a -> a.name().equals("graphical not_apply_to_all"))
			.findFirst()
			.orElseThrow();
		assertEquals(1, aux.dimensions().size());
		assertEquals("products", aux.dimensions().getFirst().name());
		assertEquals("TIME", aux.eqn);
		assertEquals(3, aux.elements().size());
		for (var e : aux.elements()) {
			assertNull(e.eqn);
			var gf = e.gf();
			assertEquals(1, gf.xscale.min, 1e-16);
			assertEquals(9, gf.xscale.max, 1e-16);
		}
	}

}
