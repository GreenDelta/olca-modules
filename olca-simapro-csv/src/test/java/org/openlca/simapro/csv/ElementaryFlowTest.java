package org.openlca.simapro.csv;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.refdata.AirEmission;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.BlockUnmarshaller;

import java.io.StringReader;
import java.util.List;

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
		List<AirEmission> rows = new BlockUnmarshaller().unmarshallRows(block,
				AirEmission.class, ";");
		Assert.assertEquals(2, rows.size());
		Assert.assertEquals("1-Butanol", rows.get(0).getName());
		Assert.assertEquals("000109-67-1", rows.get(1).getCASNumber());
	}
}
