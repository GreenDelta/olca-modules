package org.openlca.core.matrix.io.index;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.Strings;

/**
 * Contains the meta-data of a provider stored in an index. A provider is
 * a process, product system, or result that provides a product output or
 * waste input that can be linked to other processes or serve as the final
 * demand of a product system.
 */
public record IxProvider(
	String id,
	String name,
	String category,
	String locationCode) {

	private static final IxProvider empty = new IxProvider(
		null, null, null, null);

	public static IxProvider empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static IxProvider of(RootEntity process) {
		if (process == null)
			return empty;
		return new IxProvider(
			process.refId,
			process.name,
			process.category != null
				? process.category.toPath()
				: null,
			process instanceof Process p && p.location != null
				? p.location.code
				: null);
	}

	public static IxProvider of(RootDescriptor d, IxContext ctx) {
		if (d == null)
			return empty;
		var category = ctx.categories().pathOf(d.category);
		var loc = d instanceof ProcessDescriptor p && p.location != null
			? ctx.locationCodes().get(p.location)
			: null;
		return new IxProvider(
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

	static IxProvider fromProto(IxProto.Process proto) {
		return new IxProvider(
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

	static IxProvider fromCsv(CSVRecord row, int offset) {
		return new IxProvider(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2),
			Csv.read(row, offset + 3));
	}
}
