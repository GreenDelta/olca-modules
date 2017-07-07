package org.openlca.eigen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for loading the native library from a file directory.
 */
public class NativeLibrary {

	private static final String VERSION = "0.3";
	private static final String LIB_NAME = "olca-eigen";
	private static boolean loaded = false;
	private static Logger log = LoggerFactory.getLogger(NativeLibrary.class);

	public static void loadFromDir(File dir) throws UnsatisfiedLinkError {
		if (loaded) {
			log.trace("{} lib already loaded", LIB_NAME);
			return;
		}
		File realDir = makeRealDir(dir);
		log.trace("try load {} from directory {}", LIB_NAME, realDir);
		String lib = System.mapLibraryName(LIB_NAME);
		try {
			String blasPath = getJarPath(lib);
			loaded = loadLib(realDir, lib, blasPath);
			// checkLib();
		} catch (UnsatisfiedLinkError e) {
			loaded = false;
			log.info("failed to load jblas-library: " + e.getMessage());
			throw e;
		} catch (Throwable e) {
			log.info("failed to load jblas-library", e);
			loaded = false;
		}
	}

	private static File makeRealDir(File dir) {
		String os = getOs();
		String arch = System.getProperty("os.arch");
		String sep = File.separator;
		String subPath = LIB_NAME.concat("_").concat(VERSION)
				.concat(sep).concat(os).concat(sep).concat(arch);
		File realDir = new File(dir, subPath);
		if (!realDir.exists())
			realDir.mkdirs();
		return realDir;
	}

	private static boolean loadLib(File dir, String lib, String path) throws IOException {
		File libFile = new File(dir, lib);
		if (!libFile.exists()) {
			if (!copyLib(path, libFile))
				return false;
		}
		System.load(libFile.getAbsolutePath());
		return true;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	private static String getJarPath(String lib) {
		String os = getOs();
		String arch = System.getProperty("os.arch");
		String path = "/lib/" + os + "/" + arch + "/";
		return path + lib;
	}

	private static boolean copyLib(String jarPath, File file) throws IOException {
		InputStream is = NativeLibrary.class.getResourceAsStream(jarPath);
		if (is == null)
			return false;
		FileOutputStream os = new FileOutputStream(file);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0)
			os.write(buf, 0, len);
		os.flush();
		os.close();
		return true;
	}

	private static String getOs() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows"))
			return "Windows";
		else
			return os;
	}

	// private static void checkLib() {
	// log.trace("check loaded library");
	// DoubleMatrix A = new DoubleMatrix(
	// new double[][] { { 1, -2 }, { 0, 1 } });
	// DoubleMatrix B = new DoubleMatrix(new double[][] { { 0 }, { 1 } });
	// DoubleMatrix x = Solve.solve(A, B);
	// fail("not one column", x.columns != 1);
	// fail("not two rows", x.rows != 2);
	// fail("wrong result, expected [2 ; 1] but was " + x, x.get(0, 0) != 2);
	// fail("wrong result, expected [2 ; 1] but was " + x, x.get(1, 0) != 1);
	// }

	private static void fail(String message, boolean condition) {
		if (condition)
			throw new AssertionError(message);
	}
}
