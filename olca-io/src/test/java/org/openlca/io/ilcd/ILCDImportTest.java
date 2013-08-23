package org.openlca.io.ilcd;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.MemDataStore;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.io.TestSession;
import org.openlca.io.ilcd.input.ContactImport;
import org.openlca.io.ilcd.input.FlowImport;
import org.openlca.io.ilcd.input.FlowPropertyImport;
import org.openlca.io.ilcd.input.ProcessImport;
import org.openlca.io.ilcd.input.SourceImport;
import org.openlca.io.ilcd.input.UnitGroupImport;

/**
 * In order to run the tests the reference data must be contained in the
 * database of the test session.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ILCDImportTest {

	private static DataStore ilcdStore;
	private IDatabase database = TestSession.getDerbyDatabase();

	@BeforeClass
	public static void setUp() throws Exception {
		ilcdStore = new MemDataStore();
		put("contact.xml", "177ca340-ffa2-11da-92e3-0800200c9a66",
				Contact.class);
		put("source.xml", "2c699413-f88b-4cb5-a56d-98cb4068472f", Source.class);
		put("unit.xml", "59f191d6-5dd3-4553-af88-1a32accfe308", UnitGroup.class);
		put("flowproperty.xml", "93a60a56-a3c8-14da-a746-0800200c9a66",
				FlowProperty.class);
		put("flow.xml", "0d7a3ad1-6556-11dd-ad8b-0800200c9a66", Flow.class);
		put("process.xml", "76d6aaa4-37e2-40b2-994c-03292b600074",
				Process.class);
	}

	private static <T> void put(String file, String id, Class<T> clazz)
			throws Exception {
		InputStream in = ILCDImportTest.class.getResourceAsStream(file);
		T obj = JAXB.unmarshal(in, clazz);
		ilcdStore.put(obj, id);
	}

	@Test
	public void testA_importContact() throws Exception {
		String id = "177ca340-ffa2-11da-92e3-0800200c9a66";
		ContactImport contactImport = new ContactImport(ilcdStore, database);
		Actor actor = contactImport.run(ilcdStore.get(Contact.class, id));
		Assert.assertEquals(id, actor.getRefId());
	}

	@Test
	public void testB_importSource() throws Exception {
		String id = "2c699413-f88b-4cb5-a56d-98cb4068472f";
		SourceImport sourceImport = new SourceImport(ilcdStore, database);
		org.openlca.core.model.Source source = sourceImport.run(ilcdStore.get(
				Source.class, id));
		Assert.assertEquals(id, source.getRefId());
	}

	@Test
	public void testC_importUnits() throws Exception {
		String id = "59f191d6-5dd3-4553-af88-1a32accfe308";
		UnitGroupImport unitImport = new UnitGroupImport(ilcdStore, database);
		org.openlca.core.model.UnitGroup group = unitImport.run(ilcdStore.get(
				UnitGroup.class, id));
		Assert.assertEquals(id, group.getRefId());
	}

	@Test
	public void testD_importFlowProp() throws Exception {
		String id = "93a60a56-a3c8-14da-a746-0800200c9a66";
		FlowPropertyImport propImport = new FlowPropertyImport(ilcdStore,
				database);
		org.openlca.core.model.FlowProperty prop = propImport.run(ilcdStore
				.get(FlowProperty.class, id));
		Assert.assertEquals(id, prop.getRefId());
	}

	@Test
	public void testE_importFlow() throws Exception {
		String id = "0d7a3ad1-6556-11dd-ad8b-0800200c9a66";
		FlowImport flowImport = new FlowImport(ilcdStore, database);
		org.openlca.core.model.Flow flow = flowImport.run(ilcdStore.get(
				Flow.class, id));
		Assert.assertEquals(id, flow.getRefId());
	}

	@Test
	public void testF_importProcess() throws Exception {
		String id = "76d6aaa4-37e2-40b2-994c-03292b600074";
		ProcessImport processImport = new ProcessImport(ilcdStore, database);
		org.openlca.core.model.Process process = processImport.run(ilcdStore
				.get(Process.class, id));
		Assert.assertEquals(id, process.getRefId());
	}

}
