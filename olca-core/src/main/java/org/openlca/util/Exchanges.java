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
		if (e == null || e.isAvoided || e.flow == null)
			return false;
		var type = e.flow.flowType;
		if (type == null || type == FlowType.ELEMENTARY_FLOW)
			return false;
		return (type == FlowType.PRODUCT_FLOW) != e.isInput;
	}

	public static boolean isLinkable(Exchange e) {
		if (e == null || e.flow == null)
			return false;
		var type = e.flow.flowType;
		if (type == null || type == FlowType.ELEMENTARY_FLOW)
			return false;
		return (type == FlowType.PRODUCT_FLOW) == e.isInput;
	}

	public static boolean isProduct(Exchange e) {
		return e != null
				&& e.flow != null
				&& e.flow.flowType == FlowType.PRODUCT_FLOW;
	}

	public static boolean isWaste(Exchange e) {
		return e != null
				&& e.flow != null
				&& e.flow.flowType == FlowType.WASTE_FLOW;
	}
}
