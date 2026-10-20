package org.openlca.io.xls.epds;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Source;

/// Shared state for the writers of the different EPD sheets.
class WriterContext {

	private static final int[] COLUMN_WIDTHS = {5, 40, 70, 15};

	private final Workbook wb;
	private final Epd epd;
	private final Styles styles;
	private final Set<Actor> contacts = new LinkedHashSet<>();
	private final Set<Source> sources = new LinkedHashSet<>();

	WriterContext(Workbook wb, Epd epd) {
		this.wb = wb;
		this.epd = epd;
		this.styles = Styles.of(wb);
	}

	Epd epd() {
		return epd;
	}

	Styles styles() {
		return styles;
	}

	Sheet createSheet(Tab tab) {
		var sheet = wb.createSheet(tab.label());
		for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
			sheet.setColumnWidth(i, COLUMN_WIDTHS[i] * 256);
		}
		return sheet;
	}

	SheetWriter writer(Tab tab) {
		return new SheetWriter(createSheet(tab), styles);
	}

	void addContact(Actor actor) {
		if (actor != null) {
			contacts.add(actor);
		}
	}

	void addSource(Source source) {
		if (source != null) {
			sources.add(source);
		}
	}

	List<Actor> contacts() {
		return new ArrayList<>(contacts);
	}

	List<Source> sources() {
		return new ArrayList<>(sources);
	}
}
