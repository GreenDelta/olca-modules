package org.openlca.julia;

import org.openlca.util.OS;

public enum JuliaModule {

	OPEN_BLAS("jolcablas"),

	UMFPACK("jolcaumf");

	private final String baseName;

	private JuliaModule(String baseName) {
		this.baseName = baseName;
	}

	String libName() {
		OS os = OS.getCurrent();
		if (os == null)
			return "lib" + baseName + ".so";
		switch (os) {
		case Linux:
			return "lib" + baseName + ".so";
		case Mac:
			return "lib" + baseName + ".dylib";
		case Windows:
			return "lib" + baseName + ".dll";
		default:
			return "lib" + baseName + ".so";
		}
	}
}
