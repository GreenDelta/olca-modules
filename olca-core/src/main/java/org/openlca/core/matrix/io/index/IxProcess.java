package org.openlca.core.matrix.io.index;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.Strings;

/**
 * Contains the meta-data of a process stored in a library index.
 */
public record IxProcess(
	String id,
	String name,
	String category,
	String locationCode) {

	private static final IxProcess empty = new IxProcess(
		null, null, null, null);

	public static IxProcess empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static IxProcess of(RootEntity process) {
		if (process == null)
			return empty;
		return new IxProcess(
			process.refId,
			process.name,
			process.category != null
				? process.category.toPath()
				: null,
			process instanceof Process p && p.location != null
				? p.location.code
				: null);
	}

	public static IxProcess of(RootDescriptor d, IxContext ctx) {
		if (d == null)
			return empty;
		var category = ctx.categories().pathOf(d.category);
		var loc = d instanceof ProcessDescriptor p && p.location != null
			? ctx.locationCodes().get(p.location)
			: null;
		return new IxProcess(
			d.refId,
			d.name,
			category,
			loc);
	}

	IxProto.Process toProto() {
		return IxProto.Process.newBuilder()
			.setId(Strings.orEmpty(id))
			.setName(Strings.orEmpty(name))
			.setCategory(Strings.orEmpty(category))
			.setLocationCode(Strings.orEmpty(locationCode))
			.build();
	}

	static IxProcess fromProto(IxProto.Process proto) {
		return new IxProcess(
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

	static IxProcess fromCsv(CSVRecord row, int offset) {
		return new IxProcess(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2),
			Csv.read(row, offset + 3));
	}
}
