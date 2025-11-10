package org.openlca.io.xls.process;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.openlca.commons.Strings;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

final class Out {

	private Out() {
	}

	static String pathOf(RootEntity e) {
		if (e instanceof Category c)
			return c.toPath();
		return e != null && e.category != null
			? e.category.toPath()
			: null;
	}

	static <T extends RefEntity> List<T> sort(Collection<T> set) {
		return set.stream().sorted((e1, e2) -> {
			if (e1 == null && e2 == null)
				return 0;
			if (e1 == null)
				return -1;
			if (e2 == null)
				return 1;
			int c = Strings.compareIgnoreCase(e1.name, e2.name);
			if (c != 0)
				return c;
			if (e1 instanceof RootEntity re1 && e2 instanceof RootEntity re2) {
				var c1 = Out.pathOf(re1);
				var c2 = Out.pathOf(re2);
				return Strings.compareIgnoreCase(c1, c2);
			}
			return 0;
		}).toList();
	}

	static void flowPropertiesOf(RootEntity e, Consumer<FlowProperty> fn) {
		if (e instanceof FlowProperty prop) {
			fn.accept(prop);
		} else if (e instanceof Flow flow) {
			for (var f : flow.flowPropertyFactors) {
				if (f.flowProperty != null) {
					fn.accept(f.flowProperty);
				}
			}
		}
	}

	static void unitGroupsOf(RootEntity e, Consumer<UnitGroup> fn) {
		if (e instanceof UnitGroup group) {
			fn.accept(group);
			return;
		}
		flowPropertiesOf(e, prop -> {
			if (prop.unitGroup != null) {
				fn.accept(prop.unitGroup);
			}
		});
	}
}
