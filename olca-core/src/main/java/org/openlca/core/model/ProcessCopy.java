package org.openlca.core.model;

import java.util.UUID;

class ProcessCopy {

	public Process create(Process self) {
		Process other = new Process(UUID.randomUUID().toString(),
				self.getName());
		copyFields(self, other);
		copyExchanges(self, other);
		copyParameters(self, other);
		return other;
	}

	private void copyFields(Process self, Process other) {
		other.setAllocationMethod(self.getAllocationMethod());
		other.setCategoryId(self.getCategoryId());
		other.setDescription(self.getDescription());
		other.setGeographyComment(self.getGeographyComment());
		other.setInfrastructureProcess(self.isInfrastructureProcess());
		other.setLocation(self.getLocation());
		other.setProcessType(self.getProcessType());
	}

	private void copyExchanges(Process self, Process other) {
		for (Exchange exchange : self.getExchanges()) {
			Exchange otherExchange = new Exchange(other.getId());
			copyExchangeFields(exchange, otherExchange);
			other.add(otherExchange);
			if (exchange.equals(self.getQuantitativeReference())) {
				other.setQuantitativeReference(otherExchange);
			}
		}
	}

	private void copyExchangeFields(Exchange self, Exchange other) {
		other.setId(UUID.randomUUID().toString());
		other.setAvoidedProduct(self.isAvoidedProduct());
		other.getResultingAmount().setValue(
				self.getResultingAmount().getValue());
		other.getResultingAmount().setFormula(
				self.getResultingAmount().getFormula());
		other.setFlow(self.getFlow());
		other.setFlowPropertyFactor(self.getFlowPropertyFactor());
		other.setInput(self.isInput());
		other.setUnit(self.getUnit());
	}

	private void copyParameters(Process self, Process other) {
		for (Parameter parameter : self.getParameters()) {
			Parameter p = new Parameter(UUID.randomUUID().toString(),
					new Expression(parameter.getExpression().getFormula(),
							parameter.getExpression().getValue()),
					ParameterType.PROCESS, other.getId());
			p.setDescription(parameter.getDescription());
			p.setName(parameter.getName());
			other.add(p);
		}
	}

}
