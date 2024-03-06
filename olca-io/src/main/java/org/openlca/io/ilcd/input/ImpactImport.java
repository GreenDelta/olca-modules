package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.ImpactMethods;
import org.openlca.util.Strings;

import java.util.concurrent.atomic.AtomicBoolean;

public class ImpactImport {

	private final Import imp;
	private final org.openlca.ilcd.methods.ImpactMethod ds;
	private final AtomicBoolean hasRefErrors;

	public ImpactImport(Import imp, ImpactMethod ds) {
		this.imp = imp;
		this.ds = ds;
		this.hasRefErrors = new AtomicBoolean(false);
	}

	public static ImpactCategory get(Import imp, String id) {
		var impact = imp.db().get(ImpactCategory.class, id);
		if (impact != null)
			return impact;
		var ds = imp.store().get(ImpactMethod.class, id);
		if (ds == null) {
			imp.log().error("invalid reference in ILCD data set:" +
					" impact method '" + id + "' does not exist");
			return null;
		}
		return new ImpactImport(imp, ds).createNew();
	}

	public ImpactCategory run() {
		var impact = imp.db().get(
				ImpactCategory.class, ImpactMethods.getUUID(ds));
		return impact != null
				? impact
				: createNew();
	}

	private ImpactCategory createNew() {
		var impact = new ImpactCategory();
		impact.refId = ImpactMethods.getUUID(ds);
		impact.name = name();
		Import.mapVersionInfo(ds, impact);
		imp.log().info("import impact category: " + impact.name);
		impact.category = new CategoryDao(imp.db())
				.sync(ModelType.IMPACT_CATEGORY, Categories.getPath(ds));

		var info = ImpactMethods.getDataSetInfo(ds);
		if (info != null) {
			impact.description = imp.str(info.getComment());
		}
		var qref = ImpactMethods.getQuantitativeReference(ds);
		if (qref != null && qref.getQuantity() != null) {
			impact.referenceUnit = imp.str(qref.getQuantity().getName());
		}

		appendFactors(impact);
		impact = imp.insert(impact);
		appendToMethods(impact);
		imp.log().imported(impact);
		return impact;
	}

	private String name() {
		var info = ImpactMethods.getDataSetInfo(ds);
		if (info == null)
			return "- none -";

		// we have in principle 3 places where we can find the name
		var name = imp.str(info.getName());
		if (Strings.nullOrEmpty(name)) {
			name = info.getImpactCategories().stream()
					.filter(Strings::notEmpty)
					.findAny()
					.orElse(null);
			if (Strings.nullOrEmpty(name)) {
				name = info.getIndicator();
			}
		}
		if (Strings.nullOrEmpty(name))
			return "- none -";

		// add the reference year to the name if present
		var time = ImpactMethods.getTime(ds);
		if (time != null && time.getReferenceYear() != null) {
			name += " - " + time.getReferenceYear();
		}
		return name;
	}

	private void appendFactors(ImpactCategory impact) {
		for (var factor : ImpactMethods.getFactors(ds)) {
			if (factor.getFlow() == null)
				continue;
			var flowId = factor.getFlow().getUUID();
			var syncFlow = FlowImport.get(imp, flowId);
			if (syncFlow.isEmpty()) {
				if (!hasRefErrors.get()) {
					hasRefErrors.set(true);
					imp.log().error("impact category " + impact.refId
							+ " has invalid data set references; e.g. flow: "
							+ flowId);
				}
				continue;
			}

			var f = new ImpactFactor();
			f.flow = syncFlow.flow();
			f.flowPropertyFactor = syncFlow.property();
			f.unit = syncFlow.unit();
			f.location = imp.cache.locationOf(factor.getLocation());
			if (syncFlow.isMapped()) {
				var cf = syncFlow.mapFactor();
				f.value = cf != 1 && cf != 0
						? factor.getMeanValue() / cf
						: factor.getMeanValue();
			} else {
				f.value = factor.getMeanValue();
			}

			impact.impactFactors.add(f);
		}
	}

	private void appendToMethods(ImpactCategory impact) {
		var info = ImpactMethods.getDataSetInfo(ds);
		if (info == null)
			return;
		for (var name : info.getMethods()) {
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
