package org.openlca.io.ilcd;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import javax.xml.bind.JAXB;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openlca.core.model.Actor;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
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
import org.openlca.io.ilcd.output.ExportConfig;
import org.openlca.io.ilcd.output.FlowExport;
import org.openlca.io.ilcd.output.FlowPropertyExport;
import org.openlca.io.ilcd.output.ProcessExport;
import org.openlca.io.ilcd.output.SourceExport;
import org.openlca.io.ilcd.output.UnitGroupExport;

/**
 * In order to run the tests the reference data must be contained in the
 * database of the test session.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ILCDImportExportTest {

	private static ImportConfig importConfig;
	private static ExportConfig exportConfig;
	private static File zip;

	@BeforeClass
	public static void setUp() throws Exception {
		zip = Files.createTempFile("_olca_ilcd_export_test", ".zip").toFile();
		zip.delete();
		ZipStore store = new ZipStore(zip);
		importConfig = new ImportConfig(new MemDataStore(), Tests.getDb());
		exportConfig = new ExportConfig(Tests.getDb(), store);
		put("contact.xml", "177ca340-ffa2-11da-92e3-0800200c9a66",
				Contact.class);
		put("source.xml", "2c699413-f88b-4cb5-a56d-98cb4068472f", Source.class);
		put("unit.xml", "93a60a57-a4c8-11da-a746-0800200c9a66", UnitGroup.class);
		put("flowproperty.xml", "93a60a56-a3c8-11da-a746-0800200b9a66",
				FlowProperty.class);
		put("flow.xml", "0d7a3ad1-6556-11dd-ad8b-0800200c9a66", Flow.class);
		put("process.xml", "76d6aaa4-37e2-40b2-994c-03292b600074",
				Process.class);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		exportConfig.store.close();
		zip.delete();
	}

	private static <T extends IDataSet> void put(String file, String id, Class<T> clazz)
			throws Exception {
		InputStream in = ILCDImportExportTest.class.getResourceAsStream(file);
		T obj = JAXB.unmarshal(in, clazz);
		importConfig.store.put(obj);
	}

	@Test
	public void testA_Contact() throws Exception {
		String id = "177ca340-ffa2-11da-92e3-0800200c9a66";
		ContactImport contactImport = new ContactImport(importConfig);
		Actor actor = contactImport.run(importConfig.store.get(Contact.class,
				id));
		Assert.assertEquals(id, actor.getRefId());
		ActorExport export = new ActorExport(exportConfig);
		export.run(actor);
		Assert.assertTrue(exportConfig.store.contains(Contact.class, id));
	}

	@Test
	public void testB_Source() throws Exception {
		String id = "2c699413-f88b-4cb5-a56d-98cb4068472f";
		SourceImport sourceImport = new SourceImport(importConfig);
		org.openlca.core.model.Source source = sourceImport
				.run(importConfig.store.get(Source.class, id));
		Assert.assertEquals(id, source.getRefId());
		SourceExport export = new SourceExport(exportConfig);
		export.run(source);
		Assert.assertTrue(exportConfig.store.contains(Source.class, id));
	}

	@Test
	public void testC_Units() throws Exception {
		String id = "93a60a57-a4c8-11da-a746-0800200c9a66";
		UnitGroupImport unitImport = new UnitGroupImport(importConfig);
		org.openlca.core.model.UnitGroup group = unitImport
				.run(importConfig.store.get(UnitGroup.class, id));
		Assert.assertEquals(id, group.getRefId());
		UnitGroupExport export = new UnitGroupExport(exportConfig);
		export.run(group);
		Assert.assertTrue(exportConfig.store.contains(UnitGroup.class, id));
	}

	@Test
	public void testD_FlowProp() throws Exception {
		String id = "93a60a56-a3c8-11da-a746-0800200b9a66";
		FlowPropertyImport propImport = new FlowPropertyImport(importConfig);
		org.openlca.core.model.FlowProperty prop = propImport
				.run(importConfig.store.get(FlowProperty.class, id));
		Assert.assertEquals(id, prop.getRefId());
		FlowPropertyExport export = new FlowPropertyExport(exportConfig);
		export.run(prop);
		Assert.assertTrue(exportConfig.store.contains(FlowProperty.class, id));
	}

	@Test
	public void testE_Flow() throws Exception {
		String id = "0d7a3ad1-6556-11dd-ad8b-0800200c9a66";
		FlowImport flowImport = new FlowImport(importConfig);
		org.openlca.core.model.Flow flow = flowImport.run(importConfig.store
				.get(Flow.class, id));
		Assert.assertEquals(id, flow.getRefId());
		FlowExport export = new FlowExport(exportConfig);
		export.run(flow);
		Assert.assertTrue(exportConfig.store.contains(Flow.class, id));
	}

	@Test
	public void testF_Process() throws Exception {
		String id = "76d6aaa4-37e2-40b2-994c-03292b600074";
		ProcessImport processImport = new ProcessImport(importConfig);
		org.openlca.core.model.Process process = processImport
				.run(importConfig.store.get(Process.class, id));
		Assert.assertEquals(id, process.getRefId());
		ProcessExport export = new ProcessExport(exportConfig);
		export.run(process);
		Assert.assertTrue(exportConfig.store.contains(Process.class, id));
	}

}
