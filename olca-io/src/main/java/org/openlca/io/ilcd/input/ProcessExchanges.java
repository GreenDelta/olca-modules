package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
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
			ExchangeExtension extension = new ExchangeExtension(iExchange);
			exchangeFlow.process = process;
			exchangeFlow.findOrImport(config);
			if (extension.isValid()) {
				mapExtension(extension, exchangeFlow);
			}
			if (!exchangeFlow.isValid()) {
				log.warn("invalid exchange {} - not added to process {}", exchangeFlow, process);
				continue;
			}
			Exchange exchange = createExchange(iExchange, exchangeFlow);
			// TODO: map default provider if extension is valid
			// exchange.setDefaultProviderId(extension.getDefaultProvider());
			if (extension.isValid() && extension.isAvoidedProduct()) {
				exchange.isInput = true;
				exchange.isAvoided = true;
			}
			mappedPairs.add(new MappedPair(exchange, iExchange));
		}
		mapAllocation(process);
		mapReferenceFlow(ilcdProcess, process);
	}

	private Exchange createExchange(
			org.openlca.ilcd.processes.Exchange iExchange,
			ExchangeFlow exchangeFlow) {
		Exchange oExchange = new ExchangeConversion(iExchange, config).map(exchangeFlow);
		if (oExchange.flow != null) {
			if (exchangeFlow.isMapped()) {
				applyFlowAssignment(oExchange, exchangeFlow.mapEntry);
			}
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

	private void mapExtension(ExchangeExtension extension, ExchangeFlow exchangeFlow) {
		try {
			UnitDao unitDao = new UnitDao(config.db);
			exchangeFlow.unit = unitDao.getForRefId(extension.getUnitId());
			FlowPropertyDao propDao = new FlowPropertyDao(config.db);
			FlowProperty property = propDao.getForRefId(extension.getPropertyId());
			exchangeFlow.flowProperty = property;
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
