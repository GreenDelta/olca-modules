package org.openlca.io.ilcd.input;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
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
import org.openlca.ilcd.methods.QuantitativeReference;
import org.openlca.io.maps.FlowMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports an ILCD impact method data set. Note that the ILCD data sets contain
 * impact categories and not methods. Thus, this import searches for matching
 * impact methods and appends the ILCD data set as impact category to these
 * methods.
 */
public class MethodImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final ImportConfig config;
	private ImpactMethodDao dao;

	public MethodImport(ImportConfig config) {
		this.config = config;
		this.dao = new ImpactMethodDao(config.db);
	}

	public void run(LCIAMethod iMethod) {
		if (iMethod == null)
			return;
		if (exists(iMethod))
			return;
		String categoryName = getCategoryName(iMethod);
		if (categoryName == null)
			return;
		for (ImpactMethod oMethod : MethodFetch.getOrCreate(iMethod, config)) {
			if (!hasCategory(oMethod, categoryName))
				addCategory(oMethod, categoryName, iMethod);
		}
	}

	private boolean exists(LCIAMethod iMethod) {
		String uuid = getUUID(iMethod);
		if (uuid == null)
			return false;
		try {
			ImpactCategoryDao dao = new ImpactCategoryDao(config.db);
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

	private String getCategoryName(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		DataSetInfo dataInfo = info.dataSetInfo;
		List<String> categoryNames = dataInfo.impactCategories;
		if (categoryNames == null || categoryNames.isEmpty())
			return null;
		return categoryNames.get(0);
	}

	private boolean hasCategory(org.openlca.core.model.ImpactMethod oMethod,
			String categoryName) {
		for (ImpactCategory category : oMethod.impactCategories) {
			if (StringUtils.equalsIgnoreCase(category.name, categoryName))
				return true;
		}
		return false;
	}

	private void addCategory(org.openlca.core.model.ImpactMethod oMethod,
			String categoryName, LCIAMethod iMethod) {
		log.trace("Add category {} to {}", categoryName, oMethod);
		String categoryUnit = getCategoryUnit(iMethod);
		ImpactCategory category = new ImpactCategory();
		String refId = getUUID(iMethod);
		category.refId = refId != null ? refId : UUID.randomUUID().toString();
		category.name = categoryName;
		category.referenceUnit = categoryUnit;
		category.description = getCategoryDescription(iMethod);
		addFactors(iMethod, category);
		oMethod.impactCategories.add(category);
		oMethod.lastChange = Calendar.getInstance().getTimeInMillis();
		Version.incUpdate(oMethod);
		dao.update(oMethod);
	}

	private String getUUID(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		DataSetInfo dataInfo = info.dataSetInfo;
		return dataInfo.uuid;
	}

	private String getCategoryUnit(LCIAMethod iMethod) {
		String extensionUnit = getExtentionUnit(iMethod);
		if (extensionUnit != null)
			return extensionUnit;
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.quantitativeReference == null)
			return null;
		QuantitativeReference qRef = info.quantitativeReference;
		if (qRef.quantity == null)
			return null;
		String propertyId = qRef.quantity.uuid;
		if (propertyId == null)
			return null;
		Unit unit = getReferenceUnit(propertyId);
		return unit == null ? null : unit.name;
	}

	private String getExtentionUnit(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		DataSetInfo dataInfo = info.dataSetInfo;
		QName extName = new QName("http://openlca.org/ilcd-extensions",
				"olca_category_unit");
		return dataInfo.otherAttributes.get(extName);
	}

	private String getCategoryDescription(LCIAMethod iMethod) {
		MethodInfo info = iMethod.methodInfo;
		if (info == null || info.dataSetInfo == null)
			return null;
		return LangString.getFirst(info.dataSetInfo.comment,
				config.langs);
	}

	private Unit getReferenceUnit(String propertyId) {
		try {
			FlowPropertyImport propertyImport = new FlowPropertyImport(config);
			FlowProperty prop = propertyImport.run(propertyId);
			if (prop == null)
				return null;
			return getReferenceUnit(prop);
		} catch (Exception e) {
			return null;
		}
	}

	private Unit getReferenceUnit(FlowProperty prop) {
		if (prop == null)
			return null;
		return prop.unitGroup != null
				? prop.unitGroup.referenceUnit
				: null;
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
					flow = new FlowDao(config.db).getForRefId(
							e.targetFlowID());
					if (flow != null) {
						mapped = true;
					}
				}

				// otherwise, get the flow from the database or import it
				if (flow == null) {
					flow = new FlowImport(config).run(flowId);
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

}
