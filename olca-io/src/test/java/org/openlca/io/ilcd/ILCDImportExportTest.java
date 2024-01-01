package org.openlca.io.ilcd;

import java.io.File;
import java.nio.file.Files;

import jakarta.xml.bind.JAXB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.MemDataStore;
import org.openlca.ilcd.io.ZipStore;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.io.Tests;
import org.openlca.io.ilcd.input.ContactImport;
import org.openlca.io.ilcd.input.FlowImport;
import org.openlca.io.ilcd.input.FlowPropertyImport;
import org.openlca.io.ilcd.input.ImportConfig;
import org.openlca.io.ilcd.input.ProcessImport;
import org.openlca.io.ilcd.input.SourceImport;
import org.openlca.io.ilcd.input.UnitGroupImport;
import org.openlca.io.ilcd.output.ActorExport;
import org.openlca.io.ilcd.output.FlowExport;
import org.openlca.io.ilcd.output.FlowPropertyExport;
import org.openlca.io.ilcd.output.Export;
import org.openlca.io.ilcd.output.ProcessExport;
import org.openlca.io.ilcd.output.SourceExport;
import org.openlca.io.ilcd.output.UnitGroupExport;

import static org.junit.Assert.*;

/**
 * In order to run the tests the reference data must be contained in the
 * database of the test session.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ILCDImportExportTest {

	private static ImportConfig importConf;
	private static Export export;
	private static File zip;
	private static DataStore store;

	@BeforeClass
	public static void setUp() throws Exception {
		zip = Files.createTempFile("_olca_ilcd_export_test", ".zip").toFile();
		assertTrue(zip.delete());
		store = new ZipStore(zip);
		importConf = new ImportConfig(new MemDataStore(), Tests.getDb());
		export = new Export(Tests.getDb(), store);
		put("contact.xml",
			Contact.class);
		put("source.xml", Source.class);
		put("unit.xml",
			UnitGroup.class);
		put("flowproperty.xml",
			FlowProperty.class);
		put("flow.xml", Flow.class);
		put("process.xml",
			Process.class);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		store.close();
		assertTrue(zip.delete());
	}

	private static <T extends IDataSet> void put(String file, Class<T> clazz) {
		var in = ILCDImportExportTest.class.getResourceAsStream(file);
		assertNotNull(in);
		T obj = JAXB.unmarshal(in, clazz);
		importConf.store().put(obj);
	}

	@Test
	public void testA_Contact() {
		var id = "177ca340-ffa2-11da-92e3-0800200c9a66";
		var contact = importConf.store().get(Contact.class, id);
		var actor = new ContactImport(importConf).run(contact);
		assertEquals(id, actor.refId);
		new ActorExport(export).run(actor);
		assertTrue(store.contains(Contact.class, id));
	}

	@Test
	public void testB_Source() {
		var id = "2c699413-f88b-4cb5-a56d-98cb4068472f";
		var dataSet = importConf.store().get(Source.class, id);
		var source = new SourceImport(importConf).run(dataSet);
		assertEquals(id, source.refId);
		new SourceExport(export).run(source);
		assertTrue(store.contains(Source.class, id));
	}

	@Test
	public void testC_Units() {
		String id = "93a60a57-a4c8-11da-a746-0800200c9a66";
		var dataSet = importConf.store().get(UnitGroup.class, id);
		var group = new UnitGroupImport(importConf).run(dataSet);
		assertEquals(id, group.refId);
		new UnitGroupExport(export).run(group);
		assertTrue(store.contains(UnitGroup.class, id));
	}

	@Test
	public void testD_FlowProp() {
		String id = "93a60a56-a3c8-11da-a746-0800200b9a66";
		var dataSet = importConf.store().get(FlowProperty.class, id);
		var prop = new FlowPropertyImport(importConf).run(dataSet);
		assertEquals(id, prop.refId);
		new FlowPropertyExport(export).run(prop);
		assertTrue(store.contains(FlowProperty.class, id));
	}

	@Test
	public void testE_Flow() {
		String id = "0d7a3ad1-6556-11dd-ad8b-0800200c9a66";
		var dataSet = importConf.store().get(Flow.class, id);
		var syncFlow = new FlowImport(importConf).run(dataSet);
		assertEquals(id, syncFlow.flow().refId);
		new FlowExport(export).run(syncFlow.flow());
		assertTrue(store.contains(Flow.class, id));
	}

	@Test
	public void testF_Process() {
		String id = "76d6aaa4-37e2-40b2-994c-03292b600074";
		var dataSet = importConf.store().get(Process.class, id);
		var process = new ProcessImport(importConf).run(dataSet);
		assertEquals(id, process.refId);
		new ProcessExport(export).run(process);
		assertTrue(store.contains(Process.class, id));
	}

}
