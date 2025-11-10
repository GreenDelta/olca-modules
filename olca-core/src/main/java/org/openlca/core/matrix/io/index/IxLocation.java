package org.openlca.core.matrix.io.index;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.commons.Strings;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * Contains the meta-data of a location stored in an index.
 */
public record IxLocation(String id, String name, String code) {

	private static final IxLocation empty = new IxLocation(null, null, null);

	public static IxLocation empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static IxLocation of(Location loc) {
		return loc != null
			? new IxLocation(loc.refId, loc.name, loc.code)
			: empty;
	}

	public static IxLocation of(LocationDescriptor d) {
		return d != null
			? new IxLocation(d.refId, d.name, d.code)
			: empty;
	}

	IxProto.Location toProto() {
		return IxProto.Location.newBuilder()
			.setId(Strings.notNull(id))
			.setName(Strings.notNull(name))
			.setCode(Strings.notNull(code))
			.build();
	}

	static IxLocation fromProto(IxProto.Location proto) {
		return new IxLocation(
			proto.getId(),
			proto.getName(),
			proto.getCode());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(code));
	}

	static IxLocation fromCsv(CSVRecord row, int offset) {
		return new IxLocation(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2));
	}
}
