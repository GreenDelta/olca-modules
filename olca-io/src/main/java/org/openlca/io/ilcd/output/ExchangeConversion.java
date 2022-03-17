package org.openlca.io.ilcd.output;

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

import java.util.HashMap;
import java.util.Map;

class ExchangeConversion {

	private final ExportConfig config;
	private org.openlca.ilcd.processes.Process iProcess;
	private final Process process;

	public ExchangeConversion(Process process, ExportConfig config) {
		this.process = process;
		this.config = config;
	}

	public void run(org.openlca.ilcd.processes.Process iProcess) {
		this.iProcess = iProcess;
		Map<Exchange, org.openlca.ilcd.processes.Exchange> map = new HashMap<>();
		for (Exchange oExchange : process.exchanges) {
			org.openlca.ilcd.processes.Exchange iExchange = mapExchange(
					oExchange);
			map.put(oExchange, iExchange);
		}
		iProcess.exchanges.addAll(map.values());
		AllocationFactors.map(process, map);
	}

	private org.openlca.ilcd.processes.Exchange mapExchange(
			Exchange oExchange) {
		var iExchange = new org.openlca.ilcd.processes.Exchange();
		iExchange.id = oExchange.internalId;
		if (oExchange.description != null) {
			LangString.set(iExchange.comment,
					oExchange.description, config.lang);
		}
		mapFlow(oExchange, iExchange);
		iExchange.direction = oExchange.isInput
				? ExchangeDirection.INPUT
				: ExchangeDirection.OUTPUT;
		double resultingAmount = getRefAmount(oExchange);
		iExchange.resultingAmount = resultingAmount;
		mapExtensions(oExchange, iExchange);
		new UncertaintyConverter().map(oExchange, iExchange);
		if (oExchange.formula != null) {
			mapParameter(oExchange, iExchange);
		} else {
			iExchange.meanAmount = resultingAmount;
		}
		if (oExchange.location != null) {
			iExchange.location = oExchange.location.code;
		}
		return iExchange;
	}

	private double getRefAmount(Exchange oExchange) {
		double propFactor = oExchange.flowPropertyFactor != null
				? oExchange.flowPropertyFactor.conversionFactor
				: 1;
		double unitFactor = oExchange.unit != null ? oExchange.unit.conversionFactor : 1;
		return oExchange.amount * propFactor * unitFactor;
	}

	private void mapExtensions(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		ExchangeExtension ext = new ExchangeExtension(iExchange);
		if (oExchange.isAvoided) {
			// swap the direction
			iExchange.direction = oExchange.isInput
					? ExchangeDirection.OUTPUT
					: ExchangeDirection.INPUT;
			ext.setAvoidedProduct(true);
		}
		setProvider(oExchange, ext);
		ext.setAmount(oExchange.amount);
		ext.setBaseUncertainty(oExchange.baseUncertainty);
		ext.setPedigreeUncertainty(oExchange.dqEntry);
		if (oExchange.formula != null) {
			ext.setFormula(oExchange.formula);
		}
		if (oExchange.unit != null) {
			ext.setUnitId(oExchange.unit.refId);
		}
		if (oExchange.flowPropertyFactor != null) {
			FlowPropertyFactor propFactor = oExchange.flowPropertyFactor;
			FlowProperty prop = propFactor.flowProperty;
			if (prop != null) {
				ext.setPropertyId(prop.refId);
			}
		}
	}

	private void setProvider(Exchange oExchange, ExchangeExtension ext) {
		long provider = oExchange.defaultProviderId;
		if (provider == 0)
			return;
		try {
			ProcessDao dao = new ProcessDao(config.db);
			ProcessDescriptor d = dao.getDescriptor(provider);
			if (d != null) {
				ext.setDefaultProvider(d.refId);
			}
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
		parameter.formula = oExchange.formula;
		parameter.mean = oExchange.amount;
		parameter.name = paramName;
		addParameter(parameter);
	}

	private int getParamSize() {
		ProcessInfo info = iProcess.processInfo;
		if (info == null)
			return 0;
		ParameterSection list = info.parameters;
		if (list == null)
			return 0;
		return list.parameters.size();
	}

	private void addParameter(Parameter parameter) {
		ProcessInfo info = iProcess.processInfo;
		if (info == null) {
			info = new ProcessInfo();
			iProcess.processInfo = info;
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
			Ref ref = Export.of(oExchange.flow, config);
			if (ref != null) {
				iExchange.flow = ref;
			}
		}
	}

}
