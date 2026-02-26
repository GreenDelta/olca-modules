package org.openlca.sd.xmile;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class XmileReaderTest {

	private Xmile xmile;

	@Before
	public void setup() throws IOException {
		try (var stream = getClass().getResourceAsStream("example.xml")) {
			xmile = Xmile.readFrom(stream).orElseThrow();
		}
	}

	@Test
	public void testReadHeader() {
		var header = xmile.header();
		assertEquals("Example model", header.name());
		assertEquals("c5e5b6ba-6e2d-41f4-b627-c0ae7c4af38c", header.uuid());
		assertEquals("isee systems, inc.", header.vendor());
	}

	@Test
	public void testReadDims() {
		var dims = xmile.dims();
		assertEquals(1, dims.size());

		var dim = dims.getFirst();
		assertEquals("Dim_Name_1", dim.name());
		assertEquals(1, dim.size());
		assertTrue(dim.elems().isEmpty());
	}

	@Test
	public void testReadSimSpecs() {
		var specs = xmile.simSpecs();
		assertEquals("Euler", specs.method());
		assertEquals("Years", specs.timeUnits());
		assertEquals(1.0, specs.start(), 0.0001);
		assertEquals(25.0, specs.stop(), 0.0001);
		assertEquals(4.0, specs.dt().value(), 0.0001);
	}

	@Test
	public void testReadStocks() {
		var stocks = xmile.model().stocks();
		assertEquals(2, stocks.size());

		var pop = stocks.stream()
			.filter(s -> s.name().equals("Population"))
			.findFirst()
			.orElseThrow();
		assertEquals("100", pop.eqn);
		assertTrue(pop.isNonNegative());
		assertEquals("being_born", pop.inflows().getFirst());
		assertEquals("dying", pop.outflows().getFirst());
	}

	@Test
	public void testReadFlows() {
		var flows = xmile.model().flows();
		assertEquals(4, flows.size());

		var birth = flows.stream()
			.filter(f -> f.name().equals("regenerating"))
			.findFirst()
			.orElseThrow();
		assertEquals("(1+0.5)*Natural_Resources*regeneration_rate", birth.eqn);
	}

	@Test
	public void testReadAuxs() {
		var auxs = xmile.model().auxs();
		assertEquals(6, auxs.size());

		var birthRate = auxs.stream()
			.filter(a -> a.name().equals("death rate"))
			.findFirst()
			.orElseThrow();
		assertEquals(
			"\"resources\\\\_person\"/INIT(\"resources\\\\_person\")",
			birthRate.eqn);
	}
}
