package org.openlca.ilcd.file.io

import static org.junit.Assert.*

import java.io.StringWriter
import java.util.UUID

import org.junit.Test;
import org.openlca.ilcd.processes.Process
import org.openlca.ilcd.processes.ProcessInformation
import org.openlca.ilcd.processes.DataSetInformation
import org.openlca.ilcd.productmodel.ProductModel
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.ilcd.commons.*
import org.openlca.ilcd.io.XmlBinder

class ProductModelTest {

	private static XmlBinder binder = new XmlBinder()

	@Test
	void writeReadPlainProcess() {
		def process = makePlainProcess()
		def xml = marshal(process)
		def copy = unmarshal(xml)
		def uuid = {proc -> proc.processInformation.dataSetInformation.uuid}
		assertEquals uuid(process), uuid(copy)
	}

	@Test
	void writeReadProductModel() {
		def process = makeProductModel()
		def xml = marshal(process)
		def copy = unmarshal(xml)
		def copyName = copy.processInformation.other.any[0].name
		assertEquals "test-model", copyName
	}

	@Test
	void testNoModelInProcessBag() {
		def process = unmarshal(marshal(makePlainProcess()))
		def bag = new ProcessBag(process)
		assertFalse bag.hasProductModel()
		assertNull bag.getProductModel()
	}

	@Test
	void testModelInProcessBag() {
		def process = unmarshal(marshal(makeProductModel()))
		def bag = new ProcessBag(process)
		assertTrue bag.hasProductModel()
		assertEquals "test-model", bag.productModel.name
	}

	private def makePlainProcess() {
		def process = new Process()
		def procInfo = new ProcessInformation()
		process.processInformation = procInfo
		def dataSetInfo = new DataSetInformation()
		procInfo.dataSetInformation = dataSetInfo
		dataSetInfo.uuid = UUID.randomUUID().toString()
		return process
	}

	private def makeProductModel() {
		def process = makePlainProcess()
		def other = new Other()
		process.processInformation.other = other
		def extension = other.getAny()
		ProductModel model = new ProductModel()
		model.name = "test-model"
		extension.add(model)
		return process
	}

	private def marshal(def process) {
		def writer = new StringWriter()
		binder.toWriter(process, writer)
		return writer.toString()
	}

	private def unmarshal(def xml) {
		def reader = new StringReader(xml)
		return binder.fromReader(Process.class, reader)
	}
}
