package org.openlca.ilcd.io;

import org.junit.Test;
import org.openlca.ilcd.processes.DataSetInformation;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.ExchangeList;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInformation;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.util.LangString;

public class ProcessReadWriteTest {

	@Test
	public void testReadWrite() {
		Process process = new Process();
		setNameAndComment(process);
		createExchange(process);
	}

	private void setNameAndComment(Process process) {
		DataSetInformation dataSetInformation = makeDataSetInfo(process);
		ProcessName name = new ProcessName();
		LangString.addLabel(name.getBaseName(), "process name");
		dataSetInformation.setName(name);
		LangString.addFreeText(dataSetInformation.getGeneralComment(),
				"process description");
	}

	private DataSetInformation makeDataSetInfo(Process process) {
		ProcessInformation information = new ProcessInformation();
		process.setProcessInformation(information);
		DataSetInformation dataSetInformation = new DataSetInformation();
		information.setDataSetInformation(dataSetInformation);
		return dataSetInformation;
	}

	private void createExchange(Process process) {
		ExchangeList exchangeList = new ExchangeList();
		process.setExchanges(exchangeList);
		Exchange exchange = new Exchange();
		exchange.setMeanAmount(1.5);

	}

}
