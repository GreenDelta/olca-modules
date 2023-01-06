package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

class Util {

	static void write(Sheet sheet, int row, int offset, Uncertainty u) {
		var r = Excel.row(sheet, row);
		write(r, offset, u);
	}

	static void write(Row row, int offset, Uncertainty u) {
		if (u == null || u.distributionType == UncertaintyType.NONE) {
			Excel.cell(row, offset, "");
			return;
		}

		switch (u.distributionType) {
			case LOG_NORMAL -> {
				Excel.cell(row, offset, "log-normal");
				param(row, u.parameter1, offset + 1);
				param(row, u.parameter2, offset + 2);
			}
			case NORMAL -> {
				Excel.cell(row, offset, "normal");
				param(row, u.parameter1, offset + 1);
				param(row, u.parameter2, offset + 2);
			}
			case TRIANGLE -> {
				Excel.cell(row, offset, "triangular");
				param(row, u.parameter1, offset + 3);
				param(row, u.parameter2, offset + 1);
				param(row, u.parameter3, offset + 4);
			}
			case UNIFORM -> {
				Excel.cell(row, offset, "uniform");
				param(row, u.parameter1, offset + 3);
				param(row, u.parameter2, offset + 4);
			}
			default -> {
			}
		}
	}

	private static void param(Row row, Double value, int col) {
		if (value != null) {
			Excel.cell(row, col, value);
		}
	}

	static <T extends RefEntity> List<T> sort(Collection<T> set) {
		return set.stream().sorted((e1, e2) -> {
			if (e1 == null && e2 == null)
				return 0;
			if (e1 == null)
				return -1;
			if (e2 == null)
				return 1;
			int c = Strings.compare(e1.name, e2.name);
			if (c != 0)
				return c;
			if (e1 instanceof RootEntity re1 && e2 instanceof RootEntity re2) {
				var c1 = CategoryPath.getFull(re1.category);
				var c2 = CategoryPath.getFull(re2.category);
				return Strings.compare(c1, c2);
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
