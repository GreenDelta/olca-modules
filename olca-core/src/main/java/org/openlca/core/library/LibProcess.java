package org.openlca.core.library;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;
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

	public static LibProcess empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static LibProcess of(Process process) {
		if (process == null)
			return empty;
		return new LibProcess(
			process.refId,
			process.name,
			process.category != null
				? process.category.toPath()
				: null,
			process.location != null
				? process.location.code
				: null);
	}

	public static LibProcess of(ProcessDescriptor d, DbContext ctx) {
		if (d == null)
			return empty;
		var category = ctx.categories().pathOf(d.category);
		var loc = d.location != null
			? ctx.locationCodes().get(d.location)
			: null;
		return new LibProcess(
			d.refId,
			d.name,
			category,
			loc);
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
