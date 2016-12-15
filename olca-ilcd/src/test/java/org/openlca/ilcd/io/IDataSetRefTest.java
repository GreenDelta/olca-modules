package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

public class IDataSetRefTest {

	@Test
	public void testSource() throws Exception {
		with("sdk_sample_source.xml", Source.class, ref -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ref.uuid);
			assertEquals("00.00", ref.version);
			assertEquals(DataSetType.SOURCE, ref.type);
			assertEquals("shortName0", ref.name.get(0).value.trim());
			assertEquals("http://www.ilcd-network.org/data/processes/sample_source.xml",
					ref.uri.trim());
		});
	}

	@Test
	public void testContact() throws Exception {
		with("sdk_sample_contact.xml", Contact.class, ref -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ref.uuid);
			assertEquals("00.00", ref.version);
			assertEquals("name0", ref.name.get(0).value.trim());
			assertEquals(DataSetType.CONTACT, ref.type);
			assertEquals("http://www.ilcd-network.org/data/processes/sample_contact.xml",
					ref.uri.trim());
		});
	}

	@Test
	public void testUnitGroup() throws Exception {
		with("sdk_sample_unitgroup.xml", UnitGroup.class, ref -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ref.uuid);
			assertEquals("00.00", ref.version);
			assertEquals("name0", ref.name.get(0).value.trim());
			assertEquals(DataSetType.UNIT_GROUP, ref.type);
			assertEquals("http://www.ilcd-network.org/data/processes/sample_unitgroup.xml",
					ref.uri.trim());
		});
	}

	@Test
	public void testFlowProperty() throws Exception {
		with("sdk_sample_flowproperty.xml", FlowProperty.class, ref -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ref.uuid);
			assertEquals("00.00", ref.version);
			assertEquals("name0", ref.name.get(0).value.trim());
			assertEquals(DataSetType.FLOW_PROPERTY, ref.type);
			assertEquals("http://www.ilcd-network.org/data/processes/sample_flowproperty.xml",
					ref.uri.trim());
		});
	}

	@Test
	public void testFlow() throws Exception {
		with("sdk_sample_flow.xml", Flow.class, ref -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ref.uuid);
			assertEquals("00.00", ref.version);
			assertEquals("baseName0", ref.name.get(0).value.trim());
			assertEquals(DataSetType.FLOW, ref.type);
			assertEquals("http://www.ilcd-network.org/data/processes/sample_flow.xml",
					ref.uri.trim());
		});
	}

	@Test
	public void testProcess() throws Exception {
		with("sdk_sample_process.xml", org.openlca.ilcd.processes.Process.class, ref -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ref.uuid);
			assertEquals("00.00", ref.version);
			assertEquals("baseName0", ref.name.get(0).value.trim());
			assertEquals(DataSetType.PROCESS, ref.type);
			assertEquals("http://www.ilcd-network.org/data/processes/sample_process.xml",
					ref.uri.trim());
		});
	}

	@Test
	public void testMethod() throws Exception {
		with("sdk_sample_lciamethod.xml", LCIAMethod.class, ref -> {
			assertEquals("00000000-0000-0000-0000-000000000000", ref.uuid);
			assertEquals("00.00", ref.version);
			assertEquals("name0", ref.name.get(0).value.trim());
			assertEquals(DataSetType.LCIA_METHOD, ref.type);
			assertEquals("http://www.ilcd-network.org/data/lciamethods/sample_lciamethod.xml", ref.uri.trim());
		});
	}

	private void with(String xml, Class<?> type, Consumer<Ref> fn)
			throws Exception {
		try (InputStream is = getClass().getResourceAsStream(xml)) {
			Object o = JAXB.unmarshal(is, type);
			IDataSet ds = (IDataSet) o;
			fn.accept(Ref.of(ds));
		}
	}
}
