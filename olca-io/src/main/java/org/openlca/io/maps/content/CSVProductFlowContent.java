package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;

public class CSVProductFlowContent implements IMappingContent {

	private String name;

	public CSVProductFlowContent() {
	}

	public CSVProductFlowContent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getKey() {
		return KeyGen.get(name);
	}

}
