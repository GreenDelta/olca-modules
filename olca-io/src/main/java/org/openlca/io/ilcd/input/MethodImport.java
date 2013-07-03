package org.openlca.io.ilcd.input;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.TypeOfLCIAMethod;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.io.KeyGen;
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
		this.dao = new ImpactMethodDao(database.getEntityFactory());
		this.database = database;
		this.ilcdStore = ilcdStore;
		flowMap = new FlowMap(MapType.ILCD_FLOW);
	}

	public void run(LCIAMethod iMethod) throws Exception {
		if (iMethod == null || iMethod.getCharacterisationFactors() == null)
			return;
		List<String> methodNames = getMethodNames(iMethod);
		String type = getType(iMethod);
		String categoryName = getCategoryName(iMethod);
		if (categoryName == null)
			return;
		for (String methodName : methodNames) {
			String name = methodName;
			if (type != null)
				name += " (" + type + ")";
			org.openlca.core.model.ImpactMethod oMethod = fetchMethod(name);
			if (!hasCategory(oMethod, categoryName)) {
				addCategory(oMethod, categoryName, iMethod);
			}
		}
	}

	private String getCategoryName(LCIAMethod iMethod) {
		if (iMethod.getLCIAMethodInformation() == null
				|| iMethod.getLCIAMethodInformation().getDataSetInformation() == null)
			return null;
		List<String> categoryNames = iMethod.getLCIAMethodInformation()
				.getDataSetInformation().getImpactCategory();
		if (categoryNames == null || categoryNames.isEmpty())
			return null;
		return categoryNames.get(0);
	}

	private List<String> getMethodNames(LCIAMethod iMethod) {
		if (iMethod.getLCIAMethodInformation() == null
				|| iMethod.getLCIAMethodInformation().getDataSetInformation() == null)
			return Collections.emptyList();
		return iMethod.getLCIAMethodInformation().getDataSetInformation()
				.getMethodology();
	}

	private String getType(LCIAMethod iMethod) {
		if (iMethod.getModellingAndValidation() == null
				|| iMethod.getModellingAndValidation()
						.getLCIAMethodNormalisationAndWeighting() == null)
			return null;
		TypeOfLCIAMethod type = iMethod.getModellingAndValidation()
				.getLCIAMethodNormalisationAndWeighting().getTypeOfDataSet();
		if (type == null)
			return null;
		return type.value();
	}

	private org.openlca.core.model.ImpactMethod fetchMethod(String name)
			throws Exception {
		String id = KeyGen.get(name);
		org.openlca.core.model.ImpactMethod method = dao.getForId(id);
		if (method == null) {
			method = new org.openlca.core.model.ImpactMethod();
			method.setRefId(id);
			method.setName(name);
			dao.insert(method);
		}
		return method;
	}

	private boolean hasCategory(org.openlca.core.model.ImpactMethod oMethod,
			String categoryName) {
		for (ImpactCategory category : oMethod.getLCIACategories()) {
			if (StringUtils.equalsIgnoreCase(category.getName(), categoryName))
				return true;
		}
		return false;
	}

	private void addCategory(org.openlca.core.model.ImpactMethod oMethod,
			String categoryName, LCIAMethod iMethod) throws Exception {
		log.trace("Add category {} to {}", categoryName, oMethod);
		String categoryUnit = getCategoryUnit(iMethod);
		ImpactCategory category = new ImpactCategory();
		category.setRefId(UUID.randomUUID().toString());
		category.setName(categoryName);
		category.setReferenceUnit(categoryUnit);
		for (Factor factor : iMethod.getCharacterisationFactors().getFactor()) {
			addFactor(category, factor);
		}
		oMethod.getLCIACategories().add(category);
		dao.update(oMethod);
	}

	private String getCategoryUnit(LCIAMethod iMethod) {
		String propertyId = null;
		if (iMethod.getLCIAMethodInformation() != null
				&& iMethod.getLCIAMethodInformation()
						.getQuantitativeReference() != null) {
			propertyId = iMethod.getLCIAMethodInformation()
					.getQuantitativeReference().getReferenceQuantity()
					.getUuid();
		}
		if (propertyId == null)
			return null;
		Unit unit = getReferenceUnit(propertyId);
		return unit == null ? null : unit.getName();
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

	private Unit getReferenceUnit(FlowProperty prop) throws Exception {
		UnitGroup group = prop.getUnitGroup();
		if (group != null && group.getReferenceUnit() != null)
			return group.getReferenceUnit();
		return null;
	}

	private void addFactor(ImpactCategory category, Factor factor) {
		if (factor.getMeanValue() == 0)
			return;
		try {
			String flowId = factor.getReferenceToFlowDataSet().getUuid();
			Flow flow = getFlow(flowId);
			if (flow == null) {
				log.warn("Could not import flow {}", flowId);
				return;
			}
			ImpactFactor oFactor = new ImpactFactor();
			oFactor.setFlow(flow);
			oFactor.setFlowPropertyFactor(flow.getReferenceFactor());
			oFactor.setRefId(UUID.randomUUID().toString());
			oFactor.setUnit(getRefUnit(flow));
			oFactor.setValue(factor.getMeanValue());
			category.getImpactFactors().add(oFactor);
		} catch (Exception e) {
			log.warn("Failed to add factor " + factor, e);
		}
	}

	private Flow getFlow(String flowId) throws Exception {
		Flow flow = getMappedFlow(flowId);
		if (flow != null)
			return flow;
		return new FlowImport(ilcdStore, database).run(flowId);
	}

	private Flow getMappedFlow(String flowId) throws Exception {
		FlowMapEntry entry = flowMap.getEntry(flowId);
		if (entry == null)
			return null;
		return database.createDao(Flow.class).getForId(
				entry.getOpenlcaFlowKey());
	}

	private Unit getRefUnit(Flow flow) throws Exception {
		if (flow == null)
			return null;
		FlowProperty prop = flow.getReferenceFlowProperty();
		return getReferenceUnit(prop);
	}
}
