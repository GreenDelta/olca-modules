package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block {

	public final String header;
	public final List<String> dataRows = new ArrayList<>();
	private final Map<String, Section> sections = new HashMap<>();

	public Block(String header) {
		this.header = header;
	}

	public Collection<Section> getSections() {
		return sections.values();
	}

	public void addSection(Section section) {
		if (section == null || section.header == null)
			return;
		sections.put(section.header, section);
	}

	public Section getSection(String header) {
		return sections.get(header);
	}

	@Override
	public String toString() {
		return "Block [header=" + header + "]";
	}

}
