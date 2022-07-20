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
	IxFlow flow,
	IxLocation location,
	boolean isInput
) {

	public static IxEnviItem of(int idx, EnviFlow item, IxContext ctx) {
		return new IxEnviItem(
			idx,
			IxFlow.of(item.flow(), ctx),
			IxLocation.of(item.location()),
			item.isInput());
	}

	public static IxEnviItem output(int idx, Flow flow) {
		return new IxEnviItem(idx, IxFlow.of(flow), null, false);
	}

	public static IxEnviItem input(int idx, Flow flow) {
		return new IxEnviItem(idx, IxFlow.of(flow), null, true);
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
			proto.hasFlow()
				? IxFlow.fromProto(proto.getFlow())
				: IxFlow.empty(),
			proto.hasLocation()
				? IxLocation.fromProto(proto.getLocation())
				: IxLocation.empty(),
			proto.getIsInput());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
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
			IxFlow.fromCsv(row, 1),
			IxLocation.fromCsv(row, 1 + Csv.FLOW_COLS),
			Csv.readBool(row, 1 + Csv.FLOW_COLS + Csv.LOCATION_COLS));
	}

}
