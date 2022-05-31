package org.openlca.io.openepd.mapping;

import org.openlca.core.model.Epd;

import java.util.ArrayList;
import java.util.List;

public record MappingModel(List<MethodMapping> mappings) {

	public static MappingModel empty() {
		return new MappingModel(new ArrayList<>(0));
	}

	public static MappingModel initFrom(Epd epd) {
		return ExportMapping.build(epd);
	}

}
