package org.openlca.core.library;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Exchanges;
import org.openlca.util.Strings;

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
		try (var zip = ZipStore.open(new File(export.folder, "meta.zip"))) {
			var exp = new JsonExport(db, zip)
				.withDefaultProviders(false)
				.skipLibraryData(true);
			var types = List.of(
				Actor.class,
				Source.class,
				Location.class,
				UnitGroup.class,
				FlowProperty.class,
				Flow.class,
				Currency.class,
				SocialIndicator.class,
				DQSystem.class,
				Process.class,
				ImpactCategory.class,
				ImpactMethod.class);

			var dependencies = new HashSet<String>();
			for (var type : types) {
				var descriptors = db.getDescriptors(type);
				for (var d : descriptors) {
					// filter out library data
					if (Strings.notEmpty(d.library)) {
						dependencies.add(d.library);
						continue;
					}
					var full = db.get(type, d.id);
					if (full instanceof Process process) {
						writeProcess(process, exp);
					}
					if (full instanceof ImpactCategory impact) {
						writeImpact(impact, exp);
					} else {
						exp.write(full);
					}
				}
			}

			export.info.dependencies().addAll(dependencies);
		} catch (Exception e) {
			throw new RuntimeException(
				"failed to write meta data in library export", e);
		}
	}

	private void writeProcess(Process process, JsonExport exp) {
		if (process == null)
			return;
		var libProc = process.copy();
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

	private void writeImpact(ImpactCategory impact, JsonExport exp) {
			var libImpact = impact.copy();
			libImpact.id = impact.id;
			libImpact.refId = impact.refId;
			libImpact.impactFactors.clear();
			libImpact.parameters.clear();
			exp.write(libImpact);
	}

}
