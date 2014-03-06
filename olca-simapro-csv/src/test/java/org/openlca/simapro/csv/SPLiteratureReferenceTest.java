package org.openlca.simapro.csv;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.refdata.SPLiteratureReference;
import org.openlca.simapro.csv.reader.BlockReader;
import org.openlca.simapro.csv.reader.BlockUnmarshaller;

public class SPLiteratureReferenceTest {

	//@formatter:off
	private String text = "" +
			"\n" +
			"Literature reference\n" +
			"\n" +
			"Name\n" +
			"Ecoinvent 3\n" +
			"\n" +
			"Documentation link\n" +
			"http://www.ecoinvent.org\n" +
			"\n" +
			"Category\n" +
			"Ecoinvent\n" +
			"\n" +
			"Description\n" +
			"\n" +
			"\n" +
			"End";
	//@formatter:on

	@Test
	public void testUnmarshallBlock() throws Exception {
		BlockReader reader = new BlockReader(new StringReader(text));
		Block block = reader.read();
		reader.close();
		BlockUnmarshaller unmarshaller = new BlockUnmarshaller();
		SPLiteratureReference reference = unmarshaller.unmarshall(block,
				SPLiteratureReference.class, ";");
		Assert.assertEquals("Ecoinvent 3", reference.getName());
		Assert.assertEquals("http://www.ecoinvent.org",
				reference.getDocumentationLink());
		Assert.assertEquals("Ecoinvent", reference.getCategory());
		Assert.assertNull(reference.getDescription());
	}

}
