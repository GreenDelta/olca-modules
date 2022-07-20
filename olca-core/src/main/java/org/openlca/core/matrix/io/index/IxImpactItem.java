package org.openlca.core.matrix.io.index;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.matrix.io.index.IxProto.ImpactEntry;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import java.util.List;

public record IxImpactItem(int index, IxImpact impact) {

	public static IxImpactItem of(int idx, ImpactDescriptor impact) {
		return new IxImpactItem(idx, IxImpact.of(impact));
	}

	ImpactEntry toProto() {
		var proto = IxProto.ImpactEntry.newBuilder()
			.setIndex(index);
		if (impact != null) {
			proto.setImpact(impact.toProto());
		}
		return proto.build();
	}

	static IxImpactItem fromProto(IxProto.ImpactEntry proto) {
		return new IxImpactItem(
			proto.getIndex(),
			proto.hasImpact()
				? IxImpact.fromProto(proto.getImpact())
				: IxImpact.empty());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
		if (impact == null) {
			IxImpact.empty().toCsv(buffer);
		} else {
			impact.toCsv(buffer);
		}
	}

	static IxImpactItem fromCsv(CSVRecord row) {
		return new IxImpactItem(
			Csv.readInt(row, 0),
			IxImpact.fromCsv(row, 1));
	}

}
