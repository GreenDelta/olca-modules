package org.openlca.io.ilcd.output;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.processes.ExchangeList;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterList;
import org.openlca.ilcd.processes.ProcessInformation;
import org.openlca.ilcd.util.ExchangeExtension;
import org.openlca.ilcd.util.LangString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeConversion {

	private final ExportConfig config;
	private org.openlca.ilcd.processes.Process ilcdProcess;
	private Process process;

	public ExchangeConversion(Process process, ExportConfig config) {
		this.process = process;
		this.config = config;
	}

	public void run(org.openlca.ilcd.processes.Process ilcdProcess) {
		this.ilcdProcess = ilcdProcess;
		Map<Exchange, org.openlca.ilcd.processes.Exchange> exchangeMap = new HashMap<>();
		int id = 1;
		for (Exchange oExchange : process.getExchanges()) {
			org.openlca.ilcd.processes.Exchange iExchange = mapExchange(oExchange);
			if (oExchange.equals(process.getQuantitativeReference()))
				iExchange.setDataSetInternalID(BigInteger.valueOf(0));
			else {
				iExchange.setDataSetInternalID(BigInteger.valueOf(id));
				id++;
			}
			exchangeMap.put(oExchange, iExchange);
		}
		ExchangeList list = new ExchangeList();
		ilcdProcess.setExchanges(list);
		list.getExchanges().addAll(exchangeMap.values());
		AllocationFactors.map(process, exchangeMap);
	}

	private org.openlca.ilcd.processes.Exchange mapExchange(Exchange oExchange) {
		org.openlca.ilcd.processes.Exchange iExchange = new org.openlca.ilcd.processes.Exchange();
		if (oExchange.description != null)
			LangString.addLabel(iExchange.getGeneralComment(),
					oExchange.description, config.ilcdConfig);
		mapFlow(oExchange, iExchange);
		mapDirection(oExchange, iExchange);
		double resultingAmount = getRefAmount(oExchange);
		iExchange.setResultingAmount(resultingAmount);
		mapExtensions(oExchange, iExchange);
		new UncertaintyConverter().map(oExchange, iExchange);
		if (oExchange.getAmountFormula() != null)
			mapParameter(oExchange, iExchange);
		else
			iExchange.setMeanAmount(resultingAmount);
		return iExchange;
	}

	private double getRefAmount(Exchange oExchange) {
		double propFactor = oExchange.getFlowPropertyFactor() != null ? oExchange
				.getFlowPropertyFactor().getConversionFactor() : 1;
		double unitFactor = oExchange.getUnit() != null ? oExchange.getUnit()
				.getConversionFactor() : 1;
		return oExchange.getAmountValue() * propFactor * unitFactor;
	}

	private void mapExtensions(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		ExchangeExtension ext = new ExchangeExtension(iExchange);
		if (oExchange.isAvoidedProduct()) {
			iExchange.setExchangeDirection(ExchangeDirection.OUTPUT);
			ext.setAvoidedProduct(true);
		}
		setProvider(oExchange, ext);
		ext.setAmount(oExchange.getAmountValue());
		ext.setBaseUncertainty(oExchange.getBaseUncertainty());
		ext.setPedigreeUncertainty(oExchange.getPedigreeUncertainty());
		if (oExchange.getAmountFormula() != null)
			ext.setFormula(oExchange.getAmountFormula());
		if (oExchange.getUnit() != null)
			ext.setUnitId(oExchange.getUnit().getRefId());
		if (oExchange.getFlowPropertyFactor() != null) {
			FlowPropertyFactor propFactor = oExchange.getFlowPropertyFactor();
			FlowProperty prop = propFactor.getFlowProperty();
			if (prop != null)
				ext.setPropertyId(prop.getRefId());
		}
	}

	private void setProvider(Exchange oExchange, ExchangeExtension ext) {
		long provider = oExchange.getDefaultProviderId();
		if (provider == 0)
			return;
		try {
			ProcessDao dao = new ProcessDao(config.db);
			ProcessDescriptor d = dao.getDescriptor(provider);
			if (d != null)
				ext.setDefaultProvider(d.getRefId());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.warn("could not load default provider " + provider, e);
		}
	}

	private void mapParameter(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		String paramName = "temp_olca_param" + getParamSize();
		iExchange.setParameterName(paramName);
		iExchange.setMeanAmount(1d);
		Parameter parameter = new Parameter();
		parameter.setFormula(oExchange.getAmountFormula());
		parameter.setMeanValue(oExchange.getAmountValue());
		parameter.setName(paramName);
		addParameter(parameter);
	}

	private int getParamSize() {
		ProcessInformation info = ilcdProcess.getProcessInformation();
		if (info == null)
			return 0;
		ParameterList list = info.getParameters();
		if (list == null)
			return 0;
		return list.getParameters().size();
	}

	private void addParameter(Parameter parameter) {
		ProcessInformation info = ilcdProcess.getProcessInformation();
		if (info == null) {
			info = new ProcessInformation();
			ilcdProcess.setProcessInformation(info);
		}
		ParameterList list = info.getParameters();
		if (list == null) {
			list = new ParameterList();
			info.setParameters(list);
		}
		list.getParameters().add(parameter);
	}

	private void mapFlow(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		if (oExchange.getFlow() != null) {
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					oExchange.getFlow(), config);
			if (ref != null) {
				iExchange.setFlow(ref);
			}
		}
	}

	private void mapDirection(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		if (oExchange.isInput()) {
			iExchange.setExchangeDirection(ExchangeDirection.INPUT);
		} else {
			iExchange.setExchangeDirection(ExchangeDirection.OUTPUT);
		}
	}

}
