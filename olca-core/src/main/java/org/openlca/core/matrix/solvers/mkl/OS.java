package org.openlca.core.matrix.solvers.mkl;

import java.util.Locale;

enum OS {

	Linux(new String[]{
		"libmkl_rt.so",
		"libolcamkl.so"
	}),

	MacOS(new String[]{
		"libmkl_rt.2.dylib",
		"libolcamkl.dylib"
	}),

	Windows(new String[]{
		"mkl_rt.2.dll",
		"olcamkl.dll",
	});

	private final String[] libraries;

	OS(String[] libraries) {
		this.libraries = libraries;
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

	String[] libraries() {
		return libraries;
	}
}
