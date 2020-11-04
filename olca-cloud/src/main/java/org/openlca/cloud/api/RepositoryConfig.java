package org.openlca.cloud.api;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlca.cloud.util.Directories;
import org.openlca.core.database.IDatabase;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RepositoryConfig {

	private final static Logger log = LoggerFactory.getLogger(RepositoryConfig.class);
	public static final String DIR = "cloud";
	private static final String PROPERTIES_FILE = "config.json";
	public IDatabase database;
	public final String baseUrl;
	public String repositoryId;
	public CredentialSupplier credentials;
	private final String id;
	private boolean active;
	private String lastCommitId;

	public RepositoryConfig(IDatabase database, String baseUrl, String repositoryId) {
		this(database, UUID.randomUUID().toString(), baseUrl, repositoryId);
	}

	private RepositoryConfig(IDatabase database, String id, String baseUrl, String repositoryId) {
		this.id = id;
		this.database = database;
		this.baseUrl = baseUrl;
		this.repositoryId = repositoryId;
	}

	public static List<RepositoryConfig> loadAll(IDatabase database) {
		File dir = getDir(database);
		if (!dir.exists() || !dir.isDirectory())
			return new ArrayList<>();
		List<RepositoryConfig> configs = new ArrayList<>();
		for (File child : dir.listFiles()) {
			if (!child.isDirectory())
				continue;
			configs.add(load(database, child.getName()));
		}
		return configs;
	}

	public static RepositoryConfig loadActive(IDatabase database) {
		for (RepositoryConfig config : loadAll(database))
			if (config.active)
				return config;
		return null;
	}

	private static RepositoryConfig load(IDatabase database, String id) {
		File configFile = getConfigFile(database, id);
		if (!configFile.exists())
			return null;
		try (FileReader reader = new FileReader(configFile)) {
			JsonObject json = new Gson().fromJson(reader, JsonObject.class);
			String configId = json.get("id").getAsString();
			String baseUrl = json.get("baseUrl").getAsString();
			String repositoryId = json.get("repositoryId").getAsString();
			String username = json.get("username").getAsString();
			String password = json.get("password").getAsString();
			RepositoryConfig config = new RepositoryConfig(database, configId, baseUrl, repositoryId);
			config.credentials = new CredentialSupplier(username, password);
			config.active = json.get("active").getAsBoolean();
			JsonElement lastCommitId = json.get("lastCommitId");
			if (lastCommitId != null && !lastCommitId.isJsonNull()) {
				config.lastCommitId = lastCommitId.getAsString();
			}
			return config;
		} catch (IOException e) {
			log.error("Error loading repository config", e);
			return null;
		}
	}

	private void save() {
		File configFile = getConfigFile(database, id);
		if (configFile.exists()) {
			configFile.delete();
		} else {
			configFile.getParentFile().mkdirs();
		}
		JsonObject config = new JsonObject();
		config.addProperty("id", id);
		config.addProperty("baseUrl", baseUrl);
		config.addProperty("repositoryId", repositoryId);
		config.addProperty("username", credentials.username);
		config.addProperty("password", credentials.password);
		config.addProperty("lastCommitId", lastCommitId);
		config.addProperty("active", active);
		try (FileWriter writer = new FileWriter(configFile)) {
			new Gson().toJson(config, writer);
		} catch (IOException e) {
			log.error("Error saving repository properties", e);
		}
	}

	public static RepositoryConfig add(IDatabase database, String baseUrl, String repoId,
			CredentialSupplier credentials) {
		RepositoryConfig config = new RepositoryConfig(database, baseUrl, repoId);
		config.credentials = credentials;
		config.save();
		return config;
	}

	public void activate() {
		List<RepositoryConfig> configs = loadAll(database);
		for (RepositoryConfig config : configs) {
			if (!config.id.equals(id) && config.active) {
				config.active = false;
				config.save();
			}
		}
		active = true;
		save();
	}

	public void deactivate() {
		active = false;
		save();
	}
	
	public void remove() {
		Directories.delete(getConfigDir(database, id));
	}

	private static File getConfigFile(IDatabase database, String id) {
		return new File(getConfigDir(database, id), PROPERTIES_FILE);
	}

	private static File getConfigDir(IDatabase database, String id) {
		return new File(getDir(database), id);
	}

	private static File getDir(IDatabase database) {
		return new File(database.getFileStorageLocation(), DIR);
	}

	public File getConfigDir() {
		return getConfigDir(database, id);
	}

	public String getServerUrl() {
		int slashIndex = -1;
		if (baseUrl.contains("://")) {
			slashIndex = baseUrl.indexOf('/', baseUrl.indexOf("://") + 3);
		} else {
			slashIndex = baseUrl.indexOf('/');
		}
		if (slashIndex == -1)
			return baseUrl;
		return baseUrl.substring(0, slashIndex);
	}

	void setLastCommitId(String lastCommitId) {
		this.lastCommitId = lastCommitId;
		save();
	}

	public String getLastCommitId() {
		return lastCommitId;
	}
	
	public boolean isActive() {
		return active;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof RepositoryConfig))
			return false;
		RepositoryConfig config = (RepositoryConfig) obj;
		return Strings.nullOrEqual(id, config.id);
	}

}
