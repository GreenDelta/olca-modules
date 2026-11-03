package org.openlca.core.matrix.solvers.mkl;

import java.util.Locale;

enum OS {

	Linux("libmkl_rt.so.2"),

	MacOS("libmkl_rt.2.dylib"),

	Windows("mkl_rt.2.dll");

	private final String library;

	OS(String library) {
		this.library = library;
	}

	static OS detect() {
		var os = System.getProperty("os.name", "generic")
			.toLowerCase(Locale.ENGLISH);
		if (os.contains("mac") || os.contains("darwin"))
			return OS.MacOS;
		if (os.contains("win"))
			return OS.Windows;
		return OS.Linux;
	}

	/// The file name of the MKL runtime library on this platform.
	String library() {
		return library;
	}
}
