package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

public class IDataSetTest {

	@Test
	public void testSource() throws Exception {
		with("sdk_sample_source.xml", Source.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.SOURCE, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_source.xml",
					ds.getURI().trim());
			assertEquals(2, ds.getClassifications().size());
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
			assertEquals(2, ds.getClassifications().size());
		});
	}

	@Test
	public void testUnitGroup() throws Exception {
		with("sdk_sample_unitgroup.xml", UnitGroup.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.UNIT_GROUP, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_unitgroup.xml",
					ds.getURI().trim());
			assertEquals(2, ds.getClassifications().size());
		});
	}

	@Test
	public void testFlowProperty() throws Exception {
		with("sdk_sample_flowproperty.xml", FlowProperty.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.FLOW_PROPERTY, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_flowproperty.xml",
					ds.getURI().trim());
			assertEquals(2, ds.getClassifications().size());
		});
	}

	@Test
	public void testFlow() throws Exception {
		with("sdk_sample_flow.xml", Flow.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.FLOW, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_flow.xml",
					ds.getURI().trim());
			assertEquals(2, ds.getClassifications().size());
		});
	}

	@Test
	public void testProcess() throws Exception {
		with("sdk_sample_process.xml", org.openlca.ilcd.processes.Process.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.PROCESS, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_process.xml",
					ds.getURI().trim());
			assertEquals(2, ds.getClassifications().size());
		});
	}

	@Test
	public void testMethod() throws Exception {
		with("sdk_sample_lciamethod.xml", LCIAMethod.class, ds -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ds.getUUID());
			assertEquals("00.00", ds.getVersion());
			assertEquals(DataSetType.LCIA_METHOD, ds.getDataSetType());
			assertEquals("http://www.ilcd-network.org/data/lciamethods/sample_lciamethod.xml", ds.getURI().trim());
			assertEquals(1, ds.getClassifications().size());
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
