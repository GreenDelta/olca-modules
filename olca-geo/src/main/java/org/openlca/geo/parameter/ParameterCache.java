package org.openlca.geo.parameter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * This does not save the parameters values but the parameter shares of
 * geometric features of locations.
 */
public class ParameterCache {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();

	private ShapeFileFolder shapeFileFolder;

	public ParameterCache(ShapeFileFolder shapeFileFolder) {
		this.shapeFileFolder = shapeFileFolder;
	}

	public Map<String, Double> load(long locationId, String shapeFile) {
		if (!contains(locationId, shapeFile)) // also checks that input
												// is valid
			return null;
		File file = getParameterFile(locationId, shapeFile);
		try (FileReader reader = new FileReader(file)) {
			return gson.fromJson(reader, MapType.get());
		} catch (IOException e) {
			log.warn("Error loading file from parameter repository", e);
			return null;
		}
	}

	public void save(long locationId, String shapeFile, Map<String, Double> parameterMap) {
		if (!isValidInput(locationId, shapeFile, parameterMap))
			return;
		File file = getParameterFile(locationId, shapeFile);
		if (!file.exists())
			if (!create(file))
				return;
		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(parameterMap, MapType.get(), writer);
		} catch (IOException e) {
			log.warn("Error saving file to parameter repository", e);
		}
	}

	public void remove(long locationId, String shapeFile) {
		if (!isValidInput(locationId, shapeFile))
			return;
		File file = getParameterFile(locationId, shapeFile);
		if (!file.exists())
			return;
		file.delete();
	}

	public void remove(String shapeFile) {
		File folder = shapeFileFolder.folder;
		File shapeFileFolder = new File(folder, shapeFile);
		if (!shapeFileFolder.exists())
			return;
		if (!shapeFileFolder.isDirectory())
			return;
		for (File file : shapeFileFolder.listFiles())
			if (!file.delete())
				file.deleteOnExit();
		if (!shapeFileFolder.delete())
			shapeFileFolder.deleteOnExit();
	}

	private File getParameterFile(long locationId, String shapeFile) {
		File folder = shapeFileFolder.folder;
		File paramFolder = new File(folder, shapeFile);
		return new File(paramFolder, Long.toString(locationId));
	}

	private boolean create(File file) {
		if (file.exists())
			return true;
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try {
			file.createNewFile();
			return true;
		} catch (IOException e) {
			log.warn("Error creating file " + file.getAbsolutePath());
			return false;
		}
	}

	private boolean isValidInput(long locationId, String shapeFile) {
		return locationId > 0 && !Strings.isNullOrEmpty(shapeFile);
	}

	private boolean isValidInput(long locationId, String shapeFile,
			Map<String, Double> parameterMap) {
		if (!isValidInput(locationId, shapeFile))
			return false;
		if (parameterMap == null)
			return false;
		return true;
	}

	public boolean contains(long locationId, String shapeFile) {
		if (!isValidInput(locationId, shapeFile))
			return false;
		File file = getParameterFile(locationId, shapeFile);
		return file.exists();
	}

	private static class MapType extends TypeToken<Map<String, Double>> {

		private static final long serialVersionUID = 5110185914693769552L;

		private static Type get() {
			return new MapType().getType();
		}

	}

}
