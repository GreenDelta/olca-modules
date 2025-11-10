package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

class OutUnitGroupSync implements OutEntitySync {

	private final OutConfig config;
	private final Set<UnitGroup> groups = new HashSet<>();

	OutUnitGroupSync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		Out.unitGroupsOf(entity, groups::add);
	}

	@Override
	public void flush() {
		var sheet = config.createSheet(Tab.UNIT_GROUPS)
			.withColumnWidths(8, 25)
			.header(
				Field.UUID,
				Field.NAME,
				Field.CATEGORY,
				Field.DESCRIPTION,
				Field.REFERENCE_UNIT,
				Field.DEFAULT_FLOW_PROPERTY,
				Field.VERSION,
				Field.LAST_CHANGE);

		for (var group : Out.sort(groups)) {
			sheet.next(row ->
				row.next(group.refId)
					.next(group.name)
					.next(Out.pathOf(group))
					.next(group.description)
					.next(group.referenceUnit != null
						? group.referenceUnit.name
						: null)
					.next(group.defaultFlowProperty != null
						? group.defaultFlowProperty.name
						: null)
					.nextAsVersion(group.version)
					.nextAsDate(group.lastChange));
		}
	}
}
