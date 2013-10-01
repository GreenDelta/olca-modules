package org.openlca.io.csv.output;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPReferenceProduct;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;

class ProcessConverter {

	SPProcess spProcess;
	Process process;

	SPProcess convert(Process process) {
		this.process = process;
		spProcess = new SPProcess(referenceProduct());
		return spProcess;
	}

	private SPReferenceProduct referenceProduct() {
		Exchange exchange = process.getQuantitativeReference();
		SPReferenceProduct product = new SPReferenceProduct(exchange.getFlow()
				.getName(), exchange.getUnit().getName(),
				String.valueOf(exchange.getAmountValue()));
		product.setCategory(process.getCategory().getName());
		return product;
	}

	private void exchanges() {
		for (Exchange exchange : process.getExchanges()) {
			ElementaryFlowType type = null;

			SPElementaryFlow flow = new SPElementaryFlow(type, exchange
					.getFlow().getName(), exchange.getUnit().getName(),
					String.valueOf(exchange.getAmountValue()));
			spProcess.add(flow);
		}
	}
}
