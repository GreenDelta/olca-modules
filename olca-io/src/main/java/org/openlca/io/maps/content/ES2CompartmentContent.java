package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;

public class ES2CompartmentContent implements IMappingContent {

	private String compartmentId;
	private String subCompartmentId;

	public ES2CompartmentContent() {

	}

	public ES2CompartmentContent(String compartmentId, String subCompartmentId) {
		this.compartmentId = compartmentId;
		this.subCompartmentId = subCompartmentId;
	}

	public String getCompartmentId() {
		return compartmentId;
	}

	public void setCompartmentId(String compartmentId) {
		this.compartmentId = compartmentId;
	}

	public String getSubCompartmentId() {
		return subCompartmentId;
	}

	public void setSubCompartmentId(String subCompartmentId) {
		this.subCompartmentId = subCompartmentId;
	}

	@Override
	public String getKey() {
		return KeyGen.get(compartmentId + subCompartmentId);
	}

}
