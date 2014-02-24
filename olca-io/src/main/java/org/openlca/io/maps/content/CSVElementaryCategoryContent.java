package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.SubCompartment;

public class CSVElementaryCategoryContent implements IMappingContent {

	private ElementaryFlowType type;
	private SubCompartment subCompartment;

	public CSVElementaryCategoryContent() {
	}

	public CSVElementaryCategoryContent(ElementaryFlowType type) {
		this.type = type;
	}

	public ElementaryFlowType getType() {
		return type;
	}

	public void setType(ElementaryFlowType type) {
		this.type = type;
	}

	public SubCompartment getSubCompartment() {
		return subCompartment;
	}

	public void setSubCompartment(SubCompartment subCompartment) {
		this.subCompartment = subCompartment;
	}

	@Override
	public String getKey() {
		return KeyGen.get(type.getValue() + subCompartment.getValue());
	}

}
