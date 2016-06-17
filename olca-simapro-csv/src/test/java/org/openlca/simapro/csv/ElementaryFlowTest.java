package org.openlca.simapro.csv;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.io.BlockReader;
import org.openlca.simapro.csv.io.BlockUnmarshaller;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.refdata.AirEmissionBlock;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;

public class ElementaryFlowTest {

	//@formatter:off
    private String text = "" + 
            "Airborne emissions\n" +
			"1-Butanol;kg;000071-36-3;Formula: C4H10O \u007F\n" +
			"1-Pentene;kg;000109-67-1";
    //@formatter:on

	@Test
	public void testUnmarshallBlock() throws Exception {
		BlockReader reader = new BlockReader(new StringReader(text));
		Block block = reader.read();
		reader.close();
		AirEmissionBlock model = new BlockUnmarshaller(CsvConfig.getDefault())
				.unmarshall(block, AirEmissionBlock.class);
		List<ElementaryFlowRow> flows = model.getFlows();
		Assert.assertEquals(2, flows.size());
		Assert.assertEquals("1-Butanol", flows.get(0).getName());
		Assert.assertEquals("000109-67-1", flows.get(1).getCASNumber());
	}
}
