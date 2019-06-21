package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.processes.AllocationFactor;
import org.openlca.ilcd.util.ExchangeExtension;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the inputs and outputs of an ILCD process to an openLCA process.
 */
class ProcessExchanges {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private ImportConfig config;
	private List<MappedPair> mappedPairs = new ArrayList<>();

	private ProviderLinker providerLinker = new ProviderLinker();

	ProcessExchanges(ImportConfig config, ProviderLinker providerLinker) {
		this.config = config;
		this.providerLinker = providerLinker;
	}

	void map(ProcessBag iProcess, Process process) {
		mappedPairs.clear();
		int maxID = 0;
		for (org.openlca.ilcd.processes.Exchange iExchange : iProcess
				.getExchanges()) {
			ExchangeFlow flow = new ExchangeFlow(iExchange);
			ExchangeExtension ext = new ExchangeExtension(iExchange);

			flow.findOrImport(config);

			// map and check flow and unit
			mapUnit(ext, flow);
			if (!flow.isValid()) {
				log.warn("invalid exchange {} - not added to process {}",
						flow, process);
				continue;
			}

			Exchange e = init(iExchange, flow, process);

			// we take the internal IDs from ILCD
			e.internalId = iExchange.id;
			maxID = Math.max(maxID, e.internalId);

			// add a possible mapped provider
			if (flow.getMappedProvider() != null) {
				String provider = flow.getMappedProvider().refId;
				if (provider != null) {
					providerLinker.addLink(
							process.refId, e.internalId, provider);
				}
			}

			// add data from a possible openLCA extension
			if (ext.isValid()) {
				e.dqEntry = ext.getPedigreeUncertainty();
				e.baseUncertainty = ext.getBaseUncertainty();
				e.amount = ext.getAmount();
				if (ext.isAvoidedProduct()) {
					e.isInput = !e.isInput;
					e.isAvoided = true;
				}
				String provider = ext.getDefaultProvider();
				if (provider != null
						&& flow.getMappedProvider() == null) {
					providerLinker.addLink(
							process.refId, e.internalId, provider);
				}
			}
			new UncertaintyConverter().map(iExchange, e);
			mapFormula(iExchange, ext, e);
			if (flow.isMapped()) {
				applyConversionFactor(e, flow.mapEntry);
			}

			mappedPairs.add(new MappedPair(e, iExchange));
		}
		process.lastInternalId = maxID;
		mapAllocation(process);
		RefFlow.map(iProcess, process, mappedPairs.stream().collect(
				Collectors.toMap(
						pair -> pair.iExchange.id,
						pair -> pair.oExchange)));
	}

	private Exchange init(org.openlca.ilcd.processes.Exchange iExchange,
			ExchangeFlow flow, Process process) {
		Exchange e = null;
		if (flow.flowProperty != null && flow.unit != null) {
			e = process.exchange(flow.flow, flow.flowProperty, flow.unit);
		} else {
			e = process.exchange(flow.flow);
		}
		e.isInput = iExchange.direction == ExchangeDirection.INPUT;
		e.description = LangString.getFirst(iExchange.comment, config.langs);
		// set the default value for the exchange which may is overwritten
		// later by applying flow mappings, formulas, etc.
		e.amount = iExchange.resultingAmount != null
				? iExchange.resultingAmount
				: iExchange.meanAmount;
		return e;
	}

	private void mapFormula(
			org.openlca.ilcd.processes.Exchange iExchange,
			ExchangeExtension ext,
			Exchange oExchange) {
		String formula = ext != null ? ext.getFormula() : null;
		if (formula != null) {
			oExchange.amountFormula = formula;
			return;
		}
		if (Strings.nullOrEmpty(iExchange.variable))
			return;
		double amount = iExchange.meanAmount;
		String meanAmountStr = Double.toString(amount);
		String parameter = iExchange.variable;
		formula = amount == 1.0 ? parameter
				: meanAmountStr + " * " + parameter + "";
		oExchange.amountFormula = formula;
	}

	private void applyConversionFactor(Exchange e, FlowMapEntry entry) {
		if (entry == null || entry.factor == 1.0)
			return;
		e.amount *= entry.factor;
		if (Strings.notEmpty(e.amountFormula)) {
			e.amountFormula = "(" + e.amountFormula + ") * "
					+ entry.factor;
		}
	}

	private void mapUnit(ExchangeExtension ext, ExchangeFlow flow) {
		if (!ext.isValid())
			return;
		try {
			UnitDao unitDao = new UnitDao(config.db);
			flow.unit = unitDao.getForRefId(ext.getUnitId());
			FlowPropertyDao propDao = new FlowPropertyDao(config.db);
			FlowProperty property = propDao
					.getForRefId(ext.getPropertyId());
			flow.flowProperty = property;
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
		f.productId = productId;
		f.value = fraction / 100;
		if (oExchange.flow.id == productId)
			f.method = AllocationMethod.PHYSICAL;
		else {
			f.method = AllocationMethod.CAUSAL;
			f.exchange = oExchange;
		}
		process.allocationFactors.add(f);
	}

	private Long findMappedFlowId(int iExchangeId) {
		for (MappedPair p : mappedPairs) {
			if (iExchangeId == p.iExchange.id) {
				if (p.oExchange.flow != null)
					return p.oExchange.flow.id;
			}
		}
		return null;
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
