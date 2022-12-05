package org.openlca.io.simapro.csv.input;

import java.util.HashMap;
import java.util.function.Function;

import org.openlca.core.io.ImportLog;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Version;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactMethodBlock;
import org.openlca.util.KeyGen;

class ImpactMethods {

	private final ImportContext context;
	private final RefData refData;
	private final ImportLog log;
	private final ImpactMethodBlock block;

	private ImpactMethods(ImportContext context, ImpactMethodBlock block) {
		this.context = context;
		this.refData = context.refData();
		this.log = context.log();
		this.block = block;
	}

	static void map(ImportContext context, ImpactMethodBlock block) {
		new ImpactMethods(context, block).exec();
	}

	private void exec() {

		var version = block.version() != null
				? new Version(block.version().major(), block.version().minor(), 0)
				: new Version(0, 0, 0);
		var refId = KeyGen.get("SimaPro CSV", block.name(), version.toString());
		var existing = context.db().get(ImpactMethod.class, refId);
		if (existing != null) {
			log.warn("an LCIA method refId='" + refId + "' already exists; skipped");
			return;
		}

		var method = new ImpactMethod();
		method.refId = refId;
		method.name = block.name();
		method.version = version.getValue();
		method.description = block.comment();

		// create LCIA categories
		var impacts = new HashMap<String, ImpactCategory>();
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
			impact = context.insert(impact);
			impacts.put(impact.name, impact);
			method.impactCategories.add(impact);
		}

		// normalization & weighting sets
		for (var nwBlock : block.nwSets()) {
			var nwSet = new NwSet();
			nwSet.name = nwBlock.name();
			nwSet.refId = KeyGen.get(
					"SimaPro CSV", block.name(), version.toString(), nwBlock.name());
			method.nwSets.add(nwSet);

			var nwFactors = new HashMap<String, NwFactor>();
			Function<String, NwFactor> makeNwFactor = impactName -> {
				var impactCategory = impacts.get(impactName);
				if (impactCategory == null)
					return null;
				var factor = new NwFactor();
				factor.impactCategory = impactCategory;
				nwSet.factors.add(factor);
				return factor;
			};

			for (var nf : nwBlock.normalizationFactors()) {
				var factor = nwFactors.computeIfAbsent(
						nf.impactCategory(), makeNwFactor);
				if (factor == null || nf.factor() == 0)
					continue;
				factor.normalisationFactor = 1 / nf.factor();
			}
			for (var wf : nwBlock.weightingFactors()) {
				var factor = nwFactors.computeIfAbsent(
						wf.impactCategory(), makeNwFactor);
				factor.weightingFactor = wf.factor();
			}
		}

		context.insert(method);
	}

	private void addFactors(ImpactCategoryBlock block, ImpactCategory impact) {
		for (var row : block.factors()) {
			var sync = refData.elemFlowOf(row);
			if (sync == null || sync.flow() == null)
				continue;
			var factor = new ImpactFactor();
			factor.flow = sync.flow();
			if (sync.isMapped()) {
				factor.value = sync.mapFactor() != 0
						? row.factor() / sync.mapFactor()
						: 0;
				factor.unit = sync.unit();
				factor.flowPropertyFactor = sync.property();
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
