package org.openlca.io.xls.epds;

import org.openlca.commons.Strings;
import org.openlca.core.model.Source;

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
			.metaDataFieldsOf(source)
			.field(Field.URL, source.url)
			.field(Field.TEXT_REFERENCE, source.textReference)
			.field(Field.YEAR, year, null)
			.field(Field.EXTERNAL_FILE, source.externalFile)
			.blank();
	}

}
