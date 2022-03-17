package org.openlca.core.library;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.util.Strings;

/**
 * Contains the meta-data of a process stored in a library index.
 */
public record LibProcess(
	String id,
	String name,
	String category,
	String locationCode) {

	private static final LibProcess empty = new LibProcess(
		null, null, null, null);

	static LibProcess empty() {
		return empty;
	}

	boolean isEmpty() {
		return id == null || id.isBlank();
	}

	Proto.Process toProto() {
		return Proto.Process.newBuilder()
			.setId(Strings.orEmpty(id))
			.setName(Strings.orEmpty(name))
			.setCategory(Strings.orEmpty(category))
			.setLocationCode(Strings.orEmpty(locationCode))
			.build();
	}

	static LibProcess fromProto(Proto.Process proto) {
		return new LibProcess(
			proto.getId(),
			proto.getName(),
			proto.getCategory(),
			proto.getLocationCode());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(category));
		buffer.add(Csv.str(locationCode));
	}

	static LibProcess fromCsv(CSVRecord row, int offset) {
		return new LibProcess(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2),
			Csv.read(row, offset + 3));
	}
}
