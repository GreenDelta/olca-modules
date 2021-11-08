package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Version;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactMethodBlock;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactMethods {

	private final Logger log = LoggerFactory.getLogger(ImpactMethods.class);
	private final IDatabase db;
	private final RefData refData;
	private final ImpactMethodBlock block;

	ImpactMethods(IDatabase db, RefData refData, ImpactMethodBlock block) {
		this.db = db;
		this.refData = refData;
		this.block = block;
	}


	static void map(IDatabase db, RefData refData, ImpactMethodBlock block) {
		new ImpactMethods(db, refData, block).exec();
	}

	private void exec() {

		var version = block.version() != null
			? new Version(block.version().major(), block.version().minor(), 0)
			: new Version(0, 0, 0);
		var refId = KeyGen.get("SimaPro CSV", block.name(), version.toString());
		var method = db.get(ImpactMethod.class, refId);
		if (method != null) {
			log.warn("an LCIA method refId={} already exists; skipped", refId);
			return;
		}

		method = new ImpactMethod();
		method.refId = refId;
		method.name = block.name();
		method.version = version.getValue();
		method.description = block.comment();

		for (var csvImpact : block.impactCategories()) {
			if (csvImpact.info() == null)
				continue;
			var impactId = KeyGen.get(
				"SimaPro CSV",
				block.name(),
				version.toString(),
				csvImpact.info().name());
			var impact = new ImpactCategory();
			impact.refId = impactId;
			impact.name = csvImpact.info().name();
			impact.referenceUnit = csvImpact.info().unit();
			addFactors(csvImpact, impact);
			db.insert(impact);
			method.impactCategories.add(impact);
		}
		db.insert(method);
	}

	private void addFactors(ImpactCategoryBlock block, ImpactCategory impact) {
		for (var row : block.factors()) {
			var sync = refData.elemFlowOf(row);
			if (sync == null || sync.flow() == null)
				continue;
			var factor = new ImpactFactor();
			factor.flow = sync.flow();
			if (sync.isMapped()) {
				factor.value = sync.mapFactor() * row.factor();
				// TODO: also include unit and flow property of possible mappings
				factor.unit = sync.flow().getReferenceUnit();
				factor.flowPropertyFactor = sync.flow().getReferenceFactor();
			} else {
				factor.value = row.factor();
				var quantity = refData.quantityOf(row.unit());
				if (quantity != null) {
					factor.unit = quantity.unit;
					factor.flowPropertyFactor = sync.flow().getFactor(
						quantity.flowProperty);
				}
			}
			impact.impactFactors.add(factor);
		}
	}
}
