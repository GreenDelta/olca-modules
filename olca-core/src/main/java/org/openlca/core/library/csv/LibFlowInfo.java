package org.openlca.core.library.csv;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.FlowType;

public record LibFlowInfo(
		String id,
		String name,
		String category,
		String unit,
		FlowType type) {

	private static final LibFlowInfo empty = new LibFlowInfo(
			null, null, null, null, null);

	static LibFlowInfo empty() {
		return empty;
	}

	boolean isEmpty() {
		return id == null || id.isBlank();
	}

	void writeTo(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(category));
		buffer.add(Csv.str(unit));
		buffer.add(toCsv(type));
	}

	static LibFlowInfo read(CSVRecord row, int offset) {
		return new LibFlowInfo(
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
