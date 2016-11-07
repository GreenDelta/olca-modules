package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.SourceBag;

public class SourceBagTest {

	private SourceBag bag;

	@Before
	public void setUp() throws Exception {
		try (InputStream stream = this.getClass().getResourceAsStream(
				"source.xml")) {
			XmlBinder binder = new XmlBinder();
			Source source = binder.fromStream(Source.class, stream);
			bag = new SourceBag(source, "en");
		}
	}

	@Test
	public void testGetId() {
		assertEquals("2c699413-f88b-4cb5-a56d-98cb4068472f", bag.getId());
	}

	@Test
	public void testGetShortName() {
		assertEquals("IMA-Europe_Plastic_Clay_diagramme_"
				+ "2c699413-f88b-4cb5-a56d-98cb4068472f.jpg", bag
						.getShortName().trim());
	}

	@Test
	public void testGetComment() {
		assertNull(bag.getComment());
	}

	@Test
	public void testGetSourceCitation() {
		assertEquals("GaBi database", bag.getSourceCitation());
	}

	@Test
	public void testGetSortedClasses() {
		List<Category> classes = bag.getSortedClasses();
		assertTrue(classes.size() == 1);
		assertEquals("Images", classes.get(0).value);
	}

}
