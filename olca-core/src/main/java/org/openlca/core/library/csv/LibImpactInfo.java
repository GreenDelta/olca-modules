package org.openlca.core.library.csv;

import java.util.List;

public record LibImpactInfo(
		String id,
		String name,
		String unit) {

	private static final LibImpactInfo empty = new LibImpactInfo(
			null, null, null);

	static LibImpactInfo empty() {
		return empty;
	}

	boolean isEmpty() {
		return id == null || id.isBlank();
	}

	void writeTo(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(unit));
	}

}
