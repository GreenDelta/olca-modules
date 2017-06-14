package org.openlca.ilcd.io;

import java.nio.file.Path;

final class Util {

	private Util() {
	}

	/**
	 * Returns true if the given path describes an XML file (has an *.xml file
	 * extension)
	 */
	public static boolean isXml(Path path) {
		if (path == null)
			return false;
		String s = path.toString();
		int len = s.length();
		if (len < 5)
			return false;
		s = s.substring(len - 4, len).toLowerCase();
		return s.equals(".xml");
	}

}
