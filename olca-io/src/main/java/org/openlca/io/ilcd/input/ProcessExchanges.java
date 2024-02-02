package org.openlca.io.ilcd.input;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.Processes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Maps the inputs and outputs of an ILCD process to an openLCA process.
 */
class ProcessExchanges {

	private final Import imp;
	private final AtomicBoolean hasRefErrors;

	ProcessExchanges(Import imp) {
		this.imp = imp;
		this.hasRefErrors = new AtomicBoolean(false);
	}

	void map(org.openlca.ilcd.processes.Process ds, Process process) {
		int maxID = 0;

		var mappedExchanges = new ArrayList<MappedExchange>();
		for (var origin : ds.getExchanges()) {
			var flowId = origin.getFlow() != null
					? origin.getFlow().getUUID()
					: null;
			if (flowId == null) {
				imp.log().warn("invalid flow references in process "
						+ Processes.getUUID(ds));
				continue;
			}

			var flow = FlowImport.get(imp, flowId);
			if (flow.isEmpty()) {
				if (!hasRefErrors.get()) {
					hasRefErrors.set(true);
					imp.log().error("missing flows in process: "
							+ Processes.getUUID(ds) + "; flow=" + flowId);
				}
				continue;
			}

			var mapped = MappedExchange.of(flow, origin);
			mappedExchanges.add(mapped);
			if (mapped.hasExtensionError()) {
				imp.log().warn("invalid exchange extensions in process: "
						+ Processes.getUUID(ds));
			}

			var exchange = mapped.exchange();
			exchange.description = imp.str(origin.getComment());
			exchange.location = imp.cache.locationOf(origin.getLocation());

			// we take the internal IDs from ILCD
			maxID = Math.max(maxID, exchange.internalId);

			// add a possible mapped provider
			var providerId = mapped.providerId();
			if (providerId != null) {
				imp.providers().add(providerId, exchange);
			}

			process.exchanges.add(exchange);
		} // for each exchange

		process.lastInternalId = maxID;
		mapAllocation(process, mappedExchanges);

		// map the reference flow of the process
		var mappedIndex = new HashMap<Integer, Exchange>();
		for (var m : mappedExchanges) {
			mappedIndex.put(m.origin().getId(), m.exchange());
		}
		RefFlow.map(ds, process, mappedIndex);
	}


	private void mapAllocation(Process process, List<MappedExchange> mapped) {
		for (var m : mapped) {
			var factors = m.origin().getAllocations();
			if (factors == null)
				continue;
			for (var f : factors) {

				// find the product ID of the factor
				var productId = mapped.stream()
						.filter(e -> e.origin().getId() == f.getProductExchangeId())
						.map(e -> e.exchange().flow)
						.filter(Objects::nonNull)
						.mapToLong(flow -> flow.id)
						.findAny();
				if (productId.isEmpty())
					continue;

				// create the allocation factor
				createAllocationFactor(
						m.exchange(), productId.getAsLong(), f.getFraction(), process);
			}
		}
	}

	private void createAllocationFactor(
			Exchange exchange, long productId, double fraction, Process process) {
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
		} else {
			factor.method = AllocationMethod.CAUSAL;
			factor.exchange = exchange;
			process.allocationFactors.add(factor);
		}

	}
}
