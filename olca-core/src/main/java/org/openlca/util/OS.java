package org.openlca.util;

import java.io.File;

/**
 * Constants for some operating systems which are retrieved from the system
 * property "os.name". See http://lopica.sourceforge.net/os.html for a list of
 * OS names in Java.
 */
public enum OS {

	Linux("Linux"),

	Mac("Mac OS"),

	Windows("Windows"),

	Unknown("Unknown");

	private final String name;

	private OS(String name) {
		this.name = name;
	}

	public static OS getCurrent() {
		String name = System.getProperty("os.name");
		OS os = null;
		int i = 0;
		OS[] vals = values();
		while (os == null && i < vals.length) {
			if (name != null && name.startsWith(vals[i].name))
				os = vals[i];
			i++;
		}
		return os != null ? os : Unknown;
	}

	public static File getTempDir() {
		String tempDir = System.getProperty("java.io.tmpdir");
		return new File(tempDir);
	}

	@Override
	public String toString() {
		return name;
	}

}
