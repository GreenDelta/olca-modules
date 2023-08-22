package org.openlca.io.simapro.csv.output;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.simapro.csv.enums.ProcessCategory;
import org.openlca.util.Strings;

import java.util.Locale;

record CategoryPath(ProcessCategory type, String path) {

	private static CategoryPath getDefault() {
		return new CategoryPath(ProcessCategory.MATERIAL, "Other");
	}

	static CategoryPath of(SimaProExport export, Process process) {
		if (process == null)
			return getDefault();
		if (process.category != null)
			return of(export, process.category);
		var qref = process.quantitativeReference;
		return qref != null && qref.flow != null
				? of(export, qref.flow.category)
				: getDefault();
	}

	static CategoryPath of(SimaProExport export, Flow flow) {
		return flow == null
				? getDefault()
				: of(export, flow.category);
	}

	private static CategoryPath of(SimaProExport export, Category category) {
		var topAsType = export != null && export.withTopCategoryAsType;
		StringBuilder path = null;
		var c = category;
		while (c != null) {
			if (c.category == null && topAsType) {
				var type = typeOf(c);
				if (type != null) {
					var p = path == null ? "Other" : path.toString();
					return new CategoryPath(type, p);
				}
			}

			var segment = Strings.cut(c.name, 40);
			c = c.category;
			if (Strings.nullOrEmpty(segment))
				continue;
			if (path == null) {
				path = new StringBuilder(segment);
			} else {
				path.insert(0, segment + '\\');
			}
		}

		return path == null
				? getDefault()
				: new CategoryPath(ProcessCategory.MATERIAL, path.toString());
	}

	private static ProcessCategory typeOf(Category category) {
		if (category == null || Strings.nullOrEmpty(category.name))
			return null;
		var s = category.name.trim().toLowerCase(Locale.US);
		// in the import, we convert waste scenarios to parameterized
		// processes and there is no easy way to revert this in the export
		if (s.equals("waste scenario"))
			return null;
		for (var type : ProcessCategory.values()) {
			if (s.equals(type.toString()))
				return type;
		}
		return null;
	}
}
