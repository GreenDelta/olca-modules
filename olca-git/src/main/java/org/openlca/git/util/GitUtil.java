package org.openlca.git.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

public class GitUtil {

	public static final String BIN_DIR_SUFFIX = "_bin";
	public static final String DATASET_SUFFIX = ".json";
	public static final String EMPTY_CATEGORY_FLAG = ".empty";
	private static final Map<String, String> encodings = new HashMap<>();

	static {
		encodings.put("<", "%3C");
		encodings.put(">", "%3E");
		encodings.put(":", "%3A");
		encodings.put("\"", "%22");
		encodings.put("\\", "%5C");
		encodings.put("|", "%7C");
		encodings.put("?", "%3F");
		encodings.put("*", "%2A");
	}

	public static String encode(String name) {
		if (name == null)
			return null;
		for (Entry<String, String> entry : encodings.entrySet()) {
			name = name.replace(entry.getKey(), entry.getValue());
		}
		for (var i = 0; i < name.length(); i++) {
			if (name.charAt(i) == '.'
					&& (i == 0 || i == name.length() - 1 || name.charAt(i - 1) == '/' || name.charAt(i + 1) == '/')) {
				name = encodeDot(name, i);
			}
		}
		return name;
	}

	private static String encodeDot(String name, int index) {
		if (index == 0)
			return "%2E" + name.substring(1);
		return name.substring(0, index) + "%2E" + name.substring(index + 1);
	}

	public static String decode(String name) {
		if (name == null)
			return null;
		for (Entry<String, String> entry : encodings.entrySet()) {
			name = name.replace(entry.getValue(), entry.getKey());
		}
		for (var i = 0; i < name.length() - 2; i++) {
			if ((name.charAt(i) == '%' && name.charAt(i + 1) == '2' && name.charAt(i + 2) == 'E')
					&& (i == 0 || i == name.length() - 3 || name.charAt(i - 1) == '/' || name.charAt(i + 3) == '/')) {
				name = decodeDot(name, i);
			}
		}
		return name;
	}

	private static String decodeDot(String name, int index) {
		if (index == 0)
			return "." + name.substring(3);
		return name.substring(0, index) + "." + name.substring(index + 3);
	}

	public static boolean isDatasetPath(String path) {
		return containsValidRefIdAndEndsWith(path, DATASET_SUFFIX);
	}

	public static String findBinDir(String path) {
		String binDir = null;
		while (path.contains(BIN_DIR_SUFFIX + "/")) {
			path = path.substring(0, path.lastIndexOf(BIN_DIR_SUFFIX + "/") + BIN_DIR_SUFFIX.length());
			if (isBinDirPath(path)) {
				binDir = path;
			}
		}
		return binDir;
	}

	public static boolean isBinDirPath(String path) {
		return containsValidRefIdAndEndsWith(path, BIN_DIR_SUFFIX);
	}

	private static boolean containsValidRefIdAndEndsWith(String path, String suffixTest) {
		if (!path.toLowerCase().endsWith(suffixTest))
			return false;
		var lastSlash = path.lastIndexOf("/");
		if (lastSlash != -1 && path.length() <= lastSlash + suffixTest.length() + 1)
			return false;
		var refId = path.substring(0, path.lastIndexOf(suffixTest));
		if (refId.contains("/")) {
			refId = refId.substring(refId.lastIndexOf("/") + 1);
		}
		return isValidRefId(refId);
	}

	public static boolean isBinDirOf(String binDirPath, String datasetPath) {
		return GitUtil.isDatasetPath(datasetPath)
				&& binDirPath.equals(
						datasetPath.substring(0, datasetPath.length() - DATASET_SUFFIX.length())
								+ BIN_DIR_SUFFIX);
	}

	public static boolean isBinDirOrFileOf(String binFilePath, String datasetPath) {
		return GitUtil.isDatasetPath(datasetPath)
				&& binFilePath.startsWith(
						datasetPath.substring(0, datasetPath.length() - DATASET_SUFFIX.length())
								+ BIN_DIR_SUFFIX);
	}

	public static boolean isValidRefId(String value) {
		if (value == null || value.trim().isBlank())
			return false;
		if (value.endsWith(BIN_DIR_SUFFIX))
			return false;
		if (value.endsWith(DATASET_SUFFIX))
			return false;
		if (value.endsWith(EMPTY_CATEGORY_FLAG))
			return false;
		return true;
	}
	
	public static boolean isValidCategory(String category) {
		if (category == null || category.trim().isBlank())
			return true;
		var split = category.split("/");
		for (var sub : split) {
			if (sub.isBlank())
				return false;
			if (sub.endsWith(DATASET_SUFFIX))
				return false;
			if (sub.endsWith(BIN_DIR_SUFFIX))
				return false;
			if (sub.endsWith(EMPTY_CATEGORY_FLAG))
				return false;
		}
		return true;
	}

	public static String getRefId(String path) {
		var refId = getRefId(path, DATASET_SUFFIX);
		if (refId != null)
			return refId;
		return getRefId(BIN_DIR_SUFFIX);
	}

	private static String getRefId(String path, String suffix) {
		if (!path.endsWith(suffix))
			return null;
		if (path.contains("/"))
			return path.substring(path.lastIndexOf("/") + 1, path.length() - suffix.length());
		return path.substring(0, path.length() - suffix.length());
	}

	public static boolean isEmptyCategoryPath(String path) {
		return path.endsWith("/" + GitUtil.EMPTY_CATEGORY_FLAG);
	}

	public static boolean isEmptyCategoryFile(String filename) {
		return filename.equals(GitUtil.EMPTY_CATEGORY_FLAG);
	}

	public static String toDatasetPath(ModelType type, String refId) {
		return toDatasetPath(type, null, refId);
	}

	public static String toDatasetPath(ModelType type, String category, String refId) {
		var path = type.name();
		if (!Strings.nullOrEmpty(category)) {
			path += "/" + category;
		}
		return path + "/" + refId + DATASET_SUFFIX;
	}

	public static String toDatasetFilename(String refId) {
		return refId + DATASET_SUFFIX;
	}

	public static String toBinDirPath(ModelType type, String refId) {
		return toBinDirPath(type, null, refId);
	}

	public static String toBinDirPath(ModelType type, String category, String refId) {
		if (type == null || refId == null)
			return null;
		var path = type.name();
		if (!Strings.nullOrEmpty(category)) {
			path += "/" + category;
		}
		return path + "/" + refId + BIN_DIR_SUFFIX;
	}

	public static String toBinDirName(String refId) {
		return refId + BIN_DIR_SUFFIX;
	}

	public static String toEmptyCategoryPath(String categoryPath) {
		return categoryPath + "/" + EMPTY_CATEGORY_FLAG;
	}

	public static byte[] getBytes(ObjectId id) {
		var bytes = new byte[40];
		id.copyRawTo(bytes, 0);
		return bytes;
	}

}
