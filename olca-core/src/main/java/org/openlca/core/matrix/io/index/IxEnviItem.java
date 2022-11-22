package org.openlca.core.matrix.io.index;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.matrix.io.index.IxProto.ElemFlowEntry;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.Flow;

import java.util.List;

/**
 * An item of an intervention index.
 */
public record IxEnviItem(
		int index,
		boolean isInput,
		IxFlow flow,
		IxLocation location) {

	public static IxEnviItem of(int idx, EnviFlow item, IxContext ctx) {
		return new IxEnviItem(
				idx,
				item.isInput(),
				IxFlow.of(item.flow(), ctx),
				IxLocation.of(item.location()));
	}

	public static IxEnviItem output(int idx, Flow flow) {
		return new IxEnviItem(idx, false, IxFlow.of(flow), null);
	}

	public static IxEnviItem input(int idx, Flow flow) {
		return new IxEnviItem(idx, true, IxFlow.of(flow), null);
	}

	ElemFlowEntry toProto() {
		var proto = IxProto.ElemFlowEntry.newBuilder()
				.setIndex(index)
				.setIsInput(isInput);
		if (flow != null) {
			proto.setFlow(flow.toProto());
		}
		if (location != null) {
			proto.setLocation(location.toProto());
		}
		return proto.build();
	}

	static IxEnviItem fromProto(IxProto.ElemFlowEntry proto) {
		return new IxEnviItem(
				proto.getIndex(),
				proto.getIsInput(),
				proto.hasFlow()
						? IxFlow.fromProto(proto.getFlow())
						: IxFlow.empty(),
				proto.hasLocation()
						? IxLocation.fromProto(proto.getLocation())
						: IxLocation.empty());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
		buffer.add(Boolean.toString(isInput));
		if (flow == null) {
			IxFlow.empty().toCsv(buffer);
		} else {
			flow.toCsv(buffer);
		}
		if (location == null) {
			IxLocation.empty().toCsv(buffer);
		} else {
			location.toCsv(buffer);
		}
	}

	static IxEnviItem fromCsv(CSVRecord row) {
		return new IxEnviItem(
				Csv.readInt(row, 0),
				Csv.readBool(row, 1),
				IxFlow.fromCsv(row, 2),
				IxLocation.fromCsv(row, 2 + Csv.FLOW_COLS));
	}

}
