package org.openlca.core.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.internal.Resource;
import org.openlca.core.database.internal.ScriptRunner;
import org.openlca.util.Dirs;
import org.openlca.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

public class Derby extends Notifiable implements IDatabase {

	private static final AtomicInteger memInstances = new AtomicInteger(0);

	private final Logger log = LoggerFactory.getLogger(getClass());
	private EntityManagerFactory entityFactory;

	private final String name;
	private File folder;
	private File fileStorageLocation;
	private String url;

	private boolean closed = false;
	private HikariDataSource connectionPool;

	public static Derby createInMemory() {
		int i = memInstances.incrementAndGet();
		Derby db = new Derby("olca_mem_db" + i);
		db.url = "jdbc:derby:memory:" + db.name;
		db.createNew(db.url + ";create=true");
		db.connect();
		return db;
	}

	/**
	 * Restores an in-memory database from a backup folder.
	 */
	public static Derby restoreInMemory(String folder) {
		var path = searchDump(folder);
		if (path == null) {
			var log = LoggerFactory.getLogger(Derby.class);
			log.error("Could not find a database dump under {};"
				+ " will create an empty DB", folder);
			return createInMemory();
		}
		int i = memInstances.incrementAndGet();
		var db = new Derby("olca_mem_db" + i);
		var url = "jdbc:derby:memory:" + db.name + ";restoreFrom=" + path;
		try {
			DriverManager.getConnection(url).close();
		} catch (SQLException e) {
			Exceptions.unchecked("failed to restore in-memory", e);
		}
		db.url = "jdbc:derby:memory:" + db.name;
		db.connect();
		return db;
	}

	private static String searchDump(String path) {
		if (path == null)
			return null;
		var root = new File(path);
		if (!root.exists() || !root.isDirectory())
			return null;
		var dirs = new ArrayDeque<File>();
		dirs.add(root);
		while (!dirs.isEmpty()) {
			var dir = dirs.poll();
			if (isDerbyFolder(dir)) {
				var dbPath = dir.getAbsolutePath();
				return dbPath.replace('\\', '/');
			}
			var next = dir.listFiles();
			if (next == null)
				continue;
			for (var f : next) {
				if (f.isDirectory()) {
					dirs.add(f);
				}
			}
		}
		return null;
	}

	private Derby(String name) {
		this.name = name;
	}

	public Derby(File folder) {
		this.folder = folder;
		this.name = folder.getName();
		boolean create = !isDerbyFolder(folder);
		if (create) {
			Dirs.delete(folder.toPath());
		}
		log.info("initialize database folder {}, create={}", folder, create);
		url = "jdbc:derby:" + folder.getAbsolutePath().replace('\\', '/');
		log.trace("database url: {}", url);
		if (create) {
			createNew(url + ";create=true");
		}
		connect();
	}

	/**
	 * Returns true if the given folder is (most likely) a Derby database by
	 * checking if Derby specific files and folders are present; see the Derby
	 * folder specification:
	 * http://db.apache.org/derby/docs/10.0/manuals/develop/develop13.html
	 */
	public static boolean isDerbyFolder(File folder) {
		if (folder == null || !folder.exists() || !folder.isDirectory())
			return false;
		File log = new File(folder, "log");
		if (!log.exists() || !log.isDirectory())
			return false;
		File seg0 = new File(folder, "seg0");
		if (!seg0.exists() || !seg0.isDirectory())
			return false;
		File props = new File(folder, "service.properties");
		return props.exists() && props.isFile();
	}

	private void createNew(String url) {
		log.info("create new database {}", url);
		try {
			DriverManager.getConnection(url).close();
			new ScriptRunner(this).run(
				Resource.CURRENT_SCHEMA_DERBY.getStream(), "utf-8");
		} catch (Exception e) {
			log.error("failed to create database", e);
			throw new DatabaseException("Failed to create database", e);
		}
	}

	/**
	 * Returns the Derby database directory (see
	 * http://db.apache.org/derby/docs/10.0/manuals/develop/develop13.html). The
	 * name of the directory is equal to the database name.
	 */
	public File getDatabaseDirectory() {
		return folder;
	}

	/**
	 * Returns the folder '_olca_' within the database directory. If this folder
	 * does not exist is created when this method is called.
	 */
	@Override
	public File getFileStorageLocation() {
		if (fileStorageLocation != null)
			return fileStorageLocation;
		fileStorageLocation = new File(folder, "_olca_");
		if (!fileStorageLocation.exists()) {
			try {
				Files.createDirectories(fileStorageLocation.toPath());
			} catch (IOException e) {
				Exceptions.unchecked("failed to create file storage folder", e);
			}
		}
		return fileStorageLocation;
	}

	/**
	 * Set the location where files of data that are not stored directly in the
	 * database should be saved (e.g. external files of sources).
	 * <p>
	 * Typically, this is only set by in-memory databases as for file based
	 * databases it defaults to the `_olca_` folder within the database
	 * directory.
	 */
	public void setFileStorageLocation(File fileStorageLocation) {
		this.fileStorageLocation = fileStorageLocation;
	}

	private void connect() {
		log.trace("connect to database: {}", url);
		Map<Object, Object> map = new HashMap<>();
		map.put("jakarta.persistence.jdbc.url", url);
		map.put("jakarta.persistence.jdbc.driver",
			"org.apache.derby.iapi.jdbc.AutoloadedDriver");
		map.put("eclipselink.classloader", getClass().getClassLoader());
		map.put("eclipselink.target-database", "Derby");
		log.trace("Create entity factory");
		entityFactory = new PersistenceProvider()
			.createEntityManagerFactory("openLCA", map);
		log.trace("Init connection pool");
		connectionPool = new HikariDataSource();
		connectionPool.setDriverClassName(
			"org.apache.derby.iapi.jdbc.AutoloadedDriver");
		connectionPool.setJdbcUrl(url);
		connectionPool.setAutoCommit(false);
	}

	@Override
	public void close() {
		if (closed)
			return;
		log.trace("close database: {}", url);
		if (entityFactory != null && entityFactory.isOpen())
			entityFactory.close();
		if (connectionPool != null)
			connectionPool.close();
		try {
			boolean isMemDB = folder == null;
			if (isMemDB) {
				DriverManager.getConnection(url + ";drop=true");
			} else {
				DriverManager.getConnection(url + ";shutdown=true");
			}
		} catch (SQLException e) {
			// a normal shutdown of derby throws an SQL exception
			// with error code 50000 (for single database shutdown
			// 45000), otherwise an error occurred
			if (e.getErrorCode() != 45000 && e.getErrorCode() != 50000)
				log.error(e.getMessage(), e);
			else {
				closed = true;
				log.info("database closed");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			// unload embedded drivers etc.
			// http://db.apache.org/derby/docs/10.4/devguide/rdevcsecure26537.html
			System.gc();
		}
	}

	@Override
	public Connection createConnection() {
		log.trace("create connection: {}", url);
		try {
			if (connectionPool != null) {
				var con = connectionPool.getConnection();
				con.setAutoCommit(false);
				return con;
			} else {
				log.warn("no connection pool set up for {}", url);
				return DriverManager.getConnection(url);
			}
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}

	@Override
	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getVersion() {
		return DbUtils.getVersion(this);
	}

	public void compress() {
		var querySql = """
			SELECT SCHEMANAME, TABLENAME FROM sys.sysschemas s, sys.systables t
				WHERE s.schemaid = t.schemaid and t.tabletype = 'T'
			""";
		var callSql = """
			CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, ?)
			""";
		try (var con = createConnection();
				 var stmt = con.createStatement();
				 var tables = stmt.executeQuery(querySql);
				 var call = con.prepareCall(callSql)) {
			while (tables.next()) {
				var schema = tables.getString(1);
				var name = tables.getString(2);
				log.info("Compress table {}.{}", schema, name);
				call.setString(1, schema);
				call.setString(2, name);
				call.setInt(3, 1);
				call.execute();
			}
		} catch (Exception e) {
			throw new RuntimeException("Compression failed", e);
		}
	}

	/**
	 * Closes the database and deletes the underlying folder.
	 */
	public void delete() throws Exception {
		if (!closed)
			close();
		if (folder != null) {
			Dirs.delete(folder.toPath());
		}
	}

	/**
	 * Creates a backup of the database in the given folder. This is
	 * specifically useful for creating a dump of an in-memory database. See
	 * https://db.apache.org/derby/docs/10.0/manuals/admin/hubprnt43.html
	 * <p>
	 * Note that the content of the folder will be overwritten if it already
	 * exists.
	 */
	public void dump(String path) {
		try {
			File dir = new File(path);
			if (dir.exists()) {
				Dirs.delete(dir.toPath());
			}
			try {
				Files.createDirectories(dir.toPath());
			} catch (IOException e) {
				Exceptions.unchecked("failed to create folder " + dir, e);
			}
			var command = "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)";
			try (Connection con = createConnection();
					 CallableStatement cs = con.prepareCall(command)) {
				cs.setString(1, path.replace('\\', '/'));
				cs.execute();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Extracts the given database backup file into the given folder.
	 *
	 * @param dbDir  The database folder where the backup should be extracted to.
	 * @param backup The backup file of the database (a {@code *.zolca} file).
	 */
	public static void unzip(File dbDir, File backup) {
		try (var zip = new ZipFile(backup)) {
			if (!dbDir.exists()) {
				Files.createDirectories(dbDir.toPath());
			}
			var entries = zip.entries();
			while (entries.hasMoreElements()) {
				var entry = entries.nextElement();
				var target = new File(dbDir, entry.getName());
				if (entry.isDirectory()) {
					if (!target.exists()) {
						Files.createDirectories(target.toPath());
					}
					continue;
				}
				var parent = target.getParentFile();
				if (!parent.exists()) {
					Files.createDirectories(parent.toPath());
				}
				try (var stream = zip.getInputStream(entry)) {
					Files.copy(stream, target.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to restore database from '"
				+ backup + "' in '" + dbDir + "'", e);
		}
	}

	/**
	 * Creates a backup file for the given database folder.
	 *
	 * @param dbDir  The database folder from which the backup should be created.
	 * @param backup The backup file for the database (a {@code *.zolca} file).
	 */
	public static void zip(File dbDir, File backup) {

		var root = dbDir.toPath();

		// a function that translates files to zip entry names
		Function<Path, String> entryName = path -> {
			var relative = root.relativize(path);
			var entry = new StringBuilder();
			for (int i = 0; i < relative.getNameCount(); i++) {
				if (i > 0) {
					entry.append('/');
				}
				entry.append(relative.getName(i));
			}
			return entry.toString();
		};

		try (var out = new FileOutputStream(backup);
				 var zip = new ZipOutputStream(out)) {

			// a function that adds a file to the zip
			Consumer<File> add = file -> {
				try {
					var path = file.toPath();
					var entry = entryName.apply(path);
					zip.putNextEntry(new ZipEntry(entry));
					Files.copy(path, zip);
					zip.closeEntry();
				} catch (IOException e) {
					throw new RuntimeException(
						"Failed to write zip entry: " + file, e);
				}
			};

			// traversing the database directory and add the
			// files to the zip
			var queue = new ArrayDeque<File>();
			queue.add(dbDir);
			while (!queue.isEmpty()) {
				var nextDir = queue.poll();
				var files = nextDir.listFiles();
				if (files == null)
					continue;
				for (var file : files) {
					if (file.isDirectory()) {
						queue.add(file);
					} else {
						add.accept(file);
					}
				}
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to export database '"
				+ dbDir + "' to '" + backup + "'", e);
		}
	}

}
