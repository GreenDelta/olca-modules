package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.lists.CategorySystem;

public class CategorySystemTest {

	@Test
	public void testRead() {
		InputStream is = getClass().getResourceAsStream(
				"sdk_sample_classification.xml");
		CategorySystem system = JAXB.unmarshal(is, CategorySystem.class);
		assertEquals("ILCD", system.name);
		assertEquals(7, system.categories.size());
	}

}
