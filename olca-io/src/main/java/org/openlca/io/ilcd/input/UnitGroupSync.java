package org.openlca.io.ilcd.input;

import java.util.Calendar;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroupBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronisation of an existing unit group in the database with an imported
 * unit group data set. A synchronisation is only done if the openLCA extensions
 * are available in the ILCD data set (which is basically only the unit ID).
 * 
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

	private Logger log = LoggerFactory.getLogger(getClass());
	private UnitGroup olcaGroup;
	private UnitGroupBag ilcdGroup;
	private ImportConfig config;

	public UnitGroupSync(UnitGroup olcaGroup, UnitGroupBag ilcdGroup,
			ImportConfig config) {
		this.olcaGroup = olcaGroup;
		this.ilcdGroup = ilcdGroup;
		this.config = config;
	}

	public void run(IDatabase database) {
		try {
			Unit olcaRefUnit = olcaGroup.getReferenceUnit();
			org.openlca.ilcd.units.Unit ilcdRefUnit = findRefUnit(olcaRefUnit);
			if (ilcdRefUnit == null)
				return;
			double factor = olcaRefUnit.getConversionFactor()
					/ ilcdRefUnit.factor;
			boolean changed = syncUnits(factor);
			if (changed) {
				olcaGroup.setLastChange(Calendar.getInstance().getTimeInMillis());
				Version.incUpdate(olcaGroup);
				new UnitGroupDao(database).update(olcaGroup);
			}
		} catch (Exception e) {
			log.error("Failed to sync. unit groups", e);
		}
	}

	private org.openlca.ilcd.units.Unit findRefUnit(Unit olcaRefUnit) {
		if (olcaRefUnit == null)
			return null;
		for (org.openlca.ilcd.units.Unit ilcdUnit : ilcdGroup.getUnits()) {
			UnitExtension ext = new UnitExtension(ilcdUnit);
			String id = ext.getUnitId();
			if (id != null && id.equals(olcaRefUnit.getRefId()))
				return ilcdUnit;
		}
		return null;
	}

	private boolean syncUnits(double factor) {
		boolean changed = false;
		for (org.openlca.ilcd.units.Unit ilcdUnit : ilcdGroup.getUnits()) {
			UnitExtension ext = new UnitExtension(ilcdUnit);
			String id = ext.getUnitId();
			if (id == null || containsUnit(id))
				continue;
			Unit unit = new Unit();
			unit.setRefId(id);
			unit.setName(ilcdUnit.name);
			unit.setConversionFactor(factor * ilcdUnit.factor);
			unit.setDescription(LangString.getFirst(ilcdUnit.comment,
					config.langs));
			olcaGroup.getUnits().add(unit);
			changed = true;
		}
		return changed;
	}

	private boolean containsUnit(String id) {
		for (Unit unit : olcaGroup.getUnits()) {
			if (id.equals(unit.getRefId()))
				return true;
		}
		return false;
	}

}
