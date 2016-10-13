package org.openlca.ilcd.io;

import org.junit.Test;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.ProcessName;

public class ProcessReadWriteTest {

	@Test
	public void testReadWrite() {
		Process process = new Process();
		setNameAndComment(process);
		createExchange(process);
	}

	private void setNameAndComment(Process process) {
		DataSetInfo dataSetInformation = makeDataSetInfo(process);
		ProcessName name = new ProcessName();
		LangString.set(name.name, "process name", "en");
		dataSetInformation.name = name;
		LangString.set(dataSetInformation.comment,
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
