package org.openlca.io.ilcd.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.util.FlowPropertyBag;

/**
 * The import of an ILCD flow property data set to an openLCA database.
 * 
 * @author Michael Srocka
 * 
 */
public class FlowPropertyImport {

	private IDatabase database;
	private DataStore dataStore;
	private FlowPropertyBag ilcdProperty;
	private FlowProperty property;

	public FlowPropertyImport(DataStore dataStore, IDatabase database) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public FlowProperty run(
			org.openlca.ilcd.flowproperties.FlowProperty property)
			throws ImportException {
		this.ilcdProperty = new FlowPropertyBag(property);
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
		ilcdProperty = new FlowPropertyBag(iProp);
		return createNew();
	}

	private FlowProperty findExisting(String propertyId) throws ImportException {
		try {
			return database.createDao(FlowProperty.class).getForId(propertyId);
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
			org.openlca.ilcd.flowproperties.FlowProperty iProp = dataStore.get(
					org.openlca.ilcd.flowproperties.FlowProperty.class,
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
		CategoryImport categoryImport = new CategoryImport(database,
				FlowProperty.class);
		Category category = categoryImport.run(ilcdProperty.getSortedClasses());
		property.setCategoryId(category.getId());
	}

	private void createAndMapContent() throws ImportException {
		validateInput();
		mapDescriptionAttributes();
		createUnitGroupReference();
	}

	private void validateInput() throws ImportException {
		DataSetReference unitGroupRef = ilcdProperty.getUnitGroupReference();
		if (unitGroupRef == null || unitGroupRef.getUuid() == null) {
			String message = "Invalid input: flow property data set.";
			throw new ImportException(message);
		}
	}

	private void mapDescriptionAttributes() {
		property.setFlowPropertyType(FlowPropertyType.Physical); // default
		property.setId(ilcdProperty.getId());
		property.setName(ilcdProperty.getName());
		property.setDescription(ilcdProperty.getComment());
	}

	private void createUnitGroupReference() throws ImportException {
		DataSetReference unitGroupRef = ilcdProperty.getUnitGroupReference();
		if (unitGroupRef != null) {
			UnitGroupImport unitGroupImport = new UnitGroupImport(dataStore,
					database);
			UnitGroup unitGroup = unitGroupImport.run(unitGroupRef.getUuid());
			property.setUnitGroupId(unitGroup.getId());
		}
	}

	private void saveInDatabase(FlowProperty obj) throws ImportException {
		try {
			database.createDao(FlowProperty.class).insert(obj);
		} catch (Exception e) {
			String message = String.format(
					"Save operation failed in flow property %s.",
					property.getId());
			throw new ImportException(message, e);
		}
	}

}
