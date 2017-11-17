package org.openlca.cloud.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.openlca.cloud.util.Directories;
import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryConfig {

	private final static Logger log = LoggerFactory.getLogger(RepositoryConfig.class);
	public final IDatabase database;
	public final String baseUrl;
	public final String repositoryId;
	public final CredentialSupplier credentials;
	private String lastCommitId;

	public RepositoryConfig(IDatabase database, String baseUrl, String repositoryId) {
		this(database, baseUrl, repositoryId, null);
	}

	public RepositoryConfig(IDatabase database, String baseUrl, String repositoryId, CredentialSupplier credentials) {
		this.database = database;
		this.baseUrl = baseUrl;
		this.repositoryId = repositoryId;
		this.credentials = credentials;
	}

	public static RepositoryConfig loadFor(IDatabase database) {
		File configFile = getConfigFile(database);
		if (!configFile.exists())
			return null;
		Properties properties = new Properties();
		try (FileInputStream stream = new FileInputStream(configFile)) {
			properties.load(stream);
			String baseUrl = properties.getProperty("baseUrl");
			String repositoryId = properties.getProperty("repositoryId");
			String lastCommitId = properties.getProperty("lastCommitId");
			String username = properties.getProperty("username");
			String password = properties.getProperty("password");
			if ("null".equals(lastCommitId))
				lastCommitId = null;
			CredentialSupplier credentials = new CredentialSupplier(username, password);
			RepositoryConfig config = new RepositoryConfig(database, baseUrl, repositoryId, credentials);
			config.lastCommitId = lastCommitId;
			return config;
		} catch (IOException e) {
			log.error("Error loading repository properties", e);
			return null;
		}
	}

	public void save() {
		File configFile = getConfigFile(database);
		if (configFile.exists())
			configFile.delete();
		try (FileOutputStream stream = new FileOutputStream(configFile)) {
			configFile.createNewFile();
			Properties properties = new Properties();
			properties.setProperty("baseUrl", baseUrl);
			properties.setProperty("repositoryId", repositoryId);
			String lastCommitId = this.lastCommitId != null ? this.lastCommitId : "null";
			properties.setProperty("lastCommitId", lastCommitId);
			properties.setProperty("username", credentials.username);
			// TODO encrypt
			properties.setProperty("password", credentials.password);
			properties.store(stream, "");
		} catch (IOException e) {
			log.error("Error saving repository properties", e);
		}
	}

	public static RepositoryConfig connect(IDatabase database, String baseUrl, String repositoryId,
			CredentialSupplier credentials) {
		RepositoryConfig config = new RepositoryConfig(database, baseUrl, repositoryId, credentials);
		config.save();
		return config;
	}

	public void disconnect() {
		File configFile = getConfigFile(database);
		configFile.delete();
		File fileStorage = database.getFileStorageLocation();
		Directories.delete(new File(fileStorage, "cloud/" + repositoryId));
	}

	private static File getConfigFile(IDatabase database) {
		return new File(database.getFileStorageLocation(),
				"repository.properties");
	}

	public String getServerUrl() {
		int slashIndex = -1;
		if (baseUrl.contains("://"))
			slashIndex = baseUrl.indexOf('/', baseUrl.indexOf("://") + 3);
		else
			slashIndex = baseUrl.indexOf('/');
		if (slashIndex == -1)
			return baseUrl;
		return baseUrl.substring(0, slashIndex);
	}

	public String getRepositoryOwner() {
		return repositoryId.split("/")[0];
	}

	public String getRepositoryName() {
		return repositoryId.split("/")[1];
	}

	void setLastCommitId(String lastCommitId) {
		this.lastCommitId = lastCommitId;
		save();
	}

	public String getLastCommitId() {
		return lastCommitId;
	}

}
