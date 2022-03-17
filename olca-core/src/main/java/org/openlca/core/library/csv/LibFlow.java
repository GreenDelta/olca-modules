package org.openlca.core.library.csv;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.library.Proto;
import org.openlca.core.model.FlowType;
import org.openlca.util.Strings;

/**
 * Contains the meta-data of a flow stored in a library index.
 */
public record LibFlow(
	String id,
	String name,
	String category,
	String unit,
	FlowType type) {

	private static final LibFlow empty = new LibFlow(
		null, null, null, null, null);

	static LibFlow empty() {
		return empty;
	}

	boolean isEmpty() {
		return id == null || id.isBlank();
	}

	Proto.Flow toProto() {
		var proto = Proto.Flow.newBuilder()
			.setId(Strings.orEmpty(id))
			.setName(Strings.orEmpty(name))
			.setCategory(Strings.orEmpty(category))
			.setUnit(Strings.orEmpty(unit));
		if (type != null) {
			proto.setType(type.name());
		}
		return proto.build();
	}

	static LibFlow fromProto(Proto.Flow proto) {
		return new LibFlow(
			proto.getId(),
			proto.getName(),
			proto.getCategory(),
			proto.getUnit(),
			typeOf(proto.getType())
		);
	}

	void toCsv(List<String> row) {
		row.add(Csv.str(id));
		row.add(Csv.str(name));
		row.add(Csv.str(category));
		row.add(Csv.str(unit));
		row.add(toCsv(type));
	}

	static LibFlow fromCsv(CSVRecord row, int offset) {
		return new LibFlow(
			Csv.read(row, offset),
			Csv.read(row, offset + 1),
			Csv.read(row, offset + 2),
			Csv.read(row, offset + 3),
			typeOf(Csv.read(row, offset + 4)));
	}

	private String toCsv(FlowType type) {
		if (type == null)
			return "";
		return switch (type) {
			case ELEMENTARY_FLOW -> "elementary";
			case PRODUCT_FLOW -> "product";
			case WASTE_FLOW -> "waste";
		};
	}

	private static FlowType typeOf(String s) {
		if (s == null || s.isBlank())
			return null;
		var c = Character.toLowerCase(s.charAt(0));
		return switch (c) {
			case 'e' -> FlowType.ELEMENTARY_FLOW;
			case 'p' -> FlowType.PRODUCT_FLOW;
			case 'w' -> FlowType.WASTE_FLOW;
			default -> null;
		};
	}
}
