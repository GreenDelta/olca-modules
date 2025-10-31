package org.openlca.core.database.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A list of database configurations that can be de/serialized to a JSON file.
 * In openLCA this is the {@code ~/[workspace]/databases.json} file.
 */
public class DatabaseConfigList {

	private final List<DerbyConfig> localDatabases = new ArrayList<>();
	private final List<MySqlConfig> remoteDatabases = new ArrayList<>();

	public List<DerbyConfig> getDerbyConfigs() {
		return localDatabases;
	}

	public List<MySqlConfig> getMySqlConfigs() {
		return remoteDatabases;
	}

	public List<DatabaseConfig> getAll() {
		var list = new ArrayList<DatabaseConfig>(
				localDatabases.size() + remoteDatabases.size());
		list.addAll(localDatabases);
		list.addAll(remoteDatabases);
		return list;
	}

	public static DatabaseConfigList read(File file) {
		try (var in = new FileInputStream(file);
				 var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			var gson = new Gson();
			return gson.fromJson(reader, DatabaseConfigList.class);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(DatabaseConfigList.class);
			log.error("failed to read database configurations from " + file, e);
			return new DatabaseConfigList();
		}
	}

	public void write(File file) {
		try (var out = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
			var gson = new GsonBuilder().setPrettyPrinting().create();
			var s = gson.toJson(this);
			writer.write(s);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(DatabaseConfigList.class);
			log.error("failed to write database configuration to " + file, e);
		}
	}

	public boolean contains(DerbyConfig config) {
		return localDatabases.contains(config);
	}

	public boolean contains(MySqlConfig config) {
		return remoteDatabases.contains(config);
	}

	/**
	 * Returns true if a database with the given name exists.
	 */
	public boolean nameExists(String name) {
		if (Strings.isBlank(name))
			return false;
		var newName = name.trim().toLowerCase();
		Predicate<DatabaseConfig> sameName = config -> {
			if (config == null || config.name() == null)
				return false;
			return Objects.equals(config.name().toLowerCase(), newName);
		};
		for (var config : localDatabases) {
			if (sameName.test(config))
				return true;
		}
		for (var config : remoteDatabases) {
			if (sameName.test(config))
				return true;
		}
		return false;
	}

}
