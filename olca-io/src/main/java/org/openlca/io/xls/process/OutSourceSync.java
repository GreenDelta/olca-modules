package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;

class OutSourceSync implements OutEntitySync {

	private final OutConfig config;
	private final Set<Source> sources = new HashSet<>();

	OutSourceSync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Source source) {
			sources.add(source);
		}
	}

	@Override
	public void flush() {
		var sheet = config.createSheet(Tab.SOURCES)
			.withColumnWidths(9, 25)
			.header(
				Field.UUID,
				Field.NAME,
				Field.DESCRIPTION,
				Field.CATEGORY,
				Field.VERSION,
				Field.LAST_CHANGE,
				Field.URL,
				Field.TEXT_REFERENCE,
				Field.YEAR);
		for (var source : Out.sort(sources)) {
			sheet.next(row -> {
				row.next(source.refId);
				row.next(source.name);
				row.next(source.description);
				row.next(Out.pathOf(source));
				row.nextAsVersion(source.version);
				row.nextAsDate(source.lastChange);
				row.next(source.url);
				row.next(source.textReference);
				if (source.year != null) {
					row.next(source.year);
				} else {
					row.next();
				}
			});
		}
	}

}
