package org.openlca.core.library;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Exchanges;

/**
 * Writes the meta-data (JSON-LD) package in a library export.
 */
class MetaDataExport implements Runnable {

	private final LibraryExport export;
	private final IDatabase db;

	MetaDataExport(LibraryExport export) {
		this.export = export;
		this.db = export.db;
	}

	@Override
	public void run() {
		if (!export.withInventory && !export.withImpacts)
			return;
		try (var zip = ZipStore.open(new File(export.folder, "meta.zip"))) {
			var exp = new JsonExport(db, zip);
			exp.setExportDefaultProviders(false);
			if (export.withInventory) {
				writeProcesses(exp);
			}
			if (export.withImpacts) {
				writeImpactCategories(exp);
				writeImpactMethods(exp);
			}
		} catch (Exception e) {
			throw new RuntimeException(
				"failed to write meta data in library export", e);
		}
	}

	private void writeProcesses(JsonExport exp) {
		var dao = new ProcessDao(db);
		for (var d : dao.getDescriptors()) {
			var process = dao.getForId(d.id);
			if (process == null)
				continue;

			// make sure to write the used flows and locations
			for (var e : process.exchanges) {
				if (e.flow != null) {
					exp.write(e.flow);
				}
				if (e.location != null) {
					exp.write(e.location);
				}
			}

			// write the library stub of the process
			var libProc = process.clone();
			libProc.refId = process.refId;
			libProc.id = process.id;
			libProc.exchanges.removeIf(
				e -> !Exchanges.isProviderFlow(e));
			libProc.allocationFactors.clear();
			libProc.parameters.clear();
			for (var e : libProc.exchanges) {
				e.amount = 1.0;
				e.formula = null;
				if (e.flow == null)
					continue;
				e.unit = e.flow.getReferenceUnit();
				e.flowPropertyFactor = e.flow.getReferenceFactor();
			}
			exp.write(libProc);
		}
	}

	private void writeImpactCategories(JsonExport exp) {
		var dao = new ImpactCategoryDao(db);
		for (var d : dao.getDescriptors()) {
			var impact = dao.getForId(d.id);

			// make sure to write the used flows and locations
			for (var f : impact.impactFactors) {
				if (f.flow != null) {
					exp.write(f.flow);
				}
				if (f.location != null) {
					exp.write(f.location);
				}
			}

			var libImpact = impact.clone();
			libImpact.id = impact.id;
			libImpact.refId = impact.refId;
			libImpact.impactFactors.clear();
			exp.write(libImpact);
		}
	}

	private void writeImpactMethods(JsonExport exp) {
		var dao = new ImpactMethodDao(db);
		for (var d : dao.getDescriptors()) {
			var method = dao.getForId(d.id);
			exp.write(method);
		}
	}
}
