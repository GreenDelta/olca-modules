package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.processes.AllocationFactor;
import org.openlca.ilcd.util.ExchangeExtension;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.io.maps.FlowMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the inputs and outputs of an ILCD process to an openLCA process.
 */
class ProcessExchanges {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private ImportConfig config;
	private List<MappedPair> mappedPairs = new ArrayList<>();

	ProcessExchanges(ImportConfig config) {
		this.config = config;
	}

	void map(ProcessBag ilcdProcess, Process process) {
		mappedPairs.clear();
		for (org.openlca.ilcd.processes.Exchange iExchange : ilcdProcess
				.getExchanges()) {
			ExchangeFlow exchangeFlow = new ExchangeFlow(iExchange);
			exchangeFlow.findOrImport(config);
			Exchange exchange = createExchange(iExchange, exchangeFlow);
			ExchangeExtension extension = new ExchangeExtension(iExchange);
			if (extension.isValid())
				mapExtension(extension, exchange, exchangeFlow.getFlow());
			else
				mapPropertyAndUnit(exchangeFlow, exchange);
			if (isValid(exchange)) {
				exchange.internalId = process.drawNextInternalId();
				process.getExchanges().add(exchange);
				mappedPairs.add(new MappedPair(exchange, iExchange));
			} else {
				log.warn("invalid exchange {} - not added to process {}",
						exchange, process);
			}
		}
		mapAllocation(process);
		mapReferenceFlow(ilcdProcess, process);
	}

	private boolean isValid(Exchange exchange) {
		return exchange.flow != null
				&& exchange.flowPropertyFactor != null
				&& exchange.unit != null;
	}

	private Exchange createExchange(
			org.openlca.ilcd.processes.Exchange iExchange,
			ExchangeFlow exchangeFlow) {
		Exchange oExchange = new ExchangeConversion(iExchange, config).map();
		if (exchangeFlow.getFlow() != null) {
			oExchange.flow = exchangeFlow.getFlow();
			if (exchangeFlow.isMapped())
				applyFlowAssignment(oExchange, exchangeFlow.getMapEntry());
		}
		return oExchange;
	}

	private void applyFlowAssignment(Exchange oExchange,
			FlowMapEntry mapEntry) {
		double amount = oExchange.amount;
		double newVal = mapEntry.conversionFactor * amount;
		oExchange.amount = newVal;
		if (oExchange.amountFormula != null) {
			String newForm = "(" + oExchange.amountFormula + ") * "
					+ mapEntry.conversionFactor;
			oExchange.amountFormula = newForm;
		}
	}

	private void mapPropertyAndUnit(ExchangeFlow exchangeFlow,
			Exchange oExchange) {
		try {
			Flow flowInfo = exchangeFlow.getFlow();
			FlowProperty flowProperty = flowInfo.getReferenceFlowProperty();
			FlowPropertyFactor factor = flowInfo.getFactor(flowProperty);
			oExchange.flowPropertyFactor = factor;
			UnitGroup unitGroup = flowProperty.getUnitGroup();
			oExchange.unit = unitGroup.getReferenceUnit();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot get flow property or unit from database", e);
		}
	}

	private void mapExtension(ExchangeExtension extension, Exchange exchange,
			Flow flowInfo) {
		// TODO: map default provider
		// exchange.setDefaultProviderId(extension.getDefaultProvider());
		if (extension.isAvoidedProduct()) {
			exchange.isInput = true;
			exchange.isAvoided = true;
		}
		try {
			UnitDao unitDao = new UnitDao(config.db);
			Unit unit = unitDao.getForRefId(extension.getUnitId());
			final Unit unit1 = unit;
			exchange.unit = unit1;
			FlowPropertyDao propDao = new FlowPropertyDao(config.db);
			FlowProperty property = propDao.getForRefId(extension
					.getPropertyId());
			FlowPropertyFactor factor = flowInfo.getFactor(property);
			exchange.flowPropertyFactor = factor;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(this.getClass());
			log.error("Cannot get flow property or unit from database", e);
		}
	}

	private void mapAllocation(Process process) {
		for (MappedPair p : mappedPairs) {
			AllocationFactor[] factors = p.iExchange.allocations;
			if (factors == null)
				continue;
			for (AllocationFactor iFactor : factors) {
				Long productId = findMappedFlowId(iFactor.productExchangeId);
				if (productId == null)
					continue;
				createAllocationFactor(p, productId, iFactor.fraction,
						process);
			}
		}
	}

	private void createAllocationFactor(MappedPair p, long productId,
			double fraction, Process process) {
		Exchange oExchange = p.oExchange;
		if (oExchange.flow == null)
			return;
		org.openlca.core.model.AllocationFactor f = new org.openlca.core.model.AllocationFactor();
		f.setProductId(productId);
		f.setValue(fraction / 100);
		if (oExchange.flow.getId() == productId)
			f.setAllocationType(AllocationMethod.PHYSICAL);
		else {
			f.setAllocationType(AllocationMethod.CAUSAL);
			f.setExchange(oExchange);
		}
		process.getAllocationFactors().add(f);
	}

	private Long findMappedFlowId(int iExchangeId) {
		for (MappedPair p : mappedPairs) {
			if (iExchangeId == p.iExchange.id) {
				if (p.oExchange.flow != null)
					return p.oExchange.flow.getId();
			}
		}
		return null;
	}

	private void mapReferenceFlow(ProcessBag ilcdProcess, Process process) {
		Map<Integer, Exchange> map = new HashMap<>();
		for (MappedPair pair : mappedPairs)
			map.put(pair.iExchange.id, pair.oExchange);
		RefFlow.map(ilcdProcess, process, map);
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
