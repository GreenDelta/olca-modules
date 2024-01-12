package org.openlca.io.ilcd.input;

import java.util.Date;
import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroupBag;

public class UnitGroupImport {

	private final Import imp;
	private UnitGroupBag ilcdUnitGroup;
	private UnitGroup unitGroup;

	public UnitGroupImport(Import imp) {
		this.imp = imp;
	}

	public UnitGroup run(org.openlca.ilcd.units.UnitGroup dataSet) {
		this.ilcdUnitGroup = new UnitGroupBag(dataSet, imp.langOrder());
		var group = imp.db().get(UnitGroup.class, dataSet.getUUID());
		if (group != null) {
			new UnitGroupSync(group, ilcdUnitGroup, imp).run(imp.db());
			return group;
		}
		return createNew();
	}

	public static UnitGroup get(Import imp, String id) {
		var group = imp.db().get(UnitGroup.class, id);
		if (group != null)
			// TODO: check if reference unit is in database!
			return group;
		var dataSet = imp.store().get(
			org.openlca.ilcd.units.UnitGroup.class, id);
		if (dataSet == null) {
			imp.log().error("invalid reference in ILCD data set:" +
				" unit group '" + id + "' does not exist");
			return null;
		}
		return new UnitGroupImport(imp).run(dataSet);
	}

	private UnitGroup createNew() {
		unitGroup = new UnitGroup();
		var path = Categories.getPath(ilcdUnitGroup.getValue());
		unitGroup.category = new CategoryDao(imp.db())
				.sync(ModelType.UNIT_GROUP, path);
		mapDescriptionAttributes();
		createUnits();
		return imp.insert(unitGroup);
	}

	private void mapDescriptionAttributes() {
		unitGroup.refId = ilcdUnitGroup.getId();
		unitGroup.name = ilcdUnitGroup.getName();
		unitGroup.description = ilcdUnitGroup.getComment();
		String v = ilcdUnitGroup.getVersion();
		unitGroup.version = Version.fromString(v).getValue();
		Date time = ilcdUnitGroup.getTimeStamp();
		if (time != null)
			unitGroup.lastChange = time.getTime();
	}

	private void createUnits() {
		Integer refUnitId = ilcdUnitGroup.getReferenceUnitId();
		for (var iUnit : ilcdUnitGroup.getUnits()) {
			if (iUnit == null)
				continue;
			Unit oUnit = new Unit();
			unitGroup.units.add(oUnit);
			mapUnitAttributes(iUnit, oUnit);
			if (refUnitId != null && refUnitId == iUnit.id) {
				unitGroup.referenceUnit = oUnit;
			}
		}
	}

	private void mapUnitAttributes(
		org.openlca.ilcd.units.Unit iUnit, Unit oUnit) {
		var extension = new UnitExtension(iUnit);
		oUnit.refId = extension.isValid()
			? extension.getUnitId()
			: UUID.randomUUID().toString();

		oUnit.name = iUnit.name;
		oUnit.description = imp.str(iUnit.comment);
		oUnit.conversionFactor = iUnit.factor;
	}

}
