package org.openlca.core.matrix.io.index;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.util.Strings;

/**
 * Contains the meta-data of a flow stored in an index.
 */
public record IxFlow(
	String id,
	String name,
	String category,
	String unit,
	FlowType type) {

	private static final IxFlow empty = new IxFlow(
		null, null, null, null, null);

	public static IxFlow empty() {
		return empty;
	}

	public boolean isEmpty() {
		return id == null || id.isBlank();
	}

	public static IxFlow of(Flow flow) {
		if (flow == null)
			return empty;
		var unit = flow.getReferenceUnit();
		return new IxFlow(
			flow.refId,
			flow.name,
			flow.category != null
				? flow.category.toPath()
				: null,
			unit != null ? unit.name : null,
			flow.flowType);
	}

	public static IxFlow of(FlowDescriptor d, IxContext ctx) {
		if (d == null)
			return empty;
		var category = ctx.categories().pathOf(d.category);
		var prop = ctx.quantities().get(d.refFlowPropertyId);
		var unit = prop != null
			? prop.getReferenceUnit()
			: null;
		return new IxFlow(
			d.refId,
			d.name,
			category,
			unit != null ? unit.name : null,
			d.flowType);
	}

	IxProto.Flow toProto() {
		var proto = IxProto.Flow.newBuilder()
			.setId(Strings.orEmpty(id))
			.setName(Strings.orEmpty(name))
			.setCategory(Strings.orEmpty(category))
			.setUnit(Strings.orEmpty(unit));
		if (type != null) {
			proto.setType(type.name());
		}
		return proto.build();
	}

	static IxFlow fromProto(IxProto.Flow proto) {
		return new IxFlow(
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

	static IxFlow fromCsv(CSVRecord row, int offset) {
		return new IxFlow(
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
