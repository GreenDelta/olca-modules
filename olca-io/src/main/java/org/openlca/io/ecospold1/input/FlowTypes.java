package org.openlca.io.ecospold1.input;

import org.openlca.core.model.FlowType;

class FlowTypes {

	private FlowTypes() {
	}

	public static FlowType forInputGroup(int inputGroup) {
		return switch (inputGroup) {
			case 1, 2, 3, 5 -> FlowType.PRODUCT_FLOW;
			case 4 -> FlowType.ELEMENTARY_FLOW;
			default -> null;
		};
	}

	public static FlowType forOutputGroup(int outputGroup) {
		return switch (outputGroup) {
			case 0, 1, 2 -> FlowType.PRODUCT_FLOW;
			case 3 -> FlowType.WASTE_FLOW;
			case 4 -> FlowType.ELEMENTARY_FLOW;
			default -> null;
		};
	}
}
