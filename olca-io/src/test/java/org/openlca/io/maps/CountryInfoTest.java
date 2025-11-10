package org.openlca.io.maps;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CountryInfoTest {

	@Test
	public void testCountryInfo() {
		var infos = CountryInfo.getAll();

		var afg = infos.getFirst();
		assertEquals("Afghanistan", afg.name());

		var pol = infos.stream()
				.filter(c -> c.alpha2().equals("PL"))
				.findFirst()
				.orElseThrow();
		assertEquals("POL", pol.alpha3());
		assertEquals("Poland", pol.name());
		assertEquals("28840420-4e3d-3522-a930-8317344a285d", pol.refId());

		var ala = infos.getLast();
		assertEquals("AX", ala.alpha2());
	}

}
