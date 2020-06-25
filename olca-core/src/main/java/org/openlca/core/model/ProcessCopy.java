package org.openlca.core.model;

import java.util.Objects;

class ProcessCopy {

	public Process create(Process origin) {
		Process copy = new Process();
		Util.copyFields(origin, copy);
		copyFields(origin, copy);
		copyParameters(origin, copy);
		copyExchanges(origin, copy);
		copyAllocationFactors(origin, copy);
		for (SocialAspect a : origin.socialAspects)
			copy.socialAspects.add(a.clone());
		return copy;
	}

	private void copyFields(Process origin, Process copy) {
		copy.lastInternalId = origin.lastInternalId;
		copy.defaultAllocationMethod = origin.defaultAllocationMethod;
		copy.location = origin.location;
		copy.processType = origin.processType;
		copy.infrastructureProcess = origin.infrastructureProcess;
		copy.currency = origin.currency;
		copy.dqEntry = origin.dqEntry;
		copy.dqSystem = origin.dqSystem;
		copy.exchangeDqSystem = origin.exchangeDqSystem;
		copy.socialDqSystem = origin.socialDqSystem;
		if (origin.documentation != null)
			copy.documentation = origin.documentation.clone();
	}

	private void copyExchanges(Process origin, Process copy) {
		for (Exchange exchange : origin.exchanges) {
			Exchange clone = exchange.clone();
			copy.exchanges.add(clone);
			if (exchange.equals(origin.quantitativeReference))
				copy.quantitativeReference = clone;
		}
	}

	private void copyParameters(Process origin, Process copy) {
		for (Parameter parameter : origin.parameters) {
			Parameter p = parameter.clone();
			copy.parameters.add(p);
		}
	}

	private void copyAllocationFactors(Process origin, Process copy) {
		for (AllocationFactor factor : origin.allocationFactors) {
			AllocationFactor clone = factor.clone();
			// not that the cloned factor has a reference to an exchange of
			// the original process
			clone.exchange = findExchange(clone.exchange, copy);
			copy.allocationFactors.add(clone);
		}
	}

	private Exchange findExchange(Exchange origin, Process processCopy) {
		if (origin == null)
			return null;
		for (Exchange copy : processCopy.exchanges) {
			boolean equal = origin.isInput == copy.isInput
					&& Objects.equals(origin.flow, copy.flow)
					&& origin.amount == copy.amount
					&& Objects.equals(origin.unit, copy.unit);
			if (equal)
				return copy;
		}
		return null;
	}
}
