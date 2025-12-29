package org.openlca.io.pubchem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PubChemClientTest {

	private PubChemClient client;

	@Before
	public void setup() {
		client = PubChemClient.create();
	}

	@After
	public void cleanup() {
		client.close();
	}

	@Test
	public void testGetCompound() {
		var ethanol = client
			.getCompoundsByName("ethanol")
			.orElseThrow()
			.getFirst();
		assertEquals(702, ethanol.id());
		assertEquals("C2H6O", ethanol.molecularFormula());
		assertEquals("CCO", ethanol.absoluteSmiles());
		assertEquals("CCO", ethanol.connectivitySmiles());
		assertEquals("ethanol", ethanol.iupacNamePreferred());
		assertEquals("ethanol", ethanol.iupacNameSystematic());
		assertEquals("ethanol", ethanol.iupacNameTraditional());
	}

	@Test
	public void testGetCompoundView() {
		var view = client
			.getCompoundView(702)
			.orElseThrow();

		var refs = view.references();
		assertFalse(refs.isEmpty());

		var first = refs.getFirst();
		assertNotNull(first.sourceName());
		assertNotNull(first.sourceId());
		assertNotNull(first.name());
	}
}

