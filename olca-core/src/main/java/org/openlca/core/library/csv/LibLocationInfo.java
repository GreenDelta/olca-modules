package org.openlca.core.library.csv;

import java.util.List;

public record LibLocationInfo(
		String id,
		String name,
		String code) {

	private static final LibLocationInfo empty = new LibLocationInfo(
			null, null, null);

	static LibLocationInfo empty() {
		return empty;
	}

	boolean isEmpty() {
		return id == null || id.isBlank();
	}

	void writeTo(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(code));
	}
}
