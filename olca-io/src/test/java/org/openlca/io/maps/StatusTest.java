package org.openlca.io.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class StatusTest {

	@Test
	public void testFromToString() {
		int[] states = { Status.OK, Status.WARNING, Status.ERROR };
		String[] messages = { "is ok", "a warning", "some error" };
		List<Status> list = Arrays.stream(new int[] { 0, 1, 2 })
				.mapToObj(i -> new Status(states[i], messages[i]))
				.map(status -> status.toString())
				.map(s -> Status.fromString(s))
				.collect(Collectors.toList());
		for (int i = 0; i < states.length; i++) {
			assertEquals(states[i], list.get(i).type);
			assertEquals(messages[i], list.get(i).message);
		}

		assertNull(Status.fromString(null));
		assertNull(Status.fromString(""));
	}

	@Test
	public void testCsvIO() throws Exception {
		FlowMap map = new FlowMap();
		FlowMapEntry e = new FlowMapEntry();
		e.sourceFlow = new FlowRef();
		e.sourceFlow.flow = new FlowDescriptor();
		e.sourceFlow.flow.refId = "source";
		e.sourceFlow.status = Status.warn("the source flow");

		e.targetFlow = new FlowRef();
		e.targetFlow.flow = new FlowDescriptor();
		e.targetFlow.flow.refId = "target";
		e.targetFlow.status = Status.error("the target flow");
		e.factor = 42.0;
		map.entries.add(e);

		File tmpFile = Files.createTempFile(
				"_olca_" + getClass().getSimpleName(), ".csv").toFile();
		FlowMap.toCsv(map, tmpFile);
		map = FlowMap.fromCsv(tmpFile);
		e = map.entries.get(0);
		tmpFile.delete();

		assertEquals(Status.warn("the source flow"), e.sourceFlow.status);
		assertEquals(Status.error("the target flow"), e.targetFlow.status);
	}

}
