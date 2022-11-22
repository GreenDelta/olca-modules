package org.openlca.core.matrix.io.index;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.FlowType;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class IxTest {

	@Test
	public void testEnviIndex() throws IOException  {
		var item = new IxEnviItem(
				42,
				true,
				new IxFlow(
						"flow-id",
						"flow-name",
						"flow-category",
						"flow_unit",
						FlowType.WASTE_FLOW),
				new IxLocation(
						"loc-id",
						"loc-name",
						"loc-code")
		);
		var temp = Files.createTempFile("_ix", ".csv").toFile();
		new IxEnviIndex(List.of(item)).toCsv(temp);
		var enviIdx = IxEnviIndex.readFrom(temp);
		assertEquals(1, enviIdx.size());

		var copy = enviIdx.items().get(0);
		assertEquals(42, copy.index());
		assertTrue(copy.isInput());
		assertEquals(item.flow(), copy.flow());
		assertEquals(item.location(), copy.location());

		Files.delete(temp.toPath());
	}
}
