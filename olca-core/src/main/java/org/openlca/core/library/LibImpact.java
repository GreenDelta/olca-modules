package org.openlca.core.library;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.util.Strings;

import java.util.List;

public record LibImpact(String id, String name, String unit) {

	private static final LibImpact empty = new LibImpact(null, null, null);

	public static LibImpact empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static LibImpact of(ImpactCategory imp) {
		return imp != null
			? new LibImpact(imp.refId, imp.name, imp.referenceUnit)
			: empty;
	}

	public static LibImpact of(ImpactDescriptor imp) {
		return imp != null
			? new LibImpact(imp.refId, imp.name, imp.referenceUnit)
			: empty;
	}

	Proto.Impact toProto() {
		return Proto.Impact.newBuilder()
			.setId(Strings.orEmpty(id))
			.setName(Strings.orEmpty(name))
			.setUnit(Strings.orEmpty(unit))
			.build();
	}

	static LibImpact fromProto(Proto.Impact proto) {
		return new LibImpact(
			proto.getId(),
			proto.getName(),
			proto.getUnit());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(unit));
	}

	static LibImpact fromCsv(CSVRecord row, int offset) {
		return new LibImpact(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2));
	}

}
