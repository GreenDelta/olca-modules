package org.openlca.util;

import org.openlca.core.model.AbstractExchange;
import org.openlca.core.model.FlowType;

public class Exchanges {

	private Exchanges() {
	}

	/// A provider flow is a product output or waste input of a process. They
	/// can be linked to exchanges with the same flow of the opposite direction.
	public static boolean isProviderFlow(AbstractExchange e) {
		if (e == null || e.flow == null)
			return false;
		return switch (e.flow.flowType) {
			case PRODUCT_FLOW -> !e.isInput;
			case WASTE_FLOW -> e.isInput;
			case null, default -> false;
		};
	}

	/// Returns `true` when the given exchange can be linked to a provider. This
	/// is the case, when it is a product input or waste output.
	public static boolean isLinkable(AbstractExchange e) {
		if (e == null || e.flow == null)
			return false;
		return switch (e.flow.flowType) {
			case PRODUCT_FLOW -> e.isInput;
			case WASTE_FLOW -> !e.isInput;
			case null, default -> false;
		};
	}

	public static boolean isProduct(AbstractExchange e) {
		return e != null
				&& e.flow != null
				&& e.flow.flowType == FlowType.PRODUCT_FLOW;
	}

	public static boolean isWaste(AbstractExchange e) {
		return e != null
				&& e.flow != null
				&& e.flow.flowType == FlowType.WASTE_FLOW;
	}
}
