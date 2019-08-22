package org.openlca.io.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class FlowMapTest {

	@Test
	public void testWriteRead() throws Exception {
		// create
		FlowMap fm = new FlowMap();
		FlowMapEntry e = new FlowMapEntry();
		fm.entries.add(e);
		e.factor = 1000.0;
		e.sourceFlow = new FlowRef();
		e.sourceFlow.flow = new FlowDescriptor();
		e.sourceFlow.flow.refId = "06d4812b-6937-4d64-8517-b69aabce3648";
		e.sourceFlow.unit = new UnitDescriptor();
		e.sourceFlow.unit.refId = "some-unit-id";
		e.sourceFlow.unit.name = "kg";

		e.targetFlow = new FlowRef();
		e.targetFlow.flow = new FlowDescriptor();
		e.targetFlow.flow.refId = "3f7e0b7e-aefd-4efd-a946-7bdad142fd50";
		e.targetFlow.unit = new UnitDescriptor();
		e.targetFlow.unit.refId = "some-other-unit-id";
		e.targetFlow.unit.name = "m3";

		// write & read
		File tmpFile = Files.createTempFile(
				"_olca_" + getClass().getSimpleName(), ".csv").toFile();
		FlowMap.toCsv(fm, tmpFile);
		fm = FlowMap.fromCsv(tmpFile);
		tmpFile.delete();

		// check it
		e = fm.getEntry("06d4812b-6937-4d64-8517-b69aabce3648");
		assertEquals("3f7e0b7e-aefd-4efd-a946-7bdad142fd50",
				e.targetFlow.flow.refId);
		assertEquals(1000.0, e.factor, 1e-16);
		assertNull(e.sourceFlow.status);
		assertNull(e.targetFlow.status);

		// check units
		assertEquals("some-unit-id", e.sourceFlow.unit.refId);
		assertEquals("kg", e.sourceFlow.unit.name);
		assertEquals("some-other-unit-id", e.targetFlow.unit.refId);
		assertEquals("m3", e.targetFlow.unit.name);
	}
}
