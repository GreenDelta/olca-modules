package org.openlca.core.matrix.io.index;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.matrix.io.index.Proto.ProductEntry;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;

import java.util.List;

/**
 * An item of a library's technosphere index.
 */
public record IxTechItem(
	int index, IxProcess process, IxFlow flow) {

	public static IxTechItem of(int idx, RootEntity provider, Flow flow) {
		return new IxTechItem(idx, IxProcess.of(provider), IxFlow.of(flow));
	}

	public static IxTechItem of(int idx, TechFlow item, IxContext ctx) {
		return new IxTechItem(
			idx,
			IxProcess.of(item.provider(), ctx),
			IxFlow.of(item.flow(), ctx));
	}

	ProductEntry toProto() {
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

	static IxTechItem fromProto(Proto.ProductEntry proto) {
		return new IxTechItem(
			proto.getIndex(),
			proto.hasProcess()
				? IxProcess.fromProto(proto.getProcess())
				: IxProcess.empty(),
			proto.hasProduct()
				? IxFlow.fromProto(proto.getProduct())
				: IxFlow.empty());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
		var p = process != null
			? process
			: IxProcess.empty();
		p.toCsv(buffer);
		var f = flow != null
			? flow
			: IxFlow.empty();
		f.toCsv(buffer);
	}

	static IxTechItem fromCsv(CSVRecord row) {
		return new IxTechItem(
			Csv.readInt(row, 0),
			IxProcess.fromCsv(row, 1),
			IxFlow.fromCsv(row, 1 + Csv.PROCESS_COLS));
	}

}
