package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

public class Section {

	public final String header;
	public final List<String> dataRows = new ArrayList<>();

	public Section(String header) {
		this.header = header;
	}

	@Override
	public String toString() {
		return "Section [header=" + header + "]";
	}

}
