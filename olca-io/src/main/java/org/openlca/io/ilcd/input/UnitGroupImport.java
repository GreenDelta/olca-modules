package org.openlca.io.ilcd.input;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroups;

import java.util.UUID;

public class UnitGroupImport {

	private final Import imp;
	private final org.openlca.ilcd.units.UnitGroup ds;
	private UnitGroup unitGroup;

	public UnitGroupImport(Import imp, org.openlca.ilcd.units.UnitGroup ds) {
		this.imp = imp;
		this.ds = ds;
	}

	public UnitGroup run() {
		var group = imp.db().get(UnitGroup.class, ds.getUUID());
		if (group != null) {
			new UnitGroupSync(group, this.ds, imp).run(imp.db());
			return group;
		}
		return createNew();
	}

	public static UnitGroup get(Import imp, String id) {
		var group = imp.db().get(UnitGroup.class, id);
		if (group != null)
			// TODO: check if reference unit is in database!
			return group;
		var ds = imp.store().get(
			org.openlca.ilcd.units.UnitGroup.class, id);
		if (ds == null) {
			imp.log().error("invalid reference in ILCD data set:" +
				" unit group '" + id + "' does not exist");
			return null;
		}
		return new UnitGroupImport(imp, ds).run();
	}

	private UnitGroup createNew() {
		unitGroup = new UnitGroup();
		var path = Categories.getPath(ds);
		unitGroup.category = new CategoryDao(imp.db())
				.sync(ModelType.UNIT_GROUP, path);
		mapDescriptionAttributes();
		createUnits();
		return imp.insert(unitGroup);
	}

	private void mapDescriptionAttributes() {
		unitGroup.refId = ds.getUUID();
		var info = UnitGroups.getDataSetInfo(ds);
		if (info != null) {
			unitGroup.name = imp.str(info.name);
			unitGroup.description = imp.str(info.generalComment);
		}
		unitGroup.version = Version.fromString(ds.getVersion()).getValue();

		var entry = UnitGroups.getDataEntry(ds);
		if (entry != null && entry.timeStamp != null) {
			unitGroup.lastChange = entry.timeStamp
					.toGregorianCalendar()
					.getTimeInMillis();
		}
	}

	private void createUnits() {
		var qref = UnitGroups.getQuantitativeReference(ds);
		Integer refUnitId = qref != null
				? qref.referenceUnit
				: null;
		for (var iUnit : UnitGroups.getUnits(ds)) {
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
