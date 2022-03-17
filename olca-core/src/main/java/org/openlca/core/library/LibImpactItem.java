package org.openlca.core.library;

import org.apache.commons.csv.CSVRecord;

import java.util.List;

public record LibImpactItem(int index, LibImpact impact) {

	Proto.ImpactEntry toProto() {
		var proto = Proto.ImpactEntry.newBuilder()
			.setIndex(index);
		if (impact != null) {
			proto.setImpact(impact.toProto());
		}
		return proto.build();
	}

	static LibImpactItem fromProto(Proto.ImpactEntry proto) {
		return new LibImpactItem(
			proto.getIndex(),
			proto.hasImpact()
				? LibImpact.fromProto(proto.getImpact())
				: LibImpact.empty());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
		if (impact == null) {
			LibImpact.empty().toCsv(buffer);
		} else {
			impact.toCsv(buffer);
		}
	}

	static LibImpactItem fromCsv(CSVRecord row) {
		return new LibImpactItem(
			Csv.readInt(row, 0),
			LibImpact.fromCsv(row, 1));
	}

}
