package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.util.Strings;

/**
 * Maps the inputs and outputs of an ILCD process to an openLCA process.
 */
class ProcessExchanges {

	private final ImportConfig config;

	ProcessExchanges(ImportConfig config) {
		this.config = config;
	}

	void map(ProcessBag iProcess, Process process) {
		int maxID = 0;

		var mappedExchanges = new ArrayList<MappedExchange>();
		for (var origin : iProcess.getExchanges()) {
			if (origin.flow == null || Strings.nullOrEmpty(origin.flow.uuid)) {
				config.log().warn("invalid flow references in process "
					+ iProcess.getId());
				continue;
			}

			var flow = FlowImport.get(config, origin.flow.uuid);
			if (flow.isEmpty()) {
				config.log().error("missing flows in process: " + iProcess.getId());
				continue;
			}

			var mapped = MappedExchange.of(flow, origin);
			mappedExchanges.add(mapped);
			if (mapped.hasExtensionError()) {
				config.log().warn("invalid exchange extensions in process: "
					+ iProcess.getId());
			}

			var exchange = mapped.exchange();
			exchange.description = config.str(origin.comment);
			exchange.location = config.locationOf(origin.location);

			// we take the internal IDs from ILCD
			maxID = Math.max(maxID, exchange.internalId);

			// add a possible mapped provider
			var providerId = mapped.providerId();
			if (providerId != null) {
				config.providers().add(providerId, exchange);
			}

			process.exchanges.add(exchange);
		} // for each exchange

		process.lastInternalId = maxID;
		mapAllocation(process, mappedExchanges);

		// map the reference flow of the process
		var mappedIndex = new HashMap<Integer, Exchange>();
		for(var m : mappedExchanges) {
			mappedIndex.put(m.origin().id, m.exchange());
		}
		RefFlow.map(iProcess, process, mappedIndex);
	}


	private void mapAllocation(Process process, List<MappedExchange> mapped) {
		for (var m : mapped) {
			var factors = m.origin().allocations;
			if (factors == null)
				continue;
			for (var f : factors) {

				// find the product ID of the factor
				var productId = mapped.stream()
					.filter(e -> e.origin().id == f.productExchangeId)
					.map(e -> e.exchange().flow)
					.filter(Objects::nonNull)
					.mapToLong(flow -> flow.id)
					.findAny();
				if (productId.isEmpty())
					continue;

				// create the allocation factor
				createAllocationFactor(
					m.exchange(), productId.getAsLong(), f.fraction, process);
			}
		}
	}

	private void createAllocationFactor(Exchange exchange, long productId,
		double fraction, Process process) {
		if (exchange.flow == null)
			return;
		var factor = new AllocationFactor();
		factor.productId = productId;
		factor.value = fraction / 100;
		if (exchange.flow.id == productId) {
			// create a physical and economic factor
			factor.method = AllocationMethod.PHYSICAL;
			process.allocationFactors.add(factor);
			var economic = factor.copy();
			economic.method = AllocationMethod.ECONOMIC;
			process.allocationFactors.add(economic);
		}	else {
			factor.method = AllocationMethod.CAUSAL;
			factor.exchange = exchange;
			process.allocationFactors.add(factor);
		}

	}
}
