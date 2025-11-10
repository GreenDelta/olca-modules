package org.openlca.io.xls.process;

import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.store.EntityStore;

class OutConfig {

	private final Workbook wb;
	private final Styles styles;

	private final EntityStore db;
	private final Process process;
	private final List<OutEntitySync> entitySheets;

	OutConfig(Workbook wb, EntityStore db, Process process) {
		this.wb = wb;
		this.styles = Styles.of(wb);
		this.db = db;
		this.process = process;
		entitySheets = List.of(
			new OutFlowSync(this),
			new OutFlowPropertyFactorSync(this),
			new OutFlowPropertySync(this),
			new OutUnitGroupSync(this),
			new OutUnitSync(this),
			new OutCurrencySync(this),
			new OutLocationSync(this),
			new OutActorSync(this),
			new OutSourceSync(this),
			new OutProviderSync(this)
		);
	}

	Process process() {
		return process;
	}

	EntityStore db() {
		return db;
	}

	void write() {
		OutInfoSync.sync(this);
		OutExchangeSync.sync(this);
		OutAllocationSync.sync(this);
		OutParameterSync.sync(this);
		OutDocSync.sync(this);
		OutReviewSync.sync(this);
		OutComplianceSync.sync(this);
		for (var sheet : entitySheets) {
			sheet.flush();
		}
	}

	Styles styles() {
		return styles;
	}

	void visit(RootEntity e) {
		if (e == null)
			return;
		for (var sheet : entitySheets) {
			sheet.visit(e);
		}
	}

	SheetWriter createSheet(Tab tab) {
		var sheet = wb.createSheet(tab.label());
		return new SheetWriter(sheet, this);
	}

}
