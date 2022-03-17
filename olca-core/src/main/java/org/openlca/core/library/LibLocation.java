package org.openlca.core.library;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.util.Strings;

import java.util.List;

/**
 * Contains the meta-data of a location stored in a library index.
 */
public record LibLocation(String id, String name, String code) {

	private static final LibLocation empty = new LibLocation(null, null, null);

	public static LibLocation empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static LibLocation of(Location loc) {
		return loc != null
			? new LibLocation(loc.refId, loc.name, loc.code)
			: empty;
	}

	public static LibLocation of(LocationDescriptor d) {
		return d != null
			? new LibLocation(d.refId, d.name, d.code)
			: empty;
	}

	Proto.Location toProto() {
		return Proto.Location.newBuilder()
			.setId(Strings.orEmpty(id))
			.setName(Strings.orEmpty(name))
			.setCode(Strings.orEmpty(code))
			.build();
	}

	static LibLocation fromProto(Proto.Location proto) {
		return new LibLocation(
			proto.getId(),
			proto.getName(),
			proto.getCode());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(code));
	}

	static LibLocation fromCsv(CSVRecord row, int offset) {
		return new LibLocation(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2));
	}
}
