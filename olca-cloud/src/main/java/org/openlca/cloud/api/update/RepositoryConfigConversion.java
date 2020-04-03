package org.openlca.cloud.api.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Properties;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RepositoryConfigConversion {

	private final static Logger log = LoggerFactory.getLogger(RepositoryConfigConversion.class);
	private static final String DIR = "cloud";
	private static final String OLD_CONFIG_FILE = "repository.properties";
	private static final String NEW_CONFIG_FILE = "config.json";

	public static boolean needsConversion(IDatabase database) {
		return getOldConfigFile(database).exists();
	}

	public static void applyTo(IDatabase database) {
		Properties properties = loadOldConfig(database);
		if (properties == null)
			return;
		JsonObject config = convertConfig(properties);
		if (config == null)
			return;
		String configId = config.get("id").getAsString();
		String repositoryId = config.get("repositoryId").getAsString();
		moveIndex(database, repositoryId, configId);
		saveNewConfig(database, config);
	}

	private static Properties loadOldConfig(IDatabase database) {
		File file = getOldConfigFile(database);
		if (!file.exists())
			return null;
		Properties properties = new Properties();
		try (FileInputStream stream = new FileInputStream(file)) {
			properties.load(stream);
		} catch (Exception e) {
			log.error("Error converting repository config file for database " + database.getName(), e);
			return null;
		}
		file.delete();
		return properties;
	}

	private static File getOldConfigFile(IDatabase database) {
		return new File(database.getFileStorageLocation(), OLD_CONFIG_FILE);
	}

	private static JsonObject convertConfig(Properties properties) {
		JsonObject config = new JsonObject();
		String repositoryId = properties.getProperty("repositoryId");
		String lastCommitId = properties.getProperty("lastCommitId");
		String configId = UUID.randomUUID().toString();
		if ("null".equals(lastCommitId)) {
			lastCommitId = null;
		}
		config.addProperty("id", configId);
		config.addProperty("baseUrl", properties.getProperty("baseUrl"));
		config.addProperty("repositoryId", repositoryId);
		config.addProperty("username", properties.getProperty("username"));
		config.addProperty("password", properties.getProperty("password"));
		config.addProperty("lastCommitId", lastCommitId);
		config.addProperty("active", true);
		return config;
	}

	private static void saveNewConfig(IDatabase database, JsonObject config) {
		String configId = config.get("id").getAsString();
		File dir = new File(database.getFileStorageLocation(), DIR);
		File configDir = new File(dir, configId);
		if (!configDir.exists()) {
			configDir.mkdirs();
		}
		try (FileWriter writer = new FileWriter(new File(configDir, NEW_CONFIG_FILE))) {
			new Gson().toJson(config, writer);
		} catch (Exception e) {
			log.error("Error saving config json file", e);
		}
	}

	private static void moveIndex(IDatabase database, String repositoryId, String configId) {
		File indexDir = new File(database.getFileStorageLocation(), DIR);
		File repositoryIndexDir = new File(indexDir, repositoryId);
		if (!repositoryIndexDir.exists())
			return;
		repositoryIndexDir.renameTo(new File(indexDir, configId));
		repositoryIndexDir.getParentFile().delete();
	}

}
