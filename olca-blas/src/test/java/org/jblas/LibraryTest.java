package org.jblas;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.jblas.Library;

public class LibraryTest {

	private static File dir;

	@BeforeClass
	public static void makeDir() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File tmpDir = new File(tmpDirPath);
		dir = new File(tmpDir, "jblas-tests");
		if (!dir.exists())
			dir.mkdirs();
	}

	@Test
	public void testLoadFromDir() throws Exception {
		Library.loadFromDir(dir);
		assertTrue(Library.isLoaded());
		String blasLib = System.mapLibraryName("jblas");
		assertBlasLibLoaded(blasLib);
	}

	@SuppressWarnings("unchecked")
	private void assertBlasLibLoaded(String blasLib)
			throws NoSuchFieldException, IllegalAccessException {
		Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
		field.setAccessible(true);
		Vector<String> libs = (Vector<String>) field.get(getClass()
				.getClassLoader());
		boolean blasLibFound = false;
		for (String lib : libs)
			if (lib.endsWith(blasLib))
				blasLibFound = true;
		assertTrue(blasLibFound);
	}

}
