package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.sources.Source;

public class IDataSetTest {

	@Test
	public void testSource() throws Exception {
		with("sdk_sample_source.xml", Source.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.SOURCE, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_source.xml",
					ds.getURI().trim());
		});
	}

	@Test
	public void testContact() throws Exception {
		with("sdk_sample_contact.xml", Contact.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.CONTACT, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_contact.xml",
					ds.getURI().trim());
		});
	}

	private void with(String xml, Class<?> type, Consumer<IDataSet> fn)
			throws Exception {
		try (InputStream is = getClass().getResourceAsStream(xml)) {
			Object o = JAXB.unmarshal(is, type);
			IDataSet ds = (IDataSet) o;
			fn.accept(ds);
		}
	}

}
