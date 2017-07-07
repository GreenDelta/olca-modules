package org.openlca.core.matrix.solvers;

import java.io.File;

import org.openlca.eigen.NativeLibrary;

class TestSession {

	public static void loadLib() {
		if (NativeLibrary.isLoaded())
			return;
		String tempDirPath = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tempDirPath);
		NativeLibrary.loadFromDir(tmpDir);
	}

}
