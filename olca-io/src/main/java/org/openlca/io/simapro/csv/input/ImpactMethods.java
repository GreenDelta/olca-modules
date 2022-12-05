package org.openlca.io.simapro.csv.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwFactor;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Version;
import org.openlca.simapro.csv.method.DamageCategoryBlock;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactMethodBlock;
import org.openlca.util.KeyGen;

class ImpactMethods {

	private final ImportContext context;
	private final RefData refData;
	private final ImportLog log;
	private final ImpactMethodBlock block;
	private final Version version;

	private ImpactMethods(ImportContext context, ImpactMethodBlock block) {
		this.context = context;
		this.refData = context.refData();
		this.log = context.log();
		this.block = block;
		this.version = block.version() != null
				? new Version(block.version().major(), block.version().minor(), 0)
				: new Version(0, 0, 0);
	}

	static void map(ImportContext context, ImpactMethodBlock block) {
		new ImpactMethods(context, block).exec();
	}

	private void exec() {

		var hasDamage = !block.damageCategories().isEmpty();
		var name = hasDamage
				? block.name() + " - Midpoint"
				: block.name();
		var refId = KeyGen.get("SimaPro CSV", name, version.toString());
		var existing = context.db().get(ImpactMethod.class, refId);
		if (existing != null) {
			log.warn("an LCIA method refId='" + refId + "' already exists; skipped");
			return;
		}

		var method = new ImpactMethod();
		method.refId = refId;
		method.name = name;
		method.version = version.getValue();
		method.description = block.comment();
		var impacts = addImpactCategories(method);

		if (!hasDamage) {
			addNwSets(method, impacts);
		}
		context.insert(method);
		if (!hasDamage) {
			return;
		}

		// translate damage categories to an endpoint method
		var damageMethod = new ImpactMethod();
		damageMethod.name = block.name() + " - Endpoint";
		damageMethod.refId = KeyGen.get(
				"SimaPro CSV", damageMethod.name, version.toString());
		damageMethod.version = version.getValue();
		damageMethod.description = block.comment();
		var damageCategories = addDamageCategories(damageMethod, impacts);
		addNwSets(damageMethod, damageCategories);
	}

	private HashMap<String, ImpactCategory> addDamageCategories(
			ImpactMethod method, Map<String, ImpactCategory> impacts) {
		var damageCategories = new HashMap<String, ImpactCategory>();
		for (var damageBlock : block.damageCategories()) {
			var info = damageBlock.info();
			if (info == null)
				continue;
			var damageCategory = initImpact(method, info.name());
			damageCategory.referenceUnit = info.unit();
			addFactors(impacts, damageBlock, damageCategory);
			damageCategory = context.insert(damageCategory);
			damageCategories.put(info.name(), damageCategory);
			method.impactCategories.add(damageCategory);
		}
		return damageCategories;
	}

	private void addFactors(
			Map<String, ImpactCategory> impacts,
			DamageCategoryBlock damageBlock,
			ImpactCategory damageCategory) {
		var factors = new TLongObjectHashMap<ImpactFactor>();
		for (var df : damageBlock.factors()) {
			var impact = impacts.get(df.impactCategory());
			if (impact == null || df.factor() == 0) {
				continue;
			}
			for (var f : impact.impactFactors) {
				double refVal = refValueOf(f, df.factor());
				if (f.flow == null)
					continue;
				var factor = factors.get(f.flow.id);
				if (factor == null) {
					factor = f.copy();
					factor.value = 0;
					factors.put(f.flow.id, factor);
					damageCategory.impactFactors.add(factor);
				}
				factor.value += df.factor() * f.value;
			}
		}
	}

	private double refValueOf(ImpactFactor f, double factor) {
		if (f.flow == null)
			return 0;
		var refUnit = f.flow.getReferenceUnit();
		var refProp = f.flow.getReferenceFactor();
		if (Objects.equals(f.unit, refUnit)
				&& Objects.equals(refProp, f.flowPropertyFactor))
			return factor * f.value;

		// TODO: apply conversion
	}

	private HashMap<String, ImpactCategory> addImpactCategories(
			ImpactMethod method) {
		var impacts = new HashMap<String, ImpactCategory>();
		for (var impactBlock : block.impactCategories()) {
			var info = impactBlock.info();
			if (info == null)
				continue;
			var impact = initImpact(method, info.name());
			impact.referenceUnit = info.unit();
			addFactors(impactBlock, impact);
			impact = context.insert(impact);
			impacts.put(info.name(), impact);
			method.impactCategories.add(impact);
		}
		return impacts;
	}

	private ImpactCategory initImpact(ImpactMethod method, String name) {
		var impact = new ImpactCategory();
		impact.refId = KeyGen.get(
				"SimaPro CSV", method.name, version.toString(), name);
		impact.name = name;
		impact.category = CategoryDao.sync(
				context.db(), ModelType.IMPACT_CATEGORY, method.name);
		return impact;
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

	private void addNwSets(ImpactMethod method, Map<String, ImpactCategory> impacts) {
		for (var nwBlock : block.nwSets()) {
			var nwSet = new NwSet();
			nwSet.name = nwBlock.name();
			nwSet.refId = KeyGen.get(method.refId, nwBlock.name());
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
	}
}
