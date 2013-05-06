package org.openlca.io.ilcd.input;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Expression;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.processes.AllocationFactor;
import org.openlca.ilcd.util.ExchangeExtension;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the inputs and outputs of an ILCD process to an openLCA process.
 */
class ProcessExchanges {

	private Logger log = LoggerFactory.getLogger(getClass());
	private DataStore dataStore;
	private ProcessBag ilcdProcess;
	private IDatabase database;
	private org.openlca.core.model.Process olcaProcess;
	private FlowMap flowMap;
	private List<MappedPair> mappedPairs = new ArrayList<>();

	private ProcessExchanges() {
	}

	public static Dispatch mapFrom(DataStore dataStore, ProcessBag ilcdProcess) {
		Dispatch dispatch = new Dispatch();
		dispatch.dataStore = dataStore;
		dispatch.ilcdProcess = ilcdProcess;
		return dispatch;
	}

	static class Dispatch {
		private DataStore dataStore;
		private ProcessBag ilcdProcess;
		private FlowMap flowMap;

		Dispatch withFlowMap(FlowMap flowMap) {
			this.flowMap = flowMap;
			return this;
		}

		void to(IDatabase database, org.openlca.core.model.Process process) {
			ProcessExchanges exchanges = new ProcessExchanges();
			exchanges.database = database;
			exchanges.dataStore = this.dataStore;
			exchanges.ilcdProcess = this.ilcdProcess;
			exchanges.olcaProcess = process;
			exchanges.flowMap = this.flowMap;
			exchanges.map();
		}
	}

	private void map() {
		for (org.openlca.ilcd.processes.Exchange iExchange : ilcdProcess
				.getExchanges()) {
			ExchangeFlow exchangeFlow = new ExchangeFlow(iExchange);
			exchangeFlow.findOrImport(database, dataStore, flowMap);
			Exchange exchange = createExchange(iExchange, exchangeFlow);
			ExchangeExtension extension = new ExchangeExtension(iExchange);
			if (extension.isValid())
				mapExtension(extension, exchange, exchangeFlow.getFlow());
			else
				mapPropertyAndUnit(exchangeFlow, exchange);
			if (isValid(exchange)) {
				olcaProcess.add(exchange);
				mappedPairs.add(new MappedPair(exchange, iExchange));
			} else {
				log.warn("invalid exchange {} - not added to process {}",
						exchange, olcaProcess);
			}
		}
		mapAllocation();
		mapReferenceFlow();
	}

	private boolean isValid(Exchange exchange) {
		return exchange.getFlow() != null
				&& exchange.getFlowPropertyFactor() != null
				&& exchange.getUnit() != null;
	}

	private Exchange createExchange(
			org.openlca.ilcd.processes.Exchange iExchange,
			ExchangeFlow exchangeFlow) {
		Exchange oExchange = new ExchangeConversion(iExchange).map(olcaProcess
				.getId());
		if (exchangeFlow.getFlow() != null) {
			oExchange.setFlow(exchangeFlow.getFlow());
			if (exchangeFlow.isMapped())
				applyFlowAssignment(oExchange, exchangeFlow.getMapEntry());
		}
		return oExchange;
	}

	private void applyFlowAssignment(Exchange oExchange, FlowMapEntry mapEntry) {
		Expression amount = oExchange.getResultingAmount();
		double newVal = mapEntry.getConversionFactor() * amount.getValue();
		amount.setValue(newVal);
		if (oExchange.isParametrized()) {
			String newForm = "(" + amount.getFormula() + ") * "
					+ mapEntry.getConversionFactor();
			amount.setFormula(newForm);
		} else {
			String newForm = Double.toString(newVal);
			amount.setFormula(newForm);
		}
	}

	private void mapPropertyAndUnit(ExchangeFlow exchangeFlow,
			Exchange oExchange) {
		try {
			Flow flowInfo = exchangeFlow.getFlow();
			FlowProperty flowProperty = flowInfo.getReferenceFlowProperty();
			FlowPropertyFactor factor = flowInfo
					.getFlowPropertyFactor(flowProperty.getId());
			oExchange.setFlowPropertyFactor(factor);
			UnitGroup unitGroup = database.select(UnitGroup.class,
					flowProperty.getUnitGroupId());
			oExchange.setUnit(unitGroup.getReferenceUnit());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot get flow property or unit from database", e);
		}
	}

	private void mapExtension(ExchangeExtension extension, Exchange exchange,
			Flow flowInfo) {
		exchange.setDefaultProviderId(extension.getDefaultProvider());
		if (extension.isAvoidedProduct()) {
			exchange.setInput(true);
			exchange.setAvoidedProduct(true);
		}
		try {
			Unit unit = database.select(Unit.class, extension.getUnitId());
			exchange.setUnit(unit);
			FlowPropertyFactor factor = flowInfo
					.getFlowPropertyFactor(extension.getPropertyId());
			exchange.setFlowPropertyFactor(factor);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot get flow property or unit from database", e);
		}
	}

	private void mapAllocation() {
		for (MappedPair p : mappedPairs) {
			if (p.iExchange.getAllocation() == null)
				continue;
			for (AllocationFactor iFactor : p.iExchange.getAllocation()
					.getFactors()) {
				String productId = findMappedId(iFactor
						.getReferenceToCoProduct());
				BigDecimal fraction = iFactor.getAllocatedFraction();
				if (productId != null && fraction != null)
					createAllocationFactor(p, productId, fraction);
			}
		}
	}

	private void createAllocationFactor(MappedPair p, String productId,
			BigDecimal fraction) {
		org.openlca.core.model.AllocationFactor oFactor = new org.openlca.core.model.AllocationFactor();
		oFactor.setId(UUID.randomUUID().toString());
		oFactor.setProductId(productId);
		oFactor.setValue(fraction.doubleValue());
		p.oExchange.add(oFactor);
	}

	private String findMappedId(BigInteger iId) {
		if (iId == null)
			return null;
		for (MappedPair p : mappedPairs) {
			if (iId.equals(p.iExchange.getDataSetInternalID()))
				return p.oExchange.getId();
		}
		return null;
	}

	private void mapReferenceFlow() {
		Map<BigInteger, Exchange> map = new HashMap<>();
		for (MappedPair pair : mappedPairs)
			map.put(pair.iExchange.getDataSetInternalID(), pair.oExchange);
		new ProcessRefFlowMapper(ilcdProcess, olcaProcess, map)
				.setReferenceFlow();
	}

	private class MappedPair {
		Exchange oExchange;
		org.openlca.ilcd.processes.Exchange iExchange;

		MappedPair(Exchange oExchange,
				org.openlca.ilcd.processes.Exchange iExchange) {
			this.oExchange = oExchange;
			this.iExchange = iExchange;
		}
	}
}
