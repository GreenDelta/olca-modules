package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;

public class ES1UnitContent implements IMappingContent {

	private String name;

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
