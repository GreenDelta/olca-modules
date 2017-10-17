package org.openlca.io.simapro.csv.input;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.io.maps.MapFactor;
import org.openlca.io.maps.OlcaFlowMapEntry;
import org.openlca.util.KeyGen;

public class ImportMapTest {

	private ImportMap map;

	@Before
	public void setUp() {
		map = ImportMap.load(null);
	}

	@Test
	public void testMapLoaded() throws Exception {
		Assert.assertNotNull(map);
	}

	@Test
	public void testGetFlowEntry() throws Exception {
		String key = KeyGen.get("Barium", "Emissions to air",
				"low. pop., long-term", "kg");
		MapFactor<OlcaFlowMapEntry> entry = map.getFlowEntry(key);
		OlcaFlowMapEntry flow = entry.getEntity();
		Assert.assertEquals("cd5898ca-96ad-4038-b7ac-edf544d2800a",
				flow.getFlowId());
		Assert.assertEquals("93a60a56-a3c8-11da-a746-0800200b9a66",
				flow.getRefPropertyId());
		Assert.assertEquals("20aadc24-a391-41cf-b340-3e4529f44bde",
				flow.getRefUnitId());
		Assert.assertEquals(1, entry.getFactor(), 1e-16);
	}

}
