package org.openlca.io.ecospold1.importer;

import org.openlca.core.model.FlowType;

class FlowTypes {

	private FlowTypes() {
	}

	public static FlowType forInputGroup(int inputGroup) {
		switch (inputGroup) {
		case 1:
			return FlowType.ProductFlow;
		case 2:
			return FlowType.ProductFlow;
		case 3:
			return FlowType.ProductFlow;
		case 4:
			return FlowType.ElementaryFlow;
		case 5:
			return FlowType.ProductFlow;
		default:
			return null;
		}
	}
	
	public static FlowType forOutputGroup(int outputGroup) {
		switch (outputGroup) {
		case 0:
			return FlowType.ProductFlow;
		case 1:
			return FlowType.ProductFlow;
		case 2:
			return FlowType.ProductFlow;
		case 3:
			return FlowType.WasteFlow;
		case 4:
			return FlowType.ElementaryFlow;
		default:
			return null;
		}
	}
}
