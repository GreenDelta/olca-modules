package org.openlca.io.ilcd.input;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.FactorList;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.MethodInfo;
import org.openlca.ilcd.methods.Publication;
import org.openlca.ilcd.methods.QuantitativeReference;
import org.openlca.ilcd.util.Methods;
import org.openlca.io.maps.FlowMapEntry;
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

	public void run(LCIAMethod iMethod) {
		if (iMethod == null)
			return;
		if (exists(iMethod))
			return;
		ImpactCategory indicator = makeCategory(iMethod);

		// add the indicator to possible LCIA methods
		for (ImpactMethod oMethod : MethodFetch.get(iMethod, config)) {

			// add the indicator only if it does not exist yet in the method
			boolean exists = false;
			for (ImpactCategory other : oMethod.impactCategories) {
				if (Objects.equals(indicator.refId, other.refId)) {
					exists = true;
					break;
				}
			}
			if (exists)
				continue;

			oMethod.impactCategories.add(indicator);
			Version.incUpdate(oMethod);
			oMethod.lastChange = Calendar.getInstance().getTimeInMillis();
			new ImpactMethodDao(config.db).update(oMethod);
		}
	}

	private boolean exists(LCIAMethod iMethod) {
		String uuid = getUUID(iMethod);
		if (uuid == null)
			return false;
		try {
			var dao = new ImpactCategoryDao(config.db);
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

		ImpactCategory impact = new ImpactCategory();
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
		ImpactCategoryDao dao = new ImpactCategoryDao(config.db);

		return dao.insert(impact);
	}

	private String getName(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		DataSetInfo dataInfo = info.dataSetInfo;

		// try to find a name
		String name = LangString.getFirst(dataInfo.name, config.langs);
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
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.quantitativeReference == null)
			return null;
		QuantitativeReference qRef = info.quantitativeReference;
		if (qRef.quantity == null)
			return null;
		return LangString.getFirst(qRef.quantity.name, config.langs);
	}

	private String getDescription(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		return LangString.getFirst(
				info.dataSetInfo.comment, config.langs);
	}

	private void addFactors(LCIAMethod iMethod, ImpactCategory category) {
		FactorList list = iMethod.characterisationFactors;
		if (list == null)
			return;
		int errors = 0;
		for (Factor factor : list.factors) {
			try {
				if (factor.flow == null) {
					errors++;
					continue;
				}
				String flowId = factor.flow.uuid;

				// first, try to get the flow from a mapping
				Flow flow = null;
				boolean mapped = false;
				FlowMapEntry e = config.getFlowMap().getEntry(flowId);
				if (e != null) {
					flow = getFlow(e.targetFlowID(), false);
					if (flow != null) {
						mapped = true;
					}
				}

				// otherwise, get the flow from the database or import it
				if (flow == null) {
					flow = getFlow(flowId, true);
				}
				if (flow == null) {
					log.trace("Could not import flow {}", flowId);
					errors++;
					continue;
				}

				ImpactFactor f = new ImpactFactor();
				f.flow = flow;
				f.flowPropertyFactor = flow.getReferenceFactor();
				f.unit = getReferenceUnit(flow.referenceFlowProperty);
				f.value = factor.meanValue;
				if (mapped && e.factor != 1.0 & e.factor != 0.0) {
					// apply the conversion factor from the mapping
					f.value /= e.factor;
				}
				if (Strings.notEmpty(factor.location)) {
					f.location = Locations.getOrCreate(
							factor.location, config);
				}

				category.impactFactors.add(f);
			} catch (Exception e) {
				log.trace("Failed to add factor " + factor, e);
				errors++;
			}
		} // for

		if (errors > 0) {
			log.warn("there were flow errors in {} factors of LCIA category {}",
					errors, category.name);
		}
	}

	private Unit getReferenceUnit(FlowProperty prop) {
		if (prop == null)
			return null;
		return prop.unitGroup != null
				? prop.unitGroup.referenceUnit
				: null;
	}

	private Flow getFlow(String uuid, boolean canImport) {

		// check the cache
		Flow flow = config.flowCache.get(uuid);
		if (flow != null)
			return flow;

		// check the database
		FlowDao dao = new FlowDao(config.db);
		flow = dao.getForRefId(uuid);
		if (flow != null) {
			config.flowCache.put(uuid, flow);
			return flow;
		}

		// run the import
		if (canImport) {
			try {
				flow = new FlowImport(config).run(uuid);
				config.flowCache.put(uuid, flow);
				return flow;
			} catch (Exception e) {
				log.error("failed to import flow " + uuid, e);
			}
		}
		return	null;
	}
}
