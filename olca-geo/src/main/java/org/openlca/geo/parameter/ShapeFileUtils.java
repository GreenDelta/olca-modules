package org.openlca.geo.parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Utility methods for handling shapefiles (see
 * http://en.wikipedia.org/wiki/Shapefile)
 */
class ShapeFileUtils {

	private ShapeFileUtils() {
	}

	private static final String[] EXTENSIONS = new String[] { "shp", "shx",
			"dbf", "prj", "sbn", "sbx", "fbn", "fbx", "ain", "aih", "ixs",
			"mxs", "atx", "shp.xml", "cpg", "gisolca" };

	/**
	 * Get all related files of the given shape-file including the given file
	 * itself.
	 */
	public static List<File> getAllFiles(File shapeFile) {
		if (shapeFile == null || !shapeFile.exists())
			return Collections.emptyList();
		String name = getName(shapeFile);
		File folder = shapeFile.getParentFile();
		List<File> files = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (name.equals(getName(file)) && hasValidExtension(file))
				files.add(file);
		}
		return files;
	}

	/**
	 * Get the name of the shape file without file extension.
	 */
	public static String getName(File shapeFile) {
		if (shapeFile == null)
			return null;
		String fName = shapeFile.getName();
		if (fName.endsWith(".shp.xml"))
			return fName.substring(0, fName.length() - 8);
		else
			return FilenameUtils.removeExtension(fName);
	}

	public static boolean hasValidExtension(File shapeFile) {
		if (shapeFile == null || shapeFile.isDirectory())
			return false;
		String fName = shapeFile.getName();
		if (fName.endsWith(".shp.xml"))
			return true;
		else
			return FilenameUtils.isExtension(fName, EXTENSIONS);
	}

	/**
	 * Check if the mandatory files that define the shape-file are available
	 * (see http://en.wikipedia.org/wiki/Shapefile).
	 */
	static boolean isValid(File shapeFile) {
		if (shapeFile == null)
			return false;
		if (!shapeFile.exists())
			return false;
		String name = getName(shapeFile);
		String[] mandatoryExtensions = { ".shp", ".shx", ".dbf" };
		File folder = shapeFile.getParentFile();
		for (String ext : mandatoryExtensions) {
			File file = new File(folder, name + ext);
			if (!file.exists())
				return false;
		}
		return true;
	}

}
