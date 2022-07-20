package org.openlca.core.matrix.io.index;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.matrix.io.index.IxProto.ProductEntry;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;

import java.util.List;

/**
 * An item of a library's technosphere index.
 */
public record IxTechItem(
	int index, IxProvider provider, IxFlow flow) {

	public static IxTechItem of(int idx, RootEntity provider, Flow flow) {
		return new IxTechItem(idx, IxProvider.of(provider), IxFlow.of(flow));
	}

	public static IxTechItem of(int idx, TechFlow item, IxContext ctx) {
		return new IxTechItem(
			idx,
			IxProvider.of(item.provider(), ctx),
			IxFlow.of(item.flow(), ctx));
	}

	ProductEntry toProto() {
		var proto = IxProto.ProductEntry.newBuilder()
			.setIndex(index);
		if (provider != null) {
			proto.setProcess(provider.toProto());
		}
		if (flow != null) {
			proto.setProduct(flow.toProto());
		}
		return proto.build();
	}

	static IxTechItem fromProto(IxProto.ProductEntry proto) {
		return new IxTechItem(
			proto.getIndex(),
			proto.hasProcess()
				? IxProvider.fromProto(proto.getProcess())
				: IxProvider.empty(),
			proto.hasProduct()
				? IxFlow.fromProto(proto.getProduct())
				: IxFlow.empty());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
		var p = provider != null
			? provider
			: IxProvider.empty();
		p.toCsv(buffer);
		var f = flow != null
			? flow
			: IxFlow.empty();
		f.toCsv(buffer);
	}

	static IxTechItem fromCsv(CSVRecord row) {
		return new IxTechItem(
			Csv.readInt(row, 0),
			IxProvider.fromCsv(row, 1),
			IxFlow.fromCsv(row, 1 + Csv.PROCESS_COLS));
	}

}
