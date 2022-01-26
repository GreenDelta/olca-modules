package org.openlca.io.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class FlowMapTest {

	@Test
	public void testWriteRead() throws Exception {
		// create

		var source = new FlowRef();
		source.flow = FlowDescriptor.create()
			.refId("06d4812b-6937-4d64-8517-b69aabce3648")
			.get();
		source.unit = UnitDescriptor.create()
			.refId("some-unit-id")
			.name("kg")
			.get();

		var target = new FlowRef();
		target.flow = FlowDescriptor.create()
			.refId("3f7e0b7e-aefd-4efd-a946-7bdad142fd50")
			.get();
		target.unit = UnitDescriptor.create()
			.refId("some-other-unit-id")
			.name("m3")
			.get();

		var map = new FlowMap();
		map.entries.add(new FlowMapEntry(source, target, 1000));

		// write & read
		var tmpFile = Files.createTempFile(
				"_olca_" + getClass().getSimpleName(), ".csv").toFile();
		FlowMap.toCsv(map, tmpFile);
		map = FlowMap.fromCsv(tmpFile);
		assertTrue(tmpFile.delete());

		// check it
		var index = map.index();
		var e = index.get("06d4812b-6937-4d64-8517-b69aabce3648");
		assertEquals("3f7e0b7e-aefd-4efd-a946-7bdad142fd50",
				e.targetFlow().flow.refId);
		assertEquals(1000.0, e.factor(), 1e-16);
		assertNull(e.sourceFlow().status);
		assertNull(e.targetFlow().status);

		// check units
		assertEquals("some-unit-id", e.sourceFlow().unit.refId);
		assertEquals("kg", e.sourceFlow().unit.name);
		assertEquals("some-other-unit-id", e.targetFlow().unit.refId);
		assertEquals("m3", e.targetFlow().unit.name);
	}
}
