package org.openlca.core.library.csv;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

public record LibProcessInfo(
		String id,
		String name,
		String category,
		String location) {

	private static final LibProcessInfo empty = new LibProcessInfo(
			null, null, null, null);

	static LibProcessInfo empty() {
		return empty;
	}

	static int size() {
		return 4;
	}

	boolean isEmpty() {
		return id == null || id.isBlank();
	}

	void writeTo(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(category));
		buffer.add(Csv.str(location));
	}

	static LibProcessInfo read(CSVRecord row, int offset) {
		return new LibProcessInfo(
				Csv.read(row, offset),
				Csv.read(row, offset + 1),
				Csv.read(row, offset + 2),
				Csv.read(row, offset + 3));
	}
}
