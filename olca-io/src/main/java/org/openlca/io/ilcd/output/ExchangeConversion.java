package org.openlca.io.ilcd.output;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.processes.ExchangeList;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterList;
import org.openlca.ilcd.processes.ProcessInformation;
import org.openlca.ilcd.util.ExchangeExtension;

class ExchangeConversion {

	private org.openlca.ilcd.processes.Process ilcdProcess;
	private Process process;
	private IDatabase database;
	private DataStore dataStore;
	private List<MappedPair> mappedPairs = new ArrayList<>();

	public ExchangeConversion(Process process, IDatabase database,
			DataStore dataStore) {
		this.process = process;
		this.database = database;
		this.dataStore = dataStore;
	}

	public void run(org.openlca.ilcd.processes.Process ilcdProcess) {
		this.ilcdProcess = ilcdProcess;
		indexExchanges();
		List<org.openlca.ilcd.processes.Exchange> iExchanges = new ArrayList<>();
		for (MappedPair p : mappedPairs) {
			mapExchange(p.oExchange, p.iExchange);
			iExchanges.add(p.iExchange);
		}
		ExchangeList list = new ExchangeList();
		ilcdProcess.setExchanges(list);
		list.getExchanges().addAll(iExchanges);
	}

	private void indexExchanges() {
		int id = 1;
		for (Exchange oExchange : process.getExchanges()) {
			org.openlca.ilcd.processes.Exchange iExchange = new org.openlca.ilcd.processes.Exchange();
			if (oExchange.equals(process.getQuantitativeReference())) {
				iExchange.setDataSetInternalID(BigInteger.valueOf(0));
			} else {
				iExchange.setDataSetInternalID(BigInteger.valueOf(id));
				id++;
			}
			mappedPairs.add(new MappedPair(oExchange, iExchange));
		}
	}

	private void mapExchange(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		mapFlow(oExchange, iExchange);
		mapDirection(oExchange, iExchange);
		double resultingAmount = getRefAmount(oExchange);
		iExchange.setResultingAmount(resultingAmount);
		mapExtensions(oExchange, iExchange);
		new UncertaintyConverter().map(oExchange, iExchange);
		// mapAllocation(oExchange, iExchange);
		if (oExchange.getAmountFormula() != null) {
			mapParameter(oExchange, iExchange);
		} else {
			iExchange.setMeanAmount(resultingAmount);
		}
	}

	private double getRefAmount(Exchange oExchange) {
		double propFactor = oExchange.getFlowPropertyFactor() != null ? oExchange
				.getFlowPropertyFactor().getConversionFactor()
				: 1;
		double unitFactor = oExchange.getUnit() != null ? oExchange.getUnit()
				.getConversionFactor() : 1;
		return oExchange.getAmountValue() * propFactor * unitFactor;
	}

	private void mapExtensions(Exchange oExchange,
			org.openlca.ilcd.processes.Exchange iExchange) {
		ExchangeExtension extension = new ExchangeExtension(iExchange);
		if (oExchange.isAvoidedProduct()) {
			iExchange.setExchangeDirection(ExchangeDirection.OUTPUT);
			extension.setAvoidedProduct(true);
		}
		// TODO: map default provider
		// extension.setDefaultProvider(oExchange.getDefaultProviderId());

		extension.setAmount(oExchange.getAmountValue());
		extension.setBaseUncertainty(oExchange.getBaseUncertainty());
		extension.setPedigreeUncertainty(oExchange.getPedigreeUncertainty());
		if (oExchange.getAmountFormula() != null)
			extension.setFormula(oExchange.getAmountFormula());
		if (oExchange.getUnit() != null)
			extension.setUnitId(oExchange.getUnit().getRefId());
		if (oExchange.getFlowPropertyFactor() != null) {
			FlowPropertyFactor propFactor = oExchange.getFlowPropertyFactor();
			FlowProperty prop = propFactor.getFlowProperty();
			if (prop != null)
				extension.setPropertyId(prop.getRefId());
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
					oExchange.getFlow(), database, dataStore);
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

	// TODO: map allocation factors
	// private void mapAllocation(Exchange oExchange,
	// org.openlca.ilcd.processes.Exchange iExchange) {
	// for (AllocationFactor factor : oExchange.getAllocationFactors()) {
	// Allocation iAlloc = iExchange.getAllocation();
	// if (iAlloc == null) {
	// iAlloc = new Allocation();
	// iExchange.setAllocation(iAlloc);
	// }
	// double val = factor.getValue();
	// long productId = factor.getProductId();
	// BigInteger iExchangeId = findMappedId(productId);
	// if (iExchangeId != null) {
	// org.openlca.ilcd.processes.AllocationFactor iFactor = new
	// org.openlca.ilcd.processes.AllocationFactor();
	// iAlloc.getFactors().add(iFactor);
	// iFactor.setAllocatedFraction(new BigDecimal(val));
	// iFactor.setReferenceToCoProduct(iExchangeId);
	// }
	// }
	// }

	private BigInteger findMappedId(long oId) {
		for (MappedPair p : mappedPairs) {
			if (oId == p.oExchange.getId())
				return p.iExchange.getDataSetInternalID();
		}
		return null;
	}

	private class MappedPair {
		Exchange oExchange;
		org.openlca.ilcd.processes.Exchange iExchange;

		MappedPair(Exchange oExchange,
				org.openlca.ilcd.processes.Exchange iExchange) {
			super();
			this.oExchange = oExchange;
			this.iExchange = iExchange;
		}
	}

}
