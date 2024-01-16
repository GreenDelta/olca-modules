package org.openlca.io.ilcd.input;

import java.util.Calendar;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronisation of an existing unit group in the database with an imported
 * unit group data set. A synchronisation is only done if the openLCA extensions
 * are available in the ILCD data set (which is basically only the unit ID).
 * <p>
 * The synchronisation adds new units to a unit-group data set in openLCA if it
 * is not yet contained in the database. If there is a new unit there are two
 * possible cases:
 *
 * <li>The reference unit in the openLCA data set is the same as for the ILCD
 * data set. Then the new unit just needs to be added.
 *
 * <li>The reference unit in the openLCA data set is NOT the same as for the
 * ILCD data set. Then a conversion factor needs to be applied for the factor of
 * the new unit: <code>f_olca = f_olca_ref/f_ilcd_ref * f_ilcd</code>
 *
 */
class UnitGroupSync {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final UnitGroup group;
	private final org.openlca.ilcd.units.UnitGroup ds;
	private final Import imp;

	public UnitGroupSync(
			UnitGroup group, org.openlca.ilcd.units.UnitGroup ds, Import imp) {
		this.group = group;
		this.ds = ds;
		this.imp = imp;
	}

	public void run(IDatabase db) {
		try {
			Unit olcaRefUnit = group.referenceUnit;
			org.openlca.ilcd.units.Unit ilcdRefUnit = findRefUnit(olcaRefUnit);
			if (ilcdRefUnit == null)
				return;
			double factor = olcaRefUnit.conversionFactor
					/ ilcdRefUnit.factor;
			boolean changed = syncUnits(factor);
			if (changed) {
				group.lastChange = Calendar.getInstance().getTimeInMillis();
				Version.incUpdate(group);
				new UnitGroupDao(db).update(group);
			}
		} catch (Exception e) {
			log.error("Failed to sync. unit groups", e);
		}
	}

	private org.openlca.ilcd.units.Unit findRefUnit(Unit olcaRefUnit) {
		if (olcaRefUnit == null)
			return null;
		for (var unit : UnitGroups.getUnits(ds)) {
			var ext = new UnitExtension(unit);
			var id = ext.getUnitId();
			if (id != null && id.equals(olcaRefUnit.refId))
				return unit;
		}
		return null;
	}

	private boolean syncUnits(double factor) {
		boolean changed = false;
		for (var ilcdUnit : UnitGroups.getUnits(ds)) {
			var ext = new UnitExtension(ilcdUnit);
			String id = ext.getUnitId();
			if (id == null || containsUnit(id))
				continue;
			var unit = new Unit();
			unit.refId = id;
			unit.name = ilcdUnit.name;
			unit.conversionFactor = factor * ilcdUnit.factor;
			unit.description = imp.str(ilcdUnit.comment);
			group.units.add(unit);
			changed = true;
		}
		return changed;
	}

	private boolean containsUnit(String id) {
		for (Unit unit : group.units) {
			if (id.equals(unit.refId))
				return true;
		}
		return false;
	}

}
