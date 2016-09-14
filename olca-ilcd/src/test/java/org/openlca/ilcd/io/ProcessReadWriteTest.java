package org.openlca.ilcd.io;

import org.junit.Test;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.ExchangeList;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.util.IlcdConfig;
import org.openlca.ilcd.util.LangString;

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
		LangString.addLabel(name.baseName, "process name",
				IlcdConfig.getDefault());
		dataSetInformation.name = name;
		LangString.addFreeText(dataSetInformation.generalComment,
				"process description", IlcdConfig.getDefault());
	}

	private DataSetInfo makeDataSetInfo(Process process) {
		ProcessInfo information = new ProcessInfo();
		process.processInformation = information;
		DataSetInfo dataSetInformation = new DataSetInfo();
		information.dataSetInformation = dataSetInformation;
		return dataSetInformation;
	}

	private void createExchange(Process process) {
		ExchangeList exchangeList = new ExchangeList();
		process.exchanges = exchangeList;
		Exchange exchange = new Exchange();
		exchange.meanAmount = 1.5;

	}

}
