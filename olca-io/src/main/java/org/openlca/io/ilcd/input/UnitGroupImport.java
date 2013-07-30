package org.openlca.io.ilcd.input;

import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroupBag;

/**
 * The import of an ILCD unit group data set to an openLCA database.
 * 
 * @author Michael Srocka
 * 
 */
public class UnitGroupImport {

	private IDatabase database;
	private DataStore dataStore;
	private UnitGroupBag ilcdUnitGroup;
	private UnitGroup unitGroup;

	public UnitGroupImport(DataStore dataStore, IDatabase database) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public UnitGroup run(org.openlca.ilcd.units.UnitGroup group)
			throws ImportException {
		this.ilcdUnitGroup = new UnitGroupBag(group);
		UnitGroup oGroup = findExisting(ilcdUnitGroup.getId());
		if (oGroup != null) {
			new UnitGroupSync(oGroup, ilcdUnitGroup).run(database);
			return oGroup;
		}
		return createNew();
	}

	public UnitGroup run(String unitGroupId) throws ImportException {
		UnitGroup unitGroup = findExisting(unitGroupId);
		if (unitGroup != null)
			// TODO: check if reference unit is in database!
			return unitGroup;
		org.openlca.ilcd.units.UnitGroup group = tryGetUnitGroup(unitGroupId);
		ilcdUnitGroup = new UnitGroupBag(group);
		return createNew();
	}

	private UnitGroup findExisting(String unitGroupId) throws ImportException {
		try {
			UnitGroupDao dao = new UnitGroupDao(database);
			return dao.getForRefId(unitGroupId);
		} catch (Exception e) {
			String message = String.format("Search for unit group %s failed.",
					unitGroupId);
			throw new ImportException(message);
		}
	}

	private UnitGroup createNew() throws ImportException {
		unitGroup = new UnitGroup();
		importAndSetCategory();
		createAndMapContent();
		saveInDatabase(unitGroup);
		return unitGroup;
	}

	private org.openlca.ilcd.units.UnitGroup tryGetUnitGroup(String unitGroupId)
			throws ImportException {
		try {
			org.openlca.ilcd.units.UnitGroup group = dataStore.get(
					org.openlca.ilcd.units.UnitGroup.class, unitGroupId);
			if (group == null) {
				throw new ImportException("No ILCD unit group for ID "
						+ unitGroupId + " found.");
			}
			return group;
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(database,
				ModelType.UNIT_GROUP);
		Category category = categoryImport
				.run(ilcdUnitGroup.getSortedClasses());
		unitGroup.setCategory(category);
	}

	private void createAndMapContent() throws ImportException {
		validateInput();
		mapDescriptionAttributes();
		createUnits();
	}

	private void validateInput() throws ImportException {
		if (ilcdUnitGroup.getReferenceUnitId() == null
				|| ilcdUnitGroup.getName() == null
				|| ilcdUnitGroup.getUnits().size() == 0) {
			String message = "Invalid input: unit group data set.";
			throw new ImportException(message);
		}
	}

	private void mapDescriptionAttributes() {
		unitGroup.setRefId(ilcdUnitGroup.getId());
		unitGroup.setName(ilcdUnitGroup.getName());
		unitGroup.setDescription(ilcdUnitGroup.getComment());
	}

	private void createUnits() {
		Integer refUnitId = ilcdUnitGroup.getReferenceUnitId();
		for (org.openlca.ilcd.units.Unit iUnit : ilcdUnitGroup.getUnits()) {
			Unit oUnit = new Unit();
			unitGroup.getUnits().add(oUnit);
			mapUnitAttributes(iUnit, oUnit);
			if (iUnit.getDataSetInternalID() != null) {
				int id = iUnit.getDataSetInternalID().intValue();
				if (id == refUnitId)
					unitGroup.setReferenceUnit(oUnit);
			}
		}
	}

	private void mapUnitAttributes(org.openlca.ilcd.units.Unit iUnit, Unit oUnit) {
		UnitExtension extension = new UnitExtension(iUnit);
		if (extension.isValid())
			oUnit.setRefId(extension.getUnitId());
		else
			oUnit.setRefId(UUID.randomUUID().toString());
		oUnit.setName(iUnit.getName());
		oUnit.setDescription(LangString.getLabel(iUnit.getGeneralComment()));
		oUnit.setConversionFactor(iUnit.getMeanValue());
	}

	private void saveInDatabase(UnitGroup obj) throws ImportException {
		try {
			database.createDao(UnitGroup.class).insert(obj);
		} catch (Exception e) {
			String message = String.format(
					"Save operation failed in unit group %s.",
					unitGroup.getRefId());
			throw new ImportException(message, e);
		}
	}

}
