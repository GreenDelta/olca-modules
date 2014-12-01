package org.openlca.io.ecospold1.input;

import org.openlca.core.model.FlowType;

class FlowTypes {

	private FlowTypes() {
	}

	public static FlowType forInputGroup(int inputGroup) {
		switch (inputGroup) {
		case 1:
			return FlowType.PRODUCT_FLOW;
		case 2:
			return FlowType.PRODUCT_FLOW;
		case 3:
			return FlowType.PRODUCT_FLOW;
		case 4:
			return FlowType.ELEMENTARY_FLOW;
		case 5:
			return FlowType.PRODUCT_FLOW;
		default:
			return null;
		}
	}
	
	public static FlowType forOutputGroup(int outputGroup) {
		switch (outputGroup) {
		case 0:
			return FlowType.PRODUCT_FLOW;
		case 1:
			return FlowType.PRODUCT_FLOW;
		case 2:
			return FlowType.PRODUCT_FLOW;
		case 3:
			return FlowType.WASTE_FLOW;
		case 4:
			return FlowType.ELEMENTARY_FLOW;
		default:
			return null;
		}
	}
}
