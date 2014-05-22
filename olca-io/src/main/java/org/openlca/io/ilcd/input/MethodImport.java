package org.openlca.io.ilcd.input;

import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.methods.DataSetInformation;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.FactorList;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.LCIAMethodInformation;
import org.openlca.ilcd.methods.QuantitativeReference;
import org.openlca.ilcd.util.LangString;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.io.maps.MapType;
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
	private ImpactMethodDao dao;
	private IDatabase database;
	private DataStore ilcdStore;
	private FlowMap flowMap;

	public MethodImport(DataStore ilcdStore, IDatabase database) {
		this.dao = new ImpactMethodDao(database);
		this.database = database;
		this.ilcdStore = ilcdStore;
		flowMap = new FlowMap(MapType.ILCD_FLOW);
	}

	public void run(LCIAMethod iMethod) {
		if (iMethod == null)
			return;
		String categoryName = getCategoryName(iMethod);
		if (categoryName == null)
			return;
		for (ImpactMethod oMethod : MethodFetch.getOrCreate(iMethod, database)) {
			if (!hasCategory(oMethod, categoryName))
				addCategory(oMethod, categoryName, iMethod);
		}
	}

	private String getCategoryName(LCIAMethod iMethod) {
		LCIAMethodInformation info = iMethod.getLCIAMethodInformation();
		if (info == null || info.getDataSetInformation() == null)
			return null;
		DataSetInformation dataInfo = info.getDataSetInformation();
		List<String> categoryNames = dataInfo.getImpactCategory();
		if (categoryNames == null || categoryNames.isEmpty())
			return null;
		return categoryNames.get(0);
	}

	private boolean hasCategory(org.openlca.core.model.ImpactMethod oMethod,
			String categoryName) {
		for (ImpactCategory category : oMethod.getImpactCategories()) {
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
		category.setRefId(UUID.randomUUID().toString());
		category.setName(categoryName);
		category.setReferenceUnit(categoryUnit);
		category.setDescription(getCategoryDescription(iMethod));
		addFactors(iMethod, category);
		oMethod.getImpactCategories().add(category);
		dao.update(oMethod);
	}

	private String getCategoryUnit(LCIAMethod iMethod) {
		String extensionUnit = getExtentionUnit(iMethod);
		if (extensionUnit != null)
			return extensionUnit;
		LCIAMethodInformation info = iMethod.getLCIAMethodInformation();
		if (info == null || info.getQuantitativeReference() == null)
			return null;
		QuantitativeReference qRef = info.getQuantitativeReference();
		if (qRef.getReferenceQuantity() == null)
			return null;
		String propertyId = qRef.getReferenceQuantity().getUuid();
		if (propertyId == null)
			return null;
		Unit unit = getReferenceUnit(propertyId);
		return unit == null ? null : unit.getName();
	}

	private String getExtentionUnit(LCIAMethod iMethod) {
		LCIAMethodInformation info = iMethod.getLCIAMethodInformation();
		if (info == null || info.getDataSetInformation() == null)
			return null;
		DataSetInformation dataInfo = info.getDataSetInformation();
		QName extName = new QName("http://openlca.org/ilcd-extensions",
				"olca_category_unit");
		return dataInfo.getOtherAttributes().get(extName);
	}

	private String getCategoryDescription(LCIAMethod iMethod) {
		LCIAMethodInformation info = iMethod.getLCIAMethodInformation();
		if (info == null || info.getDataSetInformation() == null)
			return null;
		return LangString.getFreeText(info.getDataSetInformation()
				.getGeneralComment());
	}

	private Unit getReferenceUnit(String propertyId) {
		try {
			FlowPropertyImport propertyImport = new FlowPropertyImport(
					ilcdStore, database);
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
		FactorList list = iMethod.getCharacterisationFactors();
		if (list == null)
			return;
		for (Factor factor : list.getFactor()) {
			try {
				addFactor(category, factor);
			} catch (Exception e) {
				log.warn("Failed to add factor " + factor, e);
			}
		}
	}

	private void addFactor(ImpactCategory category, Factor factor)
			throws Exception {
		String flowId = factor.getReferenceToFlowDataSet().getUuid();
		Flow flow = getFlow(flowId);
		if (flow == null) {
			log.warn("Could not import flow {}", flowId);
			return;
		}
		ImpactFactor oFactor = new ImpactFactor();
		oFactor.setFlow(flow);
		oFactor.setFlowPropertyFactor(flow.getReferenceFactor());
		oFactor.setUnit(getRefUnit(flow));
		oFactor.setValue(factor.getMeanValue());
		category.getImpactFactors().add(oFactor);
	}

	private Flow getFlow(String flowId) throws Exception {
		Flow flow = getMappedFlow(flowId);
		if (flow != null)
			return flow;
		return new FlowImport(ilcdStore, database).run(flowId);
	}

	private Flow getMappedFlow(String flowId) {
		FlowMapEntry entry = flowMap.getEntry(flowId);
		if (entry == null)
			return null;
		FlowDao dao = new FlowDao(database);
		return dao.getForRefId(entry.getOpenlcaFlowKey());
	}

	private Unit getRefUnit(Flow flow) {
		if (flow == null)
			return null;
		FlowProperty prop = flow.getReferenceFlowProperty();
		return getReferenceUnit(prop);
	}
}
