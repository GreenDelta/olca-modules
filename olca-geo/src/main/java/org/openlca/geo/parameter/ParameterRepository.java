package org.openlca.geo.parameter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import org.openlca.geo.kml.KmlFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class ParameterRepository {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();

	private ShapeFileRepository shapeFileRepository;

	public ParameterRepository(ShapeFileRepository shapeFileRepository) {
		this.shapeFileRepository = shapeFileRepository;
	}

	public Map<String, Double> load(KmlFeature feature, String shapeFile) {
		if (!contains(feature, shapeFile)) // also checks that input is valid
			return null;
		File file = getFile(feature, shapeFile);
		try (FileReader reader = new FileReader(file)) {
			return gson.fromJson(reader, MapType.get());
		} catch (IOException e) {
			log.warn("Error loading file from parameter repository", e);
			return null;
		}
	}

	public void save(KmlFeature feature, String shapeFile,
			Map<String, Double> parameterMap) {
		if (!isValidInput(feature, shapeFile, parameterMap))
			return;
		File file = getFile(feature, shapeFile);
		if (!file.exists())
			if (!create(file))
				return;
		try (FileWriter writer = new FileWriter(file)) {
			gson.toJson(parameterMap, MapType.get(), writer);
		} catch (IOException e) {
			log.warn("Error saving file to parameter repository", e);
		}
	}

	public void remove(KmlFeature feature, String shapeFile) {
		if (!isValidInput(feature, shapeFile))
			return;
		File file = getFile(feature, shapeFile);
		if (!file.exists())
			return;
		file.delete();
	}

	private File getFile(KmlFeature feature, String shapeFile) {
		File folder = shapeFileRepository.getFolder();
		File shapeFileFolder = new File(folder, shapeFile);
		File featureFile = new File(shapeFileFolder, Long.toString(feature
				.getIdentifier()));
		return featureFile;
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

	private boolean isValidInput(KmlFeature feature, String shapeFile) {
		if (feature == null)
			return false;
		if (feature.getIdentifier() == 0l)
			return false;
		if (Strings.isNullOrEmpty(shapeFile))
			return false;
		return true;
	}

	private boolean isValidInput(KmlFeature feature, String shapeFile,
			Map<String, Double> parameterMap) {
		if (!isValidInput(feature, shapeFile))
			return false;
		if (parameterMap == null)
			return false;
		return true;
	}

	public boolean contains(KmlFeature feature, String shapeFile) {
		if (!isValidInput(feature, shapeFile))
			return false;
		File file = getFile(feature, shapeFile);
		return file.exists();
	}

	private static class MapType extends TypeToken<Map<String, Double>> {

		private static final long serialVersionUID = 5110185914693769552L;

		private static Type get() {
			return new MapType().getType();
		}

	}

}
