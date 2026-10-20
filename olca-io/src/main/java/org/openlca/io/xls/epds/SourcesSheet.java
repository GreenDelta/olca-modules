package org.openlca.io.xls.epds;

import java.util.Date;

import org.openlca.commons.Strings;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.util.Categories;

/// Writes the "Sources" sheet with one section per referenced source.
class SourcesSheet {

	private final WriterContext ctx;

	SourcesSheet(WriterContext ctx) {
		this.ctx = ctx;
	}

	void write() {
		var sheet = ctx.writer(Tab.SOURCES);
		var sources = ctx.sources();
		sources.sort((s1, s2) -> Strings.compareNatural(s1.name, s2.name));
		for (var source : sources) {
			writeSource(sheet, source);
		}
	}

	private void writeSource(SheetWriter sheet, Source source) {
		Double year = source.year != null
			? source.year.doubleValue()
			: null;

		sheet.section(source.name)
			.field(Field.UUID, source.refId)
			.field(Field.CATEGORY, categoryOf(source))
			.field(Field.VERSION, Version.asString(source.version))
			.dateTime(Field.LAST_CHANGE, source.lastChange > 0
				? new Date(source.lastChange)
				: null)
			.field(Field.TAGS, source.tags)
			.field(Field.DESCRIPTION, source.description)
			.field(Field.URL, source.url)
			.field(Field.TEXT_REFERENCE, source.textReference)
			.field(Field.YEAR, year, null)
			.field(Field.EXTERNAL_FILE, source.externalFile)
			.blank();
	}

	private static String categoryOf(Source source) {
		if (source.category == null)
			return null;
		var path = Categories.path(source.category);
		return path.isEmpty()
			? null
			: String.join("/", path);
	}
}
