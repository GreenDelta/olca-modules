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
		other.setDocumentation(self.getDocumentation().clone());
	}

	private void copyExchanges(Process self, Process other) {
		for (Exchange exchange : self.getExchanges()) {
			Exchange otherExchange = new Exchange();
			copyExchangeFields(exchange, otherExchange);
			other.getExchanges().add(otherExchange);
			if (exchange.equals(self.getQuantitativeReference())) {
				other.setQuantitativeReference(otherExchange);
			}
		}
	}

	private void copyExchangeFields(Exchange self, Exchange other) {
		other.setAvoidedProduct(self.isAvoidedProduct());
		other.setAmountFormula(self.getAmountFormula());
		other.setAmountValue(self.getAmountValue());
		if (self.getUncertainty() != null)
			other.setUncertainty(self.getUncertainty().clone());
		other.setFlow(self.getFlow());
		other.setFlowPropertyFactor(self.getFlowPropertyFactor());
		other.setInput(self.isInput());
		other.setUnit(self.getUnit());
	}

	private void copyParameters(Process self, Process other) {
		for (Parameter parameter : self.getParameters()) {
			Parameter p = new Parameter();
			p.setDescription(parameter.getDescription());
			p.setName(parameter.getName());
			p.setScope(ParameterScope.PROCESS);
			p.setFormula(parameter.getFormula());
			p.setValue(parameter.getValue());
			other.getParameters().add(p);
		}
	}
}
