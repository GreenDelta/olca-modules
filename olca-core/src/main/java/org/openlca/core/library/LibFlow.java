package org.openlca.core.library;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
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

	public static LibFlow empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static LibFlow of(Flow flow) {
		if (flow == null)
			return empty;
		var unit = flow.getReferenceUnit();
		return new LibFlow(
			flow.refId,
			flow.name,
			flow.category != null
				? flow.category.toPath()
				: null,
			unit != null ? unit.name : null,
			flow.flowType);
	}

	public static LibFlow of(FlowDescriptor d, DbContext ctx) {
		if (d == null)
			return empty;
		var category = ctx.categories().pathOf(d.category);
		var prop = ctx.quantities().get(d.refFlowPropertyId);
		var unit = prop != null
			? prop.getReferenceUnit()
			: null;
		return new LibFlow(
			d.refId,
			d.name,
			category,
			unit != null ? unit.name : null,
			d.flowType);
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

	void toCsv(List<String> buffer) {
		buffer.add(Csv.str(id));
		buffer.add(Csv.str(name));
		buffer.add(Csv.str(category));
		buffer.add(Csv.str(unit));
		buffer.add(toCsv(type));
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
