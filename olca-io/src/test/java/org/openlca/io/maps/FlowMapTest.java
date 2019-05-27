package org.openlca.io.maps;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class FlowMapTest {

	@Test
	public void testReadMap() {
		FlowMap fm = FlowMap.fromCsv(new File(
				"src/main/resources/org/openlca/io/maps/ecospold_2_flow_map.csv"));
		FlowMapEntry e = fm.getEntry("06d4812b-6937-4d64-8517-b69aabce3648");
		assertEquals("3f7e0b7e-aefd-4efd-a946-7bdad142fd50",
				e.targetFlow.flow.refId);
		assertEquals(1000.0, e.factor, 1e-16);
	}

}
