package org.openlca.io.openepd.io;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Epd;
import org.openlca.io.openepd.EpdDoc;

import java.util.ArrayList;
import java.util.List;

public record MappingModel(List<MethodMapping> mappings) {

	public static MappingModel empty() {
		return new MappingModel(new ArrayList<>(0));
	}

	public static MappingModel initFrom(Epd epd) {
		return ExportMapping.build(epd);
	}

	public static MappingModel initFrom(EpdDoc doc, IDatabase db) {
		return ImportMapping.build(doc, db);
	}

	public boolean hasEmptyMappings() {
		for (var m : mappings) {
			if (m.method() == null || m.epdMethod() == null)
				return true;
			for (var e : m.entries()) {
				if (e.indicator() == null || e.epdIndicator() == null)
					return true;
			}
		}
		return false;
	}
}
