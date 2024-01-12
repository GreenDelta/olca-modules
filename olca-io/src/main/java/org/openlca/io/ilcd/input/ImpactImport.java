package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.Methods;
import org.openlca.util.Strings;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImpactImport {

	private final Import imp;
	private final org.openlca.ilcd.methods.ImpactMethod dataSet;
	private final AtomicBoolean hasRefErrors;

	public ImpactImport(Import imp, ImpactMethod dataSet) {
		this.imp = imp;
		this.dataSet = dataSet;
		this.hasRefErrors = new AtomicBoolean(false);
	}

	public static ImpactCategory get(Import imp, String id) {
		var impact = imp.db().get(ImpactCategory.class, id);
		if (impact != null)
			return impact;
		var dataSet = imp.store().get(ImpactMethod.class, id);
		if (dataSet == null) {
			imp.log().error("invalid reference in ILCD data set:" +
					" impact method '" + id + "' does not exist");
			return null;
		}
		return new ImpactImport(imp, dataSet).createNew();
	}

	public ImpactCategory run() {
		var impact = imp.db().get(ImpactCategory.class, dataSet.getUUID());
		return impact != null
				? impact
				: createNew();
	}

	private ImpactCategory createNew() {
		var impact = new ImpactCategory();
		impact.refId = dataSet.getUUID();
		impact.name = name();
		imp.log().info("import impact category: " + impact.name);
		impact.category = new CategoryDao(imp.db())
				.sync(ModelType.IMPACT_CATEGORY, Categories.getPath(dataSet));

		var info = Methods.getDataSetInfo(dataSet);
		if (info != null) {
			impact.description = imp.str(info.comment);
		}
		var qref = Methods.getQuantitativeReference(dataSet);
		if (qref != null && qref.quantity != null) {
			impact.referenceUnit = imp.str(qref.quantity.name);
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
		impact = imp.insert(impact);
		appendToMethods(impact);
		imp.log().imported(impact);
		return impact;
	}

	private String name() {
		var info = Methods.getDataSetInfo(dataSet);
		if (info == null)
			return "- none -";

		// we have in principle 3 places where we can find the name
		var name = imp.str(info.name);
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

			var syncFlow = FlowImport.get(imp, factor.flow.uuid);
			if (syncFlow.isEmpty()) {
				if (!hasRefErrors.get()) {
					hasRefErrors.set(true);
					imp.log().error("impact category " + impact.refId
							+ " has invalid data set references; e.g. flow: "
							+ factor.flow.uuid);
				}
				continue;
			}

			var f = new ImpactFactor();
			f.flow = syncFlow.flow();
			f.flowPropertyFactor = syncFlow.property();
			f.unit = syncFlow.unit();
			f.location = imp.cache.locationOf(factor.location);
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
			var m = imp.cache.impactMethodOf(name);
			if (m == null)
				continue;
			var method = imp.db().get(
					org.openlca.core.model.ImpactMethod.class, m.id);
			if (method == null) {
				imp.log().error("could not load created method: " + m.refId);
				continue;
			}
			method.impactCategories.add(impact);
			imp.db().update(method);
		}
	}
}
