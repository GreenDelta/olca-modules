package org.openlca.core.model;

import java.util.Objects;

class ProcessCopy {

	public Process create(Process origin) {
		Process copy = new Process();
		Util.cloneRootFields(origin, copy);
		copyFields(origin, copy);
		copyParameters(origin, copy);
		copyExchanges(origin, copy);
		copyAllocationFactors(origin, copy);
		for (SocialAspect a : origin.socialAspects)
			copy.socialAspects.add(a.clone());
		return copy;
	}

	private void copyFields(Process origin, Process copy) {
		copy.setDefaultAllocationMethod(origin.getDefaultAllocationMethod());
		copy.setCategory(origin.getCategory());
		copy.setLocation(origin.getLocation());
		copy.setProcessType(origin.getProcessType());
		copy.setInfrastructureProcess(origin.isInfrastructureProcess());
		copy.currency = origin.currency;
		copy.dqEntry = origin.dqEntry;
		copy.dqSystem = origin.dqSystem;
		copy.exchangeDqSystem = origin.exchangeDqSystem;
		copy.socialDqSystem = origin.socialDqSystem;
		if (origin.getDocumentation() != null)
			copy.setDocumentation(origin.getDocumentation().clone());
	}

	private void copyExchanges(Process origin, Process copy) {
		for (Exchange exchange : origin.getExchanges()) {
			Exchange clone = exchange.clone();
			copy.getExchanges().add(clone);
			if (exchange.equals(origin.getQuantitativeReference()))
				copy.setQuantitativeReference(clone);
		}
	}

	private void copyParameters(Process origin, Process copy) {
		for (Parameter parameter : origin.getParameters()) {
			Parameter p = parameter.clone();
			copy.getParameters().add(p);
		}
	}

	private void copyAllocationFactors(Process origin, Process copy) {
		for (AllocationFactor factor : origin.getAllocationFactors()) {
			AllocationFactor clone = factor.clone();
			// not that the cloned factor has a reference to an exchange of
			// the original process
			Exchange copyExchange = findExchange(clone.getExchange(), copy);
			clone.setExchange(copyExchange);
			copy.getAllocationFactors().add(clone);
		}
	}

	private Exchange findExchange(Exchange origin, Process processCopy) {
		if (origin == null)
			return null;
		for (Exchange copy : processCopy.getExchanges()) {
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
