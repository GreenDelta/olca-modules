package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

public class Section {

	private final String header;
	private final List<String> dataRows = new ArrayList<>();

	public Section(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	public List<String> getDataRows() {
		return dataRows;
	}

	@Override
	public String toString() {
		return "Section [header=" + header + "]";
	}

}
