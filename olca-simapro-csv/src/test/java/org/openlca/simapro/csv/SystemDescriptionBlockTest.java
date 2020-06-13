package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.simapro.csv.io.BlockReader;
import org.openlca.simapro.csv.io.ModelReader;
import org.openlca.simapro.csv.model.refdata.SystemDescriptionBlock;

public class SystemDescriptionBlockTest {

	private ModelReader reader;

	@Before
	public void setUp() throws Exception {
		InputStream in = this.getClass().getResourceAsStream(
				"simple_process.csv");
		InputStreamReader isReader = new InputStreamReader(in);
		BlockReader blockReader = new BlockReader(isReader);
		reader = new ModelReader(blockReader, CsvConfig.getDefault(),
				SystemDescriptionBlock.class);
	}

	@After
	public void tearDown() throws Exception {
		reader.close();
	}

	@Test
	public void testReadBlock() throws Exception {
		SystemDescriptionBlock block = (SystemDescriptionBlock) reader.read();
		assertEquals("system name", block.name);
		assertEquals("Others", block.category);
		assertEquals("text for description", block.description);
		assertEquals("text for sub-systems", block.subSystems);
		assertEquals("text for cut-off rules", block.cutOffRules);
		assertEquals("text for energy model", block.energyModel);
		assertEquals("text for transport model", block.transportModel);
		assertEquals("text for waste model", block.wasteModel);
		assertEquals("text for other assumptions", block.otherAssumptions);
		assertEquals("text for other information", block.otherInformation);
		assertEquals("text for allocation rules", block.allocationRules);
	}

}
