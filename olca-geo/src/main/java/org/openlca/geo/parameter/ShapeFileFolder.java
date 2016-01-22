package org.openlca.geo.parameter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.google.gson.Gson;

/**
 * A folder where shapefiles are stored.
 */
public class ShapeFileFolder {

	private Logger log = LoggerFactory.getLogger(getClass());

	public final File folder;

	public ShapeFileFolder(File folder) {
		log.trace("init shapefile repository @ {}", folder);
		this.folder = folder;
		if (!folder.exists())
			folder.mkdirs();
	}

	/**
	 * Get the names (without file extension) of all shapefiles in this
	 * repository.
	 */
	public List<String> getShapeFiles() {
		List<String> names = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (!ShapeFileUtils.hasValidExtension(file))
				continue;
			String name = ShapeFileUtils.getName(file);
			if (!names.contains(name))
				names.add(name);
		}
		return names;
	}

	/**
	 * Imports the given shape-file and the associated files into the folder.
	 */
	public String importFile(File shapeFile) {
		String shapeFileName = ShapeFileUtils.getName(shapeFile);
		try {
			List<File> importFiles = ShapeFileUtils.getAllFiles(shapeFile);
			for (File importFile : importFiles) {
				File file = new File(folder, importFile.getName());
				Files.copy(importFile, file);
			}
			// initialize the parameter file
			getParameters(shapeFileName);
			return shapeFileName;
		} catch (Exception e) {
			log.error("failed to import shapefile " + shapeFile, e);
			return shapeFileName;
		}
	}

	public DataStore openDataStore(String shapeFile) {
		try {
			File file = new File(folder, shapeFile + ".shp");
			log.trace("open shape-file {}", file);
			return new ShapefileDataStore(file.toURI().toURL());
		} catch (Exception e) {
			log.error("Failed to open shape-file", e);
			return null;
		}
	}

	public List<ShapeFileParameter> getParameters(String shapeFile) {
		File paramFile = new File(folder, shapeFile + ".gisolca");
		if (paramFile.exists())
			return readParameterFile(paramFile);
		else {
			log.trace("create parameter file {}", paramFile);
			DataStore store = openDataStore(shapeFile);
			List<ShapeFileParameter> params = readParameters(store);
			writeParameterFile(shapeFile, params);
			store.dispose();
			return params;
		}
	}

	private List<ShapeFileParameter> readParameterFile(File paramFile) {
		log.trace("read parameter file {}", paramFile);
		try (FileInputStream is = new FileInputStream(paramFile);
				InputStreamReader reader = new InputStreamReader(is, "utf-8");
				BufferedReader buffer = new BufferedReader(reader)) {
			Gson gson = new Gson();
			ShapeFileParameter[] params = gson.fromJson(buffer,
					ShapeFileParameter[].class);
			return Arrays.asList(params);
		} catch (Exception e) {
			log.error("failed to read paramater file " + paramFile, e);
			return Collections.emptyList();
		}
	}

	private void writeParameterFile(String shapeFile,
			List<ShapeFileParameter> parameters) {
		File paramFile = new File(folder, shapeFile + ".gisolca");
		try (FileOutputStream os = new FileOutputStream(paramFile);
				OutputStreamWriter writer = new OutputStreamWriter(os, "utf-8");
				BufferedWriter buffer = new BufferedWriter(writer)) {
			Gson gson = new Gson();
			gson.toJson(parameters, buffer);
		} catch (Exception e) {
			log.error("failed to write paramater file " + shapeFile, e);
		}
	}

	private List<ShapeFileParameter> readParameters(DataStore dataStore) {
		if (dataStore == null)
			return Collections.emptyList();
		try {
			Map<String, ShapeFileParameter> params = new HashMap<>();
			String typeName = dataStore.getTypeNames()[0];
			SimpleFeatureCollection source = dataStore.getFeatureSource(
					typeName).getFeatures();
			SimpleFeatureIterator it = source.features();
			while (it.hasNext()) {
				SimpleFeature feature = it.next();
				readParameters(params, feature);
			}
			it.close();
			List<ShapeFileParameter> list = new ArrayList<>();
			list.addAll(params.values());
			return list;
		} catch (Exception e) {
			log.error("failed to get parameters from shape file", e);
			return Collections.emptyList();
		}
	}

	private static void readParameters(Map<String, ShapeFileParameter> params,
			SimpleFeature feature) {
		for (Property property : feature.getProperties()) {
			if (!(property.getValue() instanceof Number))
				continue;
			if (property.getName() == null)
				continue;
			String name = property.getName().toString();
			double value = ((Number) property.getValue()).doubleValue();
			ShapeFileParameter param = params.get(name);
			if (param == null) {
				param = new ShapeFileParameter();
				param.name = name;
				param.max = value;
				param.min = value;
				params.put(name, param);
			} else {
				param.max = Math.max(param.max, value);
				param.min = Math.min(param.min, value);
			}
		}
	}

}
