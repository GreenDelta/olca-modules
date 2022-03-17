package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Methods;
import org.openlca.util.Strings;

public record ImpactImport(ImportConfig config, LCIAMethod dataSet) {

	public static ImpactCategory get(ImportConfig config, String id) {
		var impact = config.db().get(ImpactCategory.class, id);
		if (impact != null)
			return impact;
		var dataSet = config.store().get(LCIAMethod.class, id);
		if (dataSet == null) {
			config.log().error("invalid reference in ILCD data set:" +
				" impact method '" + id + "' does not exist");
			return null;
		}
		return new ImpactImport(config, dataSet).createNew();
	}

	public ImpactCategory run() {
		var impact = config.db().get(ImpactCategory.class, dataSet.getUUID());
		return impact != null
			? impact
			: createNew();
	}

	private ImpactCategory createNew() {
		var impact = new ImpactCategory();
		impact.refId = dataSet.getUUID();
		impact.name = name();
		config.log().info("import impact category: " + impact.name);
		impact.category = new CategoryDao(config.db())
			.sync(ModelType.IMPACT_CATEGORY, Categories.getPath(dataSet));

		var info = Methods.getDataSetInfo(dataSet);
		if (info != null) {
			impact.description = config.str(info.comment);
		}
		var qref = Methods.getQuantitativeReference(dataSet);
		if (qref != null && qref.quantity != null) {
			impact.referenceUnit = config.str(qref.quantity.name);
		}

		// timestamp
		var entry = Methods.getDataEntry(dataSet);
		if (entry != null && entry.timeStamp != null) {
			impact.lastChange = entry.timeStamp.toGregorianCalendar()
				.getTimeInMillis();
		} else {
			impact.lastChange = System.currentTimeMillis();
		}

		// version
		var pub = Methods.getPublication(dataSet);
		if (pub != null && pub.version != null) {
			impact.version = Version.fromString(pub.version).getValue();
		}

		appendFactors(impact);
		impact = config.insert(impact);
		appendToMethods(impact);
		config.log().imported(impact);
		return impact;
	}

	private String name() {
		var info = Methods.getDataSetInfo(dataSet);
		if (info == null)
			return "- none -";

		// we have in principle 3 places where we can find the name
		var name = config.str(info.name);
		if (Strings.nullOrEmpty(name)) {
			name = info.impactCategories.stream()
				.filter(Strings::notEmpty)
				.findAny()
				.orElse(null);
			if (Strings.nullOrEmpty(name)) {
				name = info.indicator;
			}
		}
		if (Strings.nullOrEmpty(name))
			return "- none -";

		// add the reference year to the name if present
		var time = Methods.getTime(dataSet);
		if (time != null && time.referenceYear != null) {
			name += " - " + time.referenceYear;
		}
		return name;
	}

	private void appendFactors(ImpactCategory impact) {
		for (var factor : Methods.getFactors(dataSet)) {
			if (factor.flow == null)
				continue;

			var syncFlow = FlowImport.get(config, factor.flow.uuid);
			if (syncFlow.isEmpty())
				continue;

			var f = new ImpactFactor();
			f.flow = syncFlow.flow();
			f.flowPropertyFactor = syncFlow.property();
			f.unit = syncFlow.unit();
			f.location = config.locationOf(factor.location);
			if (syncFlow.isMapped()) {
				var cf = syncFlow.mapFactor();
				f.value = cf != 1 && cf != 0
					? factor.meanValue / cf
					: factor.meanValue;
			} else {
				f.value = factor.meanValue;
			}

			impact.impactFactors.add(f);
		}
	}

	private void appendToMethods(ImpactCategory impact) {
		var info = Methods.getDataSetInfo(dataSet);
		if (info == null)
			return;
		for (var name : info.methods) {
			var m = config.impactMethodOf(name);
			if (m == null)
				continue;
			var method = config.db().get(ImpactMethod.class, m.id);
			if (method == null) {
				config.log().error("could not load created method: " + m.refId);
				continue;
			}
			method.impactCategories.add(impact);
			config.db().update(method);
		}
	}
}
