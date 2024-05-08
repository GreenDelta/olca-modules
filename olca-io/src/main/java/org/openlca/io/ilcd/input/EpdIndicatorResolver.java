package org.openlca.io.ilcd.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.epd.EpdIndicatorResult;
import org.openlca.ilcd.epd.EpdProfiles;
import org.openlca.util.Strings;

class EpdIndicatorResolver {

	private final Import imp;
	private final IDatabase db;
	private final Map<String, ImpactCategory> cache;

	private EpdIndicatorResolver(Import imp) {
		this.imp = imp;
		this.db = imp.db();
		this.cache = new HashMap<>();
	}

	static EpdIndicatorResolver of(Import imp) {
		return new EpdIndicatorResolver(imp);
	}

	ImpactCategory getFor(EpdIndicatorResult r) {
		if (r == null || r.indicator() == null || !r.indicator().isValid())
			return null;

		// check the cache
		var id = r.indicator().getUUID();
		var impact = cache.get(id);
		if (impact != null)
			return impact;

		// check the database
		impact = db.get(ImpactCategory.class, id);
		if (impact != null)
			return cache(id, impact);

		// check via EPD profile code mappings
		impact = findByProfileCode(id);
		if (impact != null)
			return cache(id, impact);

		// now, the unit could be useful
		var unit = unitOf(r);

		// try the import of an impact category
		impact = ImpactImport.get(imp, r.indicator().getUUID());
		if (impact != null) {
			if (unit != null && Strings.nullOrEmpty(impact.referenceUnit)) {
				// indicator units are sometimes missing in
				// LCIA data sets of ILCD packages
				impact.referenceUnit = unit;
				impact = db.update(impact);
			}
			return cache(id, impact);
		}

		// finally, create an empty impact category
		// also for inventory indicators which are flows in ILCD+EPD!
		var name = LangString.getDefault(r.indicator().getName());
		impact = ImpactCategory.of(name, unit);
		impact.refId = id;
		impact.version = Version.fromString(
				r.indicator().getVersion()).getValue();
		impact = imp.insert(impact);
		return cache(id, impact);
	}

	private ImpactCategory findByProfileCode(String id) {
		var i = EpdProfiles.getIndicatorForId(id);
		if (i == null || i.getCode() == null)
			return null;
		var checkedIds = new HashSet<String>();
		checkedIds.add(id);
		for (var alt : EpdProfiles.getIndicatorsForCode(i.getCode())) {
			var altId = alt.getUUID();
			if (Strings.nullOrEmpty(altId) || checkedIds.contains(altId))
				continue;
			checkedIds.add(altId);
			var impact = db.get(ImpactCategory.class, altId);
			if (impact != null)
				return impact;
		}
		return null;
	}

	private String unitOf(EpdIndicatorResult r) {
		if (r.unitGroup() == null)
			return null;
		// yes, in ILCD+EPD the unit is typically written
		// into a unit-group reference; don't ask why
		var unit = LangString.getDefault(r.unitGroup().getName());
		return Strings.notEmpty(unit) ? unit : null;
	}

	private ImpactCategory cache(String id, ImpactCategory impact) {
		cache.put(id, impact);
		return impact;
	}
}
