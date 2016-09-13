package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInformation;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.util.IlcdConfig;
import org.openlca.ilcd.util.ProcessBag;

public class ProductModelTest {

	private static XmlBinder binder = new XmlBinder();

	@Test
	public void writeReadPlainProcess() throws Exception {
		Process process = makePlainProcess();
		String xml = marshal(process);
		Process copy = unmarshal(xml);
		assertEquals(process.getProcessInformation().getDataSetInformation().uuid, copy.getProcessInformation()
				.getDataSetInformation().uuid);
	}

	@Test
	public void writeReadProductModel() throws Exception {
		Process process = makeProductModel();
		String xml = marshal(process);
		Process copy = unmarshal(xml);
		ProductModel model = (ProductModel) copy.getProcessInformation()
				.getOther().getAny().get(0);
		assertEquals("test-model", model.getName());
	}

	@Test
	public void testNoModelInProcessBag() throws Exception {
		Process process = unmarshal(marshal(makePlainProcess()));
		ProcessBag bag = new ProcessBag(process, IlcdConfig.getDefault());
		assertFalse(bag.hasProductModel());
		assertNull(bag.getProductModel());
	}

	@Test
	public void testModelInProcessBag() throws Exception {
		Process process = unmarshal(marshal(makeProductModel()));
		ProcessBag bag = new ProcessBag(process, IlcdConfig.getDefault());
		assertTrue(bag.hasProductModel());
		assertEquals("test-model", bag.getProductModel().getName());
	}

	private Process makePlainProcess() {
		Process process = new Process();
		ProcessInformation procInfo = new ProcessInformation();
		process.setProcessInformation(procInfo);
		DataSetInfo dataSetInfo = new DataSetInfo();
		procInfo.setDataSetInformation(dataSetInfo);
		dataSetInfo.uuid = UUID.randomUUID().toString();
		return process;
	}

	private Process makeProductModel() {
		Process process = makePlainProcess();
		Other other = new Other();
		process.getProcessInformation().setOther(other);
		List<Object> extension = other.getAny();
		ProductModel model = new ProductModel();
		model.setName("test-model");
		extension.add(model);
		return process;
	}

	private String marshal(Process process) throws Exception {
		StringWriter writer = new StringWriter();
		binder.toWriter(process, writer);
		return writer.toString();
	}

	private Process unmarshal(String xml) throws Exception {
		StringReader reader = new StringReader(xml);
		return binder.fromReader(Process.class, reader);
	}
}
