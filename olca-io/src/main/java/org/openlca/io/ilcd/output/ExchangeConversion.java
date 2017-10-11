package org.openlca.io.ilcd.output;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterSection;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.util.ExchangeExtension;
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
			org.openlca.ilcd.processes.Exchange iExchange = mapExchange(
					oExchange);
			if (oExchange.equals(process.getQuantitativeReference()))
				iExchange.id = 0;
			else {
				iExchange.id = id;
				id++;
			}
			exchangeMap.put(oExchange, iExchange);
		}
		ilcdProcess.exchanges.addAll(exchangeMap.values());
		AllocationFactors.map(process, exchangeMap);
	}

	private org.openlca.ilcd.processes.Exchange mapExchange(
			Exchange oExchange) {
		org.openlca.ilcd.processes.Exchange iExchange = new org.openlca.ilcd.processes.Exchange();
		if (oExchange.description != null)
			LangString.set(iExchange.comment,
					oExchange.description, config.lang);
		mapFlow(oExchange, iExchange);
		mapDirection(oExchange, iExchange);
		double resultingAmount = getRefAmount(oExchange);
		iExchange.resultingAmount = resultingAmount;
		mapExtensions(oExchange, iExchange);
		new UncertaintyConverter().map(oExchange, iExchange);
		if (oExchange.amountFormula != null)
			mapParameter(oExchange, iExchange);
		else
			iExchange.meanAmount = resultingAmount;
		return iExchange;
	}

	private double getRefAmount(Exchange oExchange) {
		double propFactor = oExchange.flowPropertyFactor != null
				? oExchange.flowPropertyFactor.getConversionFactor()
				: 1;
		double unitFactor = oExchange.unit != null ? oExchange.unit
				.getConversionFactor() : 1;
		return oExchange.amount * propFactor * unitFactor;
	}

	private void mapExtensions(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		ExchangeExtension ext = new ExchangeExtension(iExchange);
		if (oExchange.isAvoided) {
			iExchange.direction = ExchangeDirection.OUTPUT;
			ext.setAvoidedProduct(true);
		}
		setProvider(oExchange, ext);
		ext.setAmount(oExchange.amount);
		ext.setBaseUncertainty(oExchange.baseUncertainty);
		ext.setPedigreeUncertainty(oExchange.dqEntry);
		if (oExchange.amountFormula != null)
			ext.setFormula(oExchange.amountFormula);
		if (oExchange.unit != null)
			ext.setUnitId(oExchange.unit.getRefId());
		if (oExchange.flowPropertyFactor != null) {
			FlowPropertyFactor propFactor = oExchange.flowPropertyFactor;
			FlowProperty prop = propFactor.getFlowProperty();
			if (prop != null)
				ext.setPropertyId(prop.getRefId());
		}
	}

	private void setProvider(Exchange oExchange, ExchangeExtension ext) {
		long provider = oExchange.defaultProviderId;
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
		iExchange.variable = paramName;
		iExchange.meanAmount = 1d;
		Parameter parameter = new Parameter();
		parameter.formula = oExchange.amountFormula;
		parameter.mean = oExchange.amount;
		parameter.name = paramName;
		addParameter(parameter);
	}

	private int getParamSize() {
		ProcessInfo info = ilcdProcess.processInfo;
		if (info == null)
			return 0;
		ParameterSection list = info.parameters;
		if (list == null)
			return 0;
		return list.parameters.size();
	}

	private void addParameter(Parameter parameter) {
		ProcessInfo info = ilcdProcess.processInfo;
		if (info == null) {
			info = new ProcessInfo();
			ilcdProcess.processInfo = info;
		}
		ParameterSection list = info.parameters;
		if (list == null) {
			list = new ParameterSection();
			info.parameters = list;
		}
		list.parameters.add(parameter);
	}

	private void mapFlow(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		if (oExchange.flow != null) {
			Ref ref = ExportDispatch.forwardExportCheck(
					oExchange.flow, config);
			if (ref != null) {
				iExchange.flow = ref;
			}
		}
	}

	private void mapDirection(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		if (oExchange.isInput) {
			iExchange.direction = ExchangeDirection.INPUT;
		} else {
			iExchange.direction = ExchangeDirection.OUTPUT;
		}
	}

}
