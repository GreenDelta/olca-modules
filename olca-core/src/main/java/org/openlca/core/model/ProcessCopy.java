package org.openlca.core.model;

import java.util.UUID;

class ProcessCopy {

	public Process create(Process self) {
		Process other = new Process();
		other.setRefId(UUID.randomUUID().toString());
		other.setName(self.getName());
		copyFields(self, other);
		copyParameters(self, other);
		copyExchanges(self, other);
		for (AllocationFactor factor : self.getAllocationFactors())
			other.getAllocationFactors().add(factor.clone());
		return other;
	}

	private void copyFields(Process self, Process other) {
		other.setDefaultAllocationMethod(self.getDefaultAllocationMethod());
		other.setCategory(self.getCategory());
		other.setDescription(self.getDescription());
		other.setLocation(self.getLocation());
		other.setProcessType(self.getProcessType());
		other.setInfrastructureProcess(self.isInfrastructureProcess());
		if (self.getDocumentation() != null)
			other.setDocumentation(self.getDocumentation().clone());
	}

	private void copyExchanges(Process self, Process other) {
		for (Exchange exchange : self.getExchanges()) {
			Exchange clone = exchange.clone();
			other.getExchanges().add(clone);
			if (exchange.equals(self.getQuantitativeReference()))
				other.setQuantitativeReference(clone);
		}
	}

	private void copyParameters(Process self, Process other) {
		for (Parameter parameter : self.getParameters()) {
			Parameter p = parameter.clone();
			other.getParameters().add(p);
		}
	}
}
