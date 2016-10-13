package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

public class XmlBinderTest {

	private List<Object> instances = new ArrayList<>();
	private XmlBinder binder = new XmlBinder();

	@Before
	public void makeInstances() {
		instances.add(makeProcess());
		instances.add(new Flow());
		instances.add(new FlowProperty());
		instances.add(new UnitGroup());
		instances.add(new Source());
		instances.add(new Contact());
	}

	@Test
	public void testFileIO() throws Exception {
		runTests(new Fun() {
			@Override
			public Object copyWithIO(Object orgiginal, File file)
					throws Exception {
				binder.toFile(orgiginal, file);
				return binder.fromFile(orgiginal.getClass(), file);
			}
		});
	}

	@Test
	public void testStreamIO() throws Exception {
		runTests(new Fun() {
			@Override
			public Object copyWithIO(Object orgiginal, File file)
					throws Exception {
				FileOutputStream os = new FileOutputStream(file);
				binder.toStream(orgiginal, os);
				FileInputStream is = new FileInputStream(file);
				return binder.fromStream(orgiginal.getClass(), is);
			}
		});
	}

	@Test
	public void testReaderWriterIO() throws Exception {
		runTests(new Fun() {
			@Override
			public Object copyWithIO(Object orgiginal, File file)
					throws Exception {
				FileWriter writer = new FileWriter(file);
				binder.toWriter(orgiginal, writer);
				FileReader reader = new FileReader(file);
				return binder.fromReader(orgiginal.getClass(), reader);
			}
		});
	}

	private void runTests(Fun fun) throws Exception {
		for (Object orig : instances) {
			File file = makeFile();
			Object copy = fun.copyWithIO(orig, file);
			assertEquals(orig.getClass(), copy.getClass());
			assertTrue(file.delete());
		}
	}

	private File makeFile() {
		File tempFolder = new File(System.getProperty("java.io.tmpdir"));
		String fileName = "000_ilcd_" + UUID.randomUUID() + ".xml";
		return new File(tempFolder, fileName);
	}

	private Process makeProcess() {
		Process process = new Process();
		ProcessInfo pi = new ProcessInfo();
		DataSetInfo info = new DataSetInfo();
		process.processInfo = pi;
		pi.dataSetInfo = info;
		info.uuid = UUID.randomUUID().toString();
		return process;
	}

	private interface Fun {
		Object copyWithIO(Object orgiginal, File file) throws Exception;
	}
}
