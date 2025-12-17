package org.openlca.io.pubchem;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PubChemClientTest {

	@Test
	public void getCompoundsByName() {
		try (var client = PubChemClient.create()) {
			var ethanol = client
				.getCompoundsByName("ethanol")
				.orElseThrow()
				.getFirst();
			assertEquals(Long.valueOf(702), ethanol.id());
		}
	}
}

