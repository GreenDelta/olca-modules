package org.openlca.simapro.csv;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.io.BlockReader;
import org.openlca.simapro.csv.model.Block;

public class BlockReaderTest {

	//@formatter:off
	private String content = ""
			 + "{SimaPro 8.0}\n"
			 + "{processes}\n"
			 + "{Date: 05.03.2014}\n"
			 + "{Time: 09:27:45}\n"
			 + "{Project: Test}\n"
			 + "{CSV Format version: 7.0.0}\n"
			 + "{CSV separator: Semicolon}\n"
			 + "{Decimal separator: ,}\n"
			 + "{Date separator: .}\n"
			 + "{Short date format: dd.MM.yyyy}\n"
			 + "\n"
			 + "\n"
			 + "Process\n"
			 + "\n"	
			 + "Category type\n"
			 + "material\n"
			 + "\n"	
			 + "Process identifier\n"
			 + "DefaultX25250700002\n"
			 + "\n"	
			 + "Type\n"
			 + "Unit process\n"
			 + "\n"	
			 + "End\n"
			 + "\n"
			 + "Quantities\n"
			 + "Mass;Yes\n"
			 + "Length;Yes\n"
			 + "\n"
			 + "End\n";	
	//@formatter:on

	@Test
	public void testReader() throws Exception {
		StringReader s = new StringReader(content);
		int i = 0;
		try (BlockReader reader = new BlockReader(s)) {
			Block block = null;
			while ((block = reader.read()) != null) {
				if (i == 0)
					checkProcessBlock(block);
				else
					checkQuantityBlock(block);
				i++;
			}
		}
		Assert.assertEquals(i, 2);
	}

	private void checkQuantityBlock(Block block) {
		Assert.assertEquals("Quantities", block.header);
		Assert.assertEquals(2, block.dataRows.size());
		Assert.assertEquals("Mass;Yes", block.dataRows.get(0));
		Assert.assertEquals("Length;Yes", block.dataRows.get(1));
	}

	private void checkProcessBlock(Block block) {
		Assert.assertEquals("Process", block.header);
		Assert.assertEquals(3, block.getSections().size());
		Assert.assertEquals("material", block.getSection("Category type").dataRows.get(0));
		Assert.assertEquals("DefaultX25250700002",
				block.getSection("Process identifier").dataRows.get(0));
		Assert.assertEquals("Unit process", block.getSection("Type").dataRows.get(0));
	}

}
