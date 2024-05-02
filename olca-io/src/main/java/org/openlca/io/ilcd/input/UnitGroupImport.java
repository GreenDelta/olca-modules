package org.openlca.io.ilcd.input;

import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroups;

public class UnitGroupImport {

	private final Import imp;
	private final org.openlca.ilcd.units.UnitGroup ds;
	private UnitGroup unitGroup;

	public UnitGroupImport(Import imp, org.openlca.ilcd.units.UnitGroup ds) {
		this.imp = imp;
		this.ds = ds;
	}

	public UnitGroup run() {
		var group = imp.db().get(UnitGroup.class, UnitGroups.getUUID(ds));
		if (group != null) {
			new UnitGroupSync(group, this.ds, imp).run(imp.db());
			return group;
		}
		return createNew();
	}

	public static UnitGroup get(Import imp, String id) {
		var group = imp.db().get(UnitGroup.class, id);
		return group != null
				? group
				: imp.getFromStore(org.openlca.ilcd.units.UnitGroup.class, id)
				.map(ds -> new UnitGroupImport(imp, ds).run())
				.orElse(null);
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
		unitGroup.refId = UnitGroups.getUUID(ds);
		var info = UnitGroups.getDataSetInfo(ds);
		if (info != null) {
			unitGroup.name = imp.str(info.getName());
			unitGroup.description = imp.str(info.getComment());
		}
		Import.mapVersionInfo(ds, unitGroup);
	}

	private void createUnits() {
		var qref = UnitGroups.getQuantitativeReference(ds);
		Integer refUnitId = qref != null
				? qref.getReferenceUnit()
				: null;
		for (var iUnit : UnitGroups.getUnits(ds)) {
			Unit oUnit = new Unit();
			unitGroup.units.add(oUnit);
			mapUnitAttributes(iUnit, oUnit);
			if (refUnitId != null && refUnitId == iUnit.getId()) {
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
		oUnit.name = iUnit.getName();
		oUnit.description = imp.str(iUnit.getComment());
		oUnit.conversionFactor = iUnit.getFactor();
	}

}
