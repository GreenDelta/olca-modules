package org.openlca.ilcd.epd.conversion;

import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.EpdProfile;
import org.openlca.ilcd.processes.Process;

public class Extensions {

	private Extensions() {
	}

	/**
	 * Reads the extensions of the given process data set into an EPD data set.
	 * The given process data set will be wrapped by the EPD data set.
	 */
	public static EpdDataSet read(Process process, EpdProfile profile) {
		return EPDExtensionReader.read(process, profile);
	}

	/**
	 * Write the EPD extensions into the underlying process data set of the
	 * given EPD.
	 */
	public static void write(EpdDataSet epd) {
		EPDExtensionWriter.write(epd);
	}
}
