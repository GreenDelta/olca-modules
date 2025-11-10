package org.openlca.io.xls.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openlca.commons.Strings;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

class OutUnitSync implements OutEntitySync {

	private final OutConfig config;
	private final Set<UnitGroup> groups = new HashSet<>();

	OutUnitSync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		Out.unitGroupsOf(entity, groups::add);
	}

	@Override
	public void flush() {
		var sheet = config.createSheet(Tab.UNITS)
			.withColumnWidths(6, 25)
			.header(
				Field.UUID,
				Field.NAME,
				Field.UNIT_GROUP,
				Field.DESCRIPTION,
				Field.SYNONYMS,
				Field.CONVERSION_FACTOR);

		for (var item : getItems()) {
			sheet.next(row ->
				row.next(item.unit.refId)
					.next(item.unit.name)
					.next(item.group.name)
					.next(item.unit.description)
					.next(item.unit.synonyms)
					.next(item.unit.conversionFactor));
		}
	}

	private List<Item> getItems() {
		var items = new ArrayList<Item>();
		for (var group : groups) {
			for (var unit : group.units) {
				items.add(new Item(unit, group));
			}
		}
		Collections.sort(items);
		return items;
	}

	private record Item(Unit unit, UnitGroup group) implements Comparable<Item> {

		@Override
		public int compareTo(Item other) {
			return Objects.equals(group, other.group)
				? Strings.compareIgnoreCase(unit.name, other.unit.name)
				: Strings.compareIgnoreCase(group.name, other.group.name);
		}
	}
}
