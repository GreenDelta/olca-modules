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
import org.openlca.core.model.UnitGroup;
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
			if (StringUtils.equalsIgnoreCase(category.getName(), categoryName))
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
		category.setRefId(refId != null ? refId : UUID.randomUUID().toString());
		category.setName(categoryName);
		category.referenceUnit = categoryUnit;
		category.setDescription(getCategoryDescription(iMethod));
		addFactors(iMethod, category);
		oMethod.impactCategories.add(category);
		oMethod.setLastChange(Calendar.getInstance().getTimeInMillis());
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
		return unit == null ? null : unit.getName();
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
		UnitGroup group = prop.getUnitGroup();
		if (group != null && group.getReferenceUnit() != null)
			return group.getReferenceUnit();
		return null;
	}

	private void addFactors(LCIAMethod iMethod, ImpactCategory category) {
		FactorList list = iMethod.characterisationFactors;
		if (list == null)
			return;
		for (Factor factor : list.factors) {
			try {
				addFactor(category, factor);
			} catch (Exception e) {
				log.warn("Failed to add factor " + factor, e);
			}
		}
	}

	private void addFactor(ImpactCategory category, Factor factor)
			throws Exception {
		String flowId = factor.flow.uuid;
		Flow flow = getFlow(flowId);
		if (flow == null) {
			log.warn("Could not import flow {}", flowId);
			return;
		}
		ImpactFactor oFactor = new ImpactFactor();
		oFactor.flow = flow;
		oFactor.flowPropertyFactor = flow.getReferenceFactor();
		oFactor.unit = getRefUnit(flow);
		oFactor.value = factor.meanValue;
		category.impactFactors.add(oFactor);
	}

	private Flow getFlow(String flowId) throws Exception {
		Flow flow = getMappedFlow(flowId);
		if (flow != null)
			return flow;
		return new FlowImport(config).run(flowId);
	}

	private Flow getMappedFlow(String flowId) {
		FlowMapEntry entry = config.getFlowMap().getEntry(flowId);
		if (entry == null)
			return null;
		FlowDao dao = new FlowDao(config.db);
		return dao.getForRefId(entry.referenceFlowID);
	}

	private Unit getRefUnit(Flow flow) {
		if (flow == null)
			return null;
		FlowProperty prop = flow.getReferenceFlowProperty();
		return getReferenceUnit(prop);
	}
}
