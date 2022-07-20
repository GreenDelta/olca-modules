package org.openlca.core.matrix.io.index;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.matrix.io.index.IxProto.Impact;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.util.Strings;

import java.util.List;

public record IxImpact(String id, String name, String unit) {

	private static final IxImpact empty = new IxImpact(null, null, null);

	public static IxImpact empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static IxImpact of(ImpactCategory imp) {
		return imp != null
			? new IxImpact(imp.refId, imp.name, imp.referenceUnit)
			: empty;
	}

	public static IxImpact of(ImpactDescriptor imp) {
		return imp != null
			? new IxImpact(imp.refId, imp.name, imp.referenceUnit)
			: empty;
	}

	Impact toProto() {
		return IxProto.Impact.newBuilder()
			.setId(Strings.orEmpty(id))
			.setName(Strings.orEmpty(name))
			.setUnit(Strings.orEmpty(unit))
			.build();
	}

	static IxImpact fromProto(IxProto.Impact proto) {
		return new IxImpact(
			proto.getId(),
			proto.getName(),
			proto.getUnit());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(unit));
	}

	static IxImpact fromCsv(CSVRecord row, int offset) {
		return new IxImpact(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2));
	}

}
