package org.openlca.util;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;

public class Exchanges {

	private Exchanges() {
	}

	/**
	 * A provider flow is a product output or waste input of a process
	 * (which is not tagged as avoided flow).
	 */
	public static boolean isProviderFlow(Exchange e) {
		if (e.isAvoided || e.flow == null)
			return false;
		var type = e.flow.flowType;
		if (type == null || type == FlowType.ELEMENTARY_FLOW)
			return false;
		return (type == FlowType.PRODUCT_FLOW) != e.isInput;
	}
}
