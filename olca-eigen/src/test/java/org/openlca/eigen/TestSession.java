package org.openlca.eigen;

import java.io.File;

public class TestSession {

	public static void loadLib() {
		if (NativeLibrary.isLoaded())
			return;
		String tempDirPath = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tempDirPath);
		NativeLibrary.loadFromDir(tmpDir);
	}

}
