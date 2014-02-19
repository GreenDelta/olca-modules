package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;
import org.openlca.simapro.csv.model.types.Geography;

public class CSVGeographyContent implements IMappingContent {

	private Geography geography;

	public CSVGeographyContent() {
	}

	public CSVGeographyContent(Geography category) {
		this.geography = category;
	}

	public Geography getGeography() {
		return geography;
	}

	public void setGeography(Geography geography) {
		this.geography = geography;
	}

	@Override
	public String getKey() {
		return KeyGen.get(geography.getValue());
	}

}
