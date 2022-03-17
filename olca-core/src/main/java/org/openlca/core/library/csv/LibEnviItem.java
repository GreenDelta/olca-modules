package org.openlca.core.library.csv;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.library.Proto;

import java.util.List;

/**
 * An item of a library's intervention index.
 */
public record LibEnviItem(
	int index,
	LibFlow flow,
	LibLocation location,
	boolean isInput
) {

	Proto.ElemFlowEntry toProto() {
		var proto = Proto.ElemFlowEntry.newBuilder();
		proto.setIndex(index);
		if (flow != null) {
			proto.setFlow(flow.toProto());
		}
		if (location != null) {
			proto.setLocation(location.toProto());
		}
		return proto.build();
	}

	static LibEnviItem fromProto(Proto.ElemFlowEntry proto) {
		return new LibEnviItem(
			proto.getIndex(),
			proto.hasFlow()
				? LibFlow.fromProto(proto.getFlow())
				: LibFlow.empty(),
			proto.hasLocation()
				? LibLocation.fromProto(proto.getLocation())
				: LibLocation.empty(),
			proto.getIsInput());
	}

	void toCsv(List<String> buffer) {
		buffer.add(Integer.toString(index));
		if (flow == null) {
			LibFlow.empty().toCsv(buffer);
		} else {
			flow.toCsv(buffer);
		}
		if (location == null) {
			LibLocation.empty().toCsv(buffer);
		} else {
			location.toCsv(buffer);
		}
	}

	static LibEnviItem fromCsv(CSVRecord row) {
		return new LibEnviItem(
			Csv.readInt(row, 0),
			LibFlow.fromCsv(row, 1),
			LibLocation.fromCsv(row, 1 + Csv.FLOW_COLS),
			Csv.readBool(row, 1 + Csv.FLOW_COLS + Csv.LOCATION_COLS));
	}

}
