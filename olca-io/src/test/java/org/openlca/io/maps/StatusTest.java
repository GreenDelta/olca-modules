package org.openlca.io.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class StatusTest {

	@Test
	public void testFromToString() {
		int[] states = {
			MappingStatus.OK,
			MappingStatus.WARNING,
			MappingStatus.ERROR };
		String[] messages = {
			"is ok",
			"a warning",
			"some error" };
		List<MappingStatus> list = Arrays.stream(new int[]{0, 1, 2})
			.mapToObj(i -> new MappingStatus(states[i], messages[i]))
			.map(MappingStatus::toString)
			.map(MappingStatus::fromString)
			.toList();
		for (int i = 0; i < states.length; i++) {
			assertEquals(states[i], list.get(i).type());
			assertEquals(messages[i], list.get(i).message());
		}

		assertTrue(MappingStatus.fromString(null).isEmpty());
		assertTrue(MappingStatus.fromString("").isEmpty());
	}

	@Test
	public void testCsvIO() throws Exception {
		var sourceFlow = new FlowRef();
		sourceFlow.flow = new FlowDescriptor();
		sourceFlow.flow.refId = "source";
		sourceFlow.status = MappingStatus.warn("the source flow");

		var targetFlow = new FlowRef();
		targetFlow.flow = new FlowDescriptor();
		targetFlow.flow.refId = "target";
		targetFlow.status = MappingStatus.error("the target flow");

		FlowMap map = new FlowMap();
		map.entries.add(new FlowMapEntry(sourceFlow, targetFlow, 42));

		File tmpFile = Files.createTempFile(
				"_olca_" + getClass().getSimpleName(), ".csv").toFile();
		FlowMap.toCsv(map, tmpFile);
		map = FlowMap.fromCsv(tmpFile);
		var e = map.entries.get(0);
		assertTrue(tmpFile.delete());

		assertEquals(MappingStatus.warn("the source flow"), e.sourceFlow().status);
		assertEquals(MappingStatus.error("the target flow"), e.targetFlow().status);
	}

}
