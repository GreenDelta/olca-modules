package org.openlca.io.ilcd.input;

import java.util.Date;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.util.FlowPropertyBag;

public class FlowPropertyImport {

	private final ImportConfig config;
	private FlowPropertyBag ilcdProperty;
	private FlowProperty property;

	public FlowPropertyImport(ImportConfig config) {
		this.config = config;
	}

	public FlowProperty run(
			org.openlca.ilcd.flowproperties.FlowProperty property)
			throws ImportException {
		this.ilcdProperty = new FlowPropertyBag(property, config.langs);
		FlowProperty oProperty = findExisting(ilcdProperty.getId());
		if (oProperty != null)
			return oProperty;
		return createNew();
	}

	public FlowProperty run(String propertyId) throws ImportException {
		FlowProperty property = findExisting(propertyId);
		if (property != null)
			return property;
		org.openlca.ilcd.flowproperties.FlowProperty iProp = tryGetFlowProperty(propertyId);
		ilcdProperty = new FlowPropertyBag(iProp, config.langs);
		return createNew();
	}

	private FlowProperty findExisting(String propertyId) throws ImportException {
		try {
			FlowPropertyDao dao = new FlowPropertyDao(config.db);
			return dao.getForRefId(propertyId);
		} catch (Exception e) {
			String message = String.format(
					"Search for flow property %s failed.", propertyId);
			throw new ImportException(message, e);
		}
	}

	private FlowProperty createNew() throws ImportException {
		property = new FlowProperty();
		importAndSetCategory();
		createAndMapContent();
		saveInDatabase(property);
		return property;
	}

	private org.openlca.ilcd.flowproperties.FlowProperty tryGetFlowProperty(
			String propertyId) throws ImportException {
		try {
			org.openlca.ilcd.flowproperties.FlowProperty iProp = config.store
					.get(org.openlca.ilcd.flowproperties.FlowProperty.class,
							propertyId);
			if (iProp == null) {
				throw new ImportException("No ILCD flow property for ID "
						+ propertyId + " found");
			}
			return iProp;
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(config,
				ModelType.FLOW_PROPERTY);
		Category category = categoryImport.run(ilcdProperty.getSortedClasses());
		property.setCategory(category);
	}

	private void createAndMapContent() throws ImportException {
		validateInput();
		mapDescriptionAttributes();
		createUnitGroupReference();
	}

	private void validateInput() throws ImportException {
		Ref unitGroupRef = ilcdProperty.getUnitGroupReference();
		if (unitGroupRef == null || unitGroupRef.uuid == null) {
			String message = "Invalid input: flow property data set.";
			throw new ImportException(message);
		}
	}

	private void mapDescriptionAttributes() {
		property.setFlowPropertyType(FlowPropertyType.PHYSICAL); // default
		property.setRefId(ilcdProperty.getId());
		property.setName(ilcdProperty.getName());
		property.setDescription(ilcdProperty.getComment());
		String v = ilcdProperty.getVersion();
		property.setVersion(Version.fromString(v).getValue());
		Date time = ilcdProperty.getTimeStamp();
		if (time != null)
			property.setLastChange(time.getTime());
	}

	private void createUnitGroupReference() throws ImportException {
		Ref unitGroupRef = ilcdProperty.getUnitGroupReference();
		if (unitGroupRef != null) {
			UnitGroupImport unitGroupImport = new UnitGroupImport(config);
			UnitGroup unitGroup = unitGroupImport.run(unitGroupRef.uuid);
			property.setUnitGroup(unitGroup);
		}
	}

	private void saveInDatabase(FlowProperty obj) throws ImportException {
		try {
			new FlowPropertyDao(config.db).insert(obj);
		} catch (Exception e) {
			String message = String.format(
					"Save operation failed in flow property %s.",
					property.getRefId());
			throw new ImportException(message, e);
		}
	}

}
