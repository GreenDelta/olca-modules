package org.openlca.ilcd.epd.conversion;

import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.EpdProfile;
import org.openlca.ilcd.processes.Process;

public class EpdExtensions {

	private EpdExtensions() {
	}

	public static EpdDataSet read(Process process) {
		return read(process, EpdProfile.create());
	}

	/**
	 * Reads the extensions of the given process data set into an EPD data set.
	 * The given process data set will be wrapped by the EPD data set.
	 */
	public static EpdDataSet read(Process process, EpdProfile profile) {
		return EpdExtensionReader.read(process, profile);
	}

	/**
	 * Write the EPD extensions into the underlying process data set of the
	 * given EPD.
	 */
	public static void write(EpdDataSet epd) {
		EpdExtensionWriter.write(epd);
	}
}
