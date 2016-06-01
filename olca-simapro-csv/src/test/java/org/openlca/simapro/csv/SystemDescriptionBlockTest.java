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
		assertEquals("system name", block.getName());
		assertEquals("Others", block.getCategory());
		assertEquals("text for description", block.getDescription());
		assertEquals("text for sub-systems", block.getSubSystems());
		assertEquals("text for cut-off rules", block.getCutOffRules());
		assertEquals("text for energy model", block.getEnergyModel());
		assertEquals("text for transport model", block.getTransportModel());
		assertEquals("text for waste model", block.getWasteModel());
		assertEquals("text for other assumptions", block.getOtherAssumptions());
		assertEquals("text for other information", block.getOtherInformation());
		assertEquals("text for allocation rules", block.getAllocationRules());
	}

}
