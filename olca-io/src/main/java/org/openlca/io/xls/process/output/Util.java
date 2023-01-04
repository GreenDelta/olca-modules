package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class Util {

	static void write(Sheet sheet, int row, int col, Uncertainty u) {
		if (u == null || u.distributionType == UncertaintyType.NONE) {
			Excel.cell(sheet, row, col, "");
			return;
		}
		switch (u.distributionType) {
			case LOG_NORMAL -> {
				Excel.cell(sheet, row, col, "log-normal");
				param(sheet, u.parameter1,  row, col + 1);
				param(sheet, u.parameter2,  row, col + 2);
			}
			case NORMAL -> {
				Excel.cell(sheet, row, col, "normal");
				param(sheet, u.parameter1,  row, col + 1);
				param(sheet, u.parameter2,  row, col + 2);
			}
			case TRIANGLE -> {
				Excel.cell(sheet, row, col, "triangular");
				param(sheet, u.parameter1,  row, col + 3);
				param(sheet, u.parameter2,  row, col + 1);
				param(sheet, u.parameter3,  row, col + 4);
			}
			case UNIFORM -> {
				Excel.cell(sheet, row, col, "uniform");
				param(sheet, u.parameter1,  row, col + 3);
				param(sheet, u.parameter2,  row, col + 4);
			}
			default -> {
			}
		}
	}

	private static void param(Sheet sheet, Double value, int row, int col) {
		if (value != null) {
			Excel.cell(sheet, row, col, value);
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
}
