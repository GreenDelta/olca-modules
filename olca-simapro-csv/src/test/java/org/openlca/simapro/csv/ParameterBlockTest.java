package org.openlca.simapro.csv;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.simapro.csv.io.BlockReader;
import org.openlca.simapro.csv.io.ModelReader;
import org.openlca.simapro.csv.model.refdata.DatabaseCalculatedParameterBlock;
import org.openlca.simapro.csv.model.refdata.DatabaseInputParameterBlock;
import org.openlca.simapro.csv.model.refdata.ProjectCalculatedParameterBlock;
import org.openlca.simapro.csv.model.refdata.ProjectInputParameterBlock;

public class ParameterBlockTest {

	private BlockReader blockReader;

	@Before
	public void setUp() throws Exception {
		InputStream is = this.getClass().getResourceAsStream(
				"simple_process.csv");
		InputStreamReader reader = new InputStreamReader(is);
		blockReader = new BlockReader(reader);
	}

	@After
	public void tearDown() throws Exception {
		blockReader.close();
	}

	@Test
	public void testReadDatabaseCalculatedParameterBlock() throws Exception {
		ModelReader modelReader = new ModelReader(blockReader,
				CsvConfig.getDefault(), DatabaseCalculatedParameterBlock.class);
		DatabaseCalculatedParameterBlock block = (DatabaseCalculatedParameterBlock) modelReader
				.read();
		assertEquals(1, block.getParameters().size());
		assertEquals("db_calc_param", block.getParameters().get(0).getName());
		modelReader.close();
	}

	@Test
	public void testReadDatabaseInputParameterBlock() throws Exception {
		ModelReader modelReader = new ModelReader(blockReader,
				CsvConfig.getDefault(), DatabaseInputParameterBlock.class);
		DatabaseInputParameterBlock block = (DatabaseInputParameterBlock) modelReader
				.read();
		assertEquals(1, block.getParameters().size());
		assertEquals("db_input_param", block.getParameters().get(0).getName());
		modelReader.close();
	}

	@Test
	public void testReadProjectCalculatedParameterBlock() throws Exception {
		ModelReader modelReader = new ModelReader(blockReader,
				CsvConfig.getDefault(), ProjectCalculatedParameterBlock.class);
		ProjectCalculatedParameterBlock block = (ProjectCalculatedParameterBlock) modelReader
				.read();
		assertEquals(1, block.getParameters().size());
		assertEquals("proj_calc_param", block.getParameters().get(0).getName());
		modelReader.close();
	}

	@Test
	public void testReadProjectInputParameterBlock() throws Exception {
		ModelReader modelReader = new ModelReader(blockReader,
				CsvConfig.getDefault(), ProjectInputParameterBlock.class);
		ProjectInputParameterBlock block = (ProjectInputParameterBlock) modelReader
				.read();
		assertEquals(1, block.getParameters().size());
		assertEquals("proj_input_param", block.getParameters().get(0).getName());
		modelReader.close();
	}
}
