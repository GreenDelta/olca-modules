package org.openlca.io.ilcd.output;

import java.util.HashMap;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.util.ExchangeExtension;
import org.openlca.ilcd.util.Processes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeConversion {

	private final Export exp;
	private org.openlca.ilcd.processes.Process iProcess;
	private final Process process;

	public ExchangeConversion(Process process, Export exp) {
		this.process = process;
		this.exp = exp;
	}

	public void run(org.openlca.ilcd.processes.Process iProcess) {
		this.iProcess = iProcess;
		var map = new HashMap<Exchange, org.openlca.ilcd.processes.Exchange>();
		for (var oExchange : process.exchanges) {
			var iExchange = mapExchange(oExchange);
			map.put(oExchange, iExchange);
		}
		iProcess.withExchanges().addAll(map.values());
		AllocationFactors.map(process, map);
	}

	private org.openlca.ilcd.processes.Exchange mapExchange(Exchange oExchange) {
		var iExchange = new org.openlca.ilcd.processes.Exchange();
		iExchange.withId(oExchange.internalId);
		exp.add(iExchange::withComment, oExchange.description);
		iExchange.withFlow(exp.writeRef(oExchange.flow));
		iExchange.withDirection(oExchange.isInput
				? ExchangeDirection.INPUT
				: ExchangeDirection.OUTPUT);
		double resultingAmount = getRefAmount(oExchange);
		iExchange.withResultingAmount(resultingAmount);
		mapExtensions(oExchange, iExchange);
		new UncertaintyConverter().map(oExchange, iExchange);
		if (oExchange.formula != null) {
			mapParameter(oExchange, iExchange);
		} else {
			iExchange.withMeanAmount(resultingAmount);
		}
		if (oExchange.location != null) {
			iExchange.withLocation(oExchange.location.code);
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
			iExchange.withDirection(oExchange.isInput
					? ExchangeDirection.OUTPUT
					: ExchangeDirection.INPUT);
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
			ProcessDao dao = new ProcessDao(exp.db);
			ProcessDescriptor d = dao.getDescriptor(provider);
			if (d != null) {
				ext.setDefaultProvider(d.refId);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.warn("could not load default provider {}", provider, e);
		}
	}

	private void mapParameter(Exchange o, org.openlca.ilcd.processes.Exchange i) {
		var name = "temp_olca_param" + Processes.getParameters(iProcess).size();
		i.withVariable(name);
		i.withMeanAmount(1d);
		var param = new Parameter()
				.withFormula(o.formula)
				.withMean(o.amount)
				.withName(name);
		iProcess.withProcessInfo()
				.withParameterModel()
				.withParameters().add(param);
	}
}
