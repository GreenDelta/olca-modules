package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.RootEntity;

class OutFlowPropertySync implements OutEntitySync {

	private final OutConfig config;
	private final Set<FlowProperty> properties = new HashSet<>();

	OutFlowPropertySync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		Out.flowPropertiesOf(entity, properties::add);
	}

	@Override
	public void flush() {
		var sheet = config.createSheet(Tab.FLOW_PROPERTIES)
			.withColumnWidths(8, 25)
			.header(
				Field.UUID,
				Field.NAME,
				Field.DESCRIPTION,
				Field.CATEGORY,
				Field.UNIT_GROUP,
				Field.TYPE,
				Field.VERSION,
				Field.LAST_CHANGE);

		for (var p : Out.sort(properties)) {
			sheet.next(row ->
				row.next(p.refId)
					.next(p.name)
					.next(p.description)
					.next(Out.pathOf(p))
					.next(p.unitGroup != null
						? p.unitGroup.name
						: null)
					.next(p.flowPropertyType == FlowPropertyType.ECONOMIC
						? "Economic"
						: "Physical")
					.nextAsVersion(p.version)
					.nextAsDate(p.lastChange));
		}
	}
}
