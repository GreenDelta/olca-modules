package org.openlca.simapro.csv;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.simapro.csv.model.enums.ProcessCategory;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.ModelReader;

public class ProcessBlockTest {

	private ProcessBlock block;

	@Before
	public void setUp() throws Exception {
		try (InputStream is = this.getClass().getResourceAsStream(
				"simple_process.csv");
				InputStreamReader reader = new InputStreamReader(is);
				BlockReader blockReader = new BlockReader(reader);
				ModelReader modelReader = new ModelReader(blockReader,
						CsvConfig.getDefault(), ProcessBlock.class)) {
			this.block = (ProcessBlock) modelReader.read();
		}
	}

	@Test
	public void testTextEntries() {

	}

	@Test
	public void testEnumEntries() {
		Assert.assertEquals(ProcessCategory.MATERIAL, block.getCategory());
	}

}
