package org.openlca.ilcd.epd.conversion;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.util.Strings;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Processes;

/**
 * Remove empty elements so that the data set validation is happy.
 */
class Cleanup {

	static void on(EpdDataSet epd) {
		if (epd == null)
			return;

		var info = Processes.getProcessInfo(epd.process);
		if (info != null && isEmpty(info.time)) {
			info.time = null;
		}
		if (info != null && isEmpty(info.geography)) {
			info.geography = null;
		}
		if (info != null && isEmpty(info.technology)) {
			info.technology = null;
		}

		var adminInfo = Processes.getAdminInfo(epd.process);

		// bug #59, remove empty commissioner and goal types
		if (adminInfo != null && adminInfo.commissionerAndGoal != null) {
			var comGoal = adminInfo.commissionerAndGoal;
			if (isEmpty(comGoal.other)) {
				comGoal.other = null;
			}
			if (isEmpty(comGoal)) {
				adminInfo.commissionerAndGoal = null;
			}
		}

		if (adminInfo != null && isEmpty(adminInfo.dataGenerator)) {
			adminInfo.dataGenerator = null;
		}
	}

	private static boolean isEmpty(CommissionerAndGoal comGoal) {
		if (comGoal == null)
			return true;
		return comGoal.commissioners.isEmpty()
					 && comGoal.intendedApplications.isEmpty()
					 && comGoal.project.isEmpty()
					 && isEmpty(comGoal.other);
	}

	private static boolean isEmpty(Time time) {
		if (time == null)
			return true;
		return time.description.isEmpty()
					 && time.referenceYear == null
					 && time.validUntil == null
					 && isEmpty(time.other);
	}

	private static boolean isEmpty(Geography geography) {
		if (geography == null)
			return true;
		return isEmpty(geography.location)
					 && geography.subLocations.isEmpty()
					 && isEmpty(geography.other);
	}

	private static boolean isEmpty(Technology technology) {
		if (technology == null)
			return true;
		return technology.applicability.isEmpty()
					 && technology.description.isEmpty()
					 && technology.includedProcesses.isEmpty()
					 && technology.pictogram == null
					 && technology.pictures.isEmpty();
	}

	private static boolean isEmpty(Location location) {
		if (location == null)
			return true;
		return Strings.nullOrEmpty(location.code)
					 && location.description.isEmpty()
					 && isEmpty(location.other);
	}

	private static boolean isEmpty(DataGenerator generator) {
		if (generator == null)
			return true;
		return generator.contacts.isEmpty()
			&& isEmpty(generator.other);
	}

	private static boolean isEmpty(Other other) {
		return other == null || other.any.isEmpty();
	}
}
