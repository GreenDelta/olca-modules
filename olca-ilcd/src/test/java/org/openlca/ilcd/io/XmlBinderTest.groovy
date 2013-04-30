package org.openlca.ilcd.file.io

import static org.junit.Assert.*

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.XmlBinder
import org.openlca.ilcd.processes.DataSetInformation
import org.openlca.ilcd.processes.Process
import org.openlca.ilcd.processes.ProcessInformation
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

class XmlBinderTest {

	private def instances = []
	private static def binder = new XmlBinder()

	@Before
	void makeInstances() {
		instances << makeProcess()  << new Flow()  << new FlowProperty() \
		<< new UnitGroup() << new Source() << new Contact()
	}

	@Test
	void testFileIO() {
		runTests {orig, file ->
			binder.toFile(orig, file)
			return binder.fromFile(orig.getClass(), file)
		}
	}

	@Test
	void testStreamIO() {
		runTests {orig, file ->
			def os = new FileOutputStream(file)
			binder.toStream(orig, os)
			def is = new FileInputStream(file)
			return binder.fromStream(orig.getClass(), is)
		}
	}

	@Test
	void testReaderWriterIO() {
		runTests { orig, file ->
			def writer = new FileWriter(file)
			binder.toWriter(orig, writer)
			def reader = new FileReader(file)
			return binder.fromReader(orig.getClass(), reader)
		}
	}

	private void runTests(Closure clos) {
		instances.each { orig ->
			def file = makeFile()
			def copy = clos(orig, file)
			assertEquals(orig.getClass(), copy.getClass())
			assertTrue file.delete()
		}
	}

	private File makeFile() {
		def tempFolder = new File(System.getProperty("java.io.tmpdir"))
		def fileName = "000_ilcd_" + UUID.randomUUID() + ".xml"
		def file = new File(tempFolder, fileName)
	}

	private def makeProcess() {
		def process = new Process()
		def pi = new ProcessInformation()
		def info = new DataSetInformation()
		process.processInformation = pi
		pi.dataSetInformation = info
		info.uuid = UUID.randomUUID().toString()
		return process;
	}
}
