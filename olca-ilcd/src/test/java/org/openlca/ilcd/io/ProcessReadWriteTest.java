package org.openlca.ilcd.io;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.ProcessName;

public class ProcessReadWriteTest {

	@Test
	public void testReadWrite() throws Exception {
		Process process = new Process();
		setNameAndComment(process);
		createExchange(process);
		XmlBinder binder = new XmlBinder();
		byte[] bytes = binder.toByteArray(process);
		process = binder.fromStream(Process.class,
				new ByteArrayInputStream(bytes));
		Assert.assertEquals("process name",
				process.processInfo.dataSetInfo.name.name.get(0).value);
		Assert.assertEquals("process description",
				process.processInfo.dataSetInfo.comment.get(0).value);
		Assert.assertEquals(1, process.exchanges.size());
	}

	private void setNameAndComment(Process process) {
		DataSetInfo info = makeDataSetInfo(process);
		ProcessName name = new ProcessName();
		LangString.set(name.name, "process name", "en");
		info.name = name;
		LangString.set(info.comment,
				"process description", "en");
	}

	private DataSetInfo makeDataSetInfo(Process process) {
		ProcessInfo information = new ProcessInfo();
		process.processInfo = information;
		DataSetInfo dataSetInformation = new DataSetInfo();
		information.dataSetInfo = dataSetInformation;
		return dataSetInformation;
	}

	private void createExchange(Process process) {
		Exchange exchange = new Exchange();
		exchange.meanAmount = 1.5;
		process.exchanges.add(exchange);
	}

}
