package org.openlca.util;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class Processes {

	private Processes() {
	}

	/**
	 * Searches the given database for a process with the given label. A label
	 * can contain a suffix with a location code which is considered in this
	 * function.
	 */
	public static ProcessDescriptor findForLabel(IDatabase db, String label) {
		if (db == null || label == null)
			return null;
		String fullName = label;
		String name = null;
		Location location = null;
		if (fullName.contains(" - ")) {
			int splitIdx = fullName.lastIndexOf(" - ");
			name = fullName.substring(0, splitIdx).trim();
			String locationCode = fullName.substring(splitIdx + 3).trim();
			LocationDao dao = new LocationDao(db);
			for (Location loc : dao.getAll()) {
				if (Strings.nullOrEqual(loc.getCode(), locationCode)) {
					location = loc;
					break;
				}
			}
		}

		ProcessDescriptor selected = null;
		ProcessDao pDao = new ProcessDao(db);
		for (ProcessDescriptor d : pDao.getDescriptors()) {
			if (!Strings.nullOrEqual(fullName, d.name)
					&& !Strings.nullOrEqual(name, d.name))
				continue;
			if (selected == null) {
				selected = d;
				if (matchLocation(selected, location))
					break;
				else
					continue;
			}
			if (matchLocation(d, location)
					&& !matchLocation(selected, location)) {
				selected = d;
				break;
			}
		}
		return selected;
	}

	private static boolean matchLocation(ProcessDescriptor d, Location loc) {
		if (d == null)
			return false;
		if (d.location == null)
			return loc == null;
		if (loc == null)
			return d.location == null;
		return d.location.longValue() == loc.getId();
	}
}
