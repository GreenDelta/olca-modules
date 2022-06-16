package org.openlca.core.library;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;

import java.util.List;

/**
 * An item of a library's technosphere index.
 */
public record LibTechItem(
	int index, LibProcess process, LibFlow flow) {

	public static LibTechItem of(int idx, RootEntity provider, Flow flow) {
		return new LibTechItem(idx, LibProcess.of(provider), LibFlow.of(flow));
	}

	public static LibTechItem of(int idx, TechFlow item, DbContext ctx) {
		return new LibTechItem(
			idx,
			LibProcess.of(item.provider(), ctx),
			LibFlow.of(item.flow(), ctx));
	}

	Proto.ProductEntry toProto() {
		var proto = Proto.ProductEntry.newBuilder()
			.setIndex(index);
		if (process != null) {
			proto.setProcess(process.toProto());
		}
		if (flow != null) {
			proto.setProduct(flow.toProto());
		}
		return proto.build();
	}

	static LibTechItem fromProto(Proto.ProductEntry proto) {
		return new LibTechItem(
			proto.getIndex(),
			proto.hasProcess()
				? LibProcess.fromProto(proto.getProcess())
				: LibProcess.empty(),
			proto.hasProduct()
				? LibFlow.fromProto(proto.getProduct())
				: LibFlow.empty());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
		var p = process != null
			? process
			: LibProcess.empty();
		p.toCsv(buffer);
		var f = flow != null
			? flow
			: LibFlow.empty();
		f.toCsv(buffer);
	}

	static LibTechItem fromCsv(CSVRecord row) {
		return new LibTechItem(
			Csv.readInt(row, 0),
			LibProcess.fromCsv(row, 1),
			LibFlow.fromCsv(row, 1 + Csv.PROCESS_COLS));
	}

}
