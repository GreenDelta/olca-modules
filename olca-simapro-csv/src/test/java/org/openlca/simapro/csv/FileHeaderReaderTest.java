package org.openlca.simapro.csv;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.io.FileHeaderReader;
import org.openlca.simapro.csv.model.FileHeader;

public class FileHeaderReaderTest {

	//@formatter:off
	private String header = ""
		 + "{SimaPro 8.0}\n"
		 + "{processes}\n"
		 + "{Date: 05.03.2014}\n"
		 + "{Time: 09:27:45}\n"
		 + "{Project: Test}\n"
		 + "{CSV Format version: 7.0.0}\n"
		 + "{CSV separator: Semicolon}\n"
		 + "{Decimal separator: ,}\n"
		 + "{Date separator: .}\n"
		 + "{Short date format: dd.MM.yyyy}\n";	
	//@formatter:on

	@Test
	public void testRead() throws Exception {
		StringReader reader = new StringReader(header);
		FileHeader header = new FileHeaderReader(reader).read();
		Assert.assertEquals("SimaPro 8.0", header.getSimaProVersion());
		Assert.assertEquals("processes", header.getContentType());
		Assert.assertEquals("05.03.2014", header.getDate());
		Assert.assertEquals("09:27:45", header.getTime());
		Assert.assertEquals("Test", header.getProject());
		Assert.assertEquals("7.0.0", header.getFormatVersion());
		Assert.assertEquals("Semicolon", header.getCsvSeparator());
		Assert.assertEquals(",", header.getDecimalSeparator());
		Assert.assertEquals(".", header.getDateSeparator());
		Assert.assertEquals("dd.MM.yyyy", header.getShortDateFormat());
	}
}
