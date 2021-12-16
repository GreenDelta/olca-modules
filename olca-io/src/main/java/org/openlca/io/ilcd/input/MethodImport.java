package org.openlca.io.ilcd.input;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.MethodInfo;
import org.openlca.ilcd.methods.Publication;
import org.openlca.ilcd.util.Methods;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports an ILCD LCIA data set. Note that the ILCD LCIA data sets contain
 * impact categories and not methods. This import tries to find or create
 * matching LCIA methods and adds the resulting LCIA category to these methods.
 */
public class MethodImport {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ImportConfig config;

	public MethodImport(ImportConfig config) {
		this.config = config;

	}

	public void run(LCIAMethod dataSet) {
		if (dataSet == null)
			return;
		if (exists(dataSet))
			return;
		var indicator = makeCategory(dataSet);

		// add the indicator to possible LCIA methods
		for (var method : MethodFetch.get(dataSet, config)) {

			// add the indicator only if it does not exist yet in the method
			boolean exists = false;
			for (var other : method.impactCategories) {
				if (Objects.equals(indicator.refId, other.refId)) {
					exists = true;
					break;
				}
			}
			if (exists)
				continue;

			method.impactCategories.add(indicator);
			Version.incUpdate(method);
			method.lastChange = Calendar.getInstance().getTimeInMillis();
			config.db().update(method);
		}
	}

	private boolean exists(LCIAMethod iMethod) {
		String uuid = getUUID(iMethod);
		if (uuid == null)
			return false;
		try {
			var dao = new ImpactCategoryDao(config.db());
			ImpactCategory category = dao.getForRefId(uuid);
			if (category != null) {
				log.info("LCIA category {} not imported because it "
					+ "already exists in the database", uuid);
				return true;
			}
			log.trace("import LCIA category {}", uuid);
			return false;
		} catch (Exception e) {
			log.error("failed to check if LCIA category exists " + uuid, e);
			return false;
		}
	}

	private ImpactCategory makeCategory(LCIAMethod iMethod) {

		var impact = new ImpactCategory();
		String refId = getUUID(iMethod);
		impact.refId = refId != null ? refId : UUID.randomUUID().toString();
		impact.name = getName(iMethod);
		impact.description = getDescription(iMethod);
		impact.referenceUnit = getReferenceUnit(iMethod);

		// timestamp
		var entry = Methods.getDataEntry(iMethod);
		if (entry != null && entry.timeStamp != null) {
			impact.lastChange = entry.timeStamp
				.toGregorianCalendar().getTimeInMillis();
		} else {
			impact.lastChange = new Date().getTime();
		}

		// version
		Publication pub = Methods.getPublication(iMethod);
		if (pub != null && pub.version != null) {
			impact.version = Version.fromString(pub.version).getValue();
		}

		// add LCIA factors and save it
		addFactors(iMethod, impact);

		return config.db().insert(impact);
	}

	private String getName(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		DataSetInfo dataInfo = info.dataSetInfo;

		// try to find a name
		String name = config.str(dataInfo.name);
		if (name == null) {
			List<String> names = dataInfo.impactCategories;
			if (!names.isEmpty()) {
				name = names.get(0);
			}
		}
		if (name == null) {
			name = "?";
		}

		// add the reference year to the name if present
		if (info.time != null && info.time.referenceYear != null) {
			name += ", " + info.time.referenceYear;
		}
		return name;
	}

	private String getUUID(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		DataSetInfo dataInfo = info.dataSetInfo;
		return dataInfo.uuid;
	}

	private String getReferenceUnit(LCIAMethod iMethod) {
		var info = iMethod.methodInfo;
		if (info == null || info.quantitativeReference == null)
			return null;
		var qRef = info.quantitativeReference;
		return qRef.quantity == null
			? null
			: config.str(qRef.quantity.name);
	}

	private String getDescription(LCIAMethod iMethod) {
		var info = iMethod.methodInfo;
		return info == null || info.dataSetInfo == null
			? null
			: config.str(info.dataSetInfo.comment);
	}

	private void addFactors(LCIAMethod m, ImpactCategory impact) {
		var list = m.characterisationFactors;
		if (list == null)
			return;
		for (var factor : list.factors) {
			if (factor.flow == null)
				continue;

			var syncFlow = FlowImport.get(config, factor.flow.uuid);
			if (syncFlow.isEmpty())
				continue;

			ImpactFactor f = new ImpactFactor();
			f.flow = syncFlow.flow();
			f.flowPropertyFactor = syncFlow.property();
			f.unit = syncFlow.unit();
			if (syncFlow.isMapped()) {
				var cf = syncFlow.mapFactor();
				f.value = cf != 1 && cf != 0
					? factor.meanValue / cf
					: factor.meanValue;
			} else {
				f.value = factor.meanValue;
			}

			if (Strings.notEmpty(factor.location)) {
				f.location = Locations.getOrCreate(
					factor.location, config);
			}

			impact.impactFactors.add(f);
		} // for
	}

}
