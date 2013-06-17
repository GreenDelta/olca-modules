package org.openlca.core.database.mysql;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openlca.core.database.DataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MySQL database import.
 */
public class MySQLDatabaseImport {

	private ZipFile archive;
	private MySQLDatabase newDatabase;
	private Logger log = LoggerFactory.getLogger(getClass());

	public MySQLDatabaseImport(MySQLDatabase newDatabase, File file) {
		this.newDatabase = newDatabase;
		try {
			this.archive = new ZipFile(file);
		} catch (Exception e) {
			throw new IllegalArgumentException(file + " is not a ZIP archive");
		}
	}

	public void run() throws DataProviderException {
		// TODO: we can skip this when we move to derby for client database
		throw new RuntimeException("not yet implemented");

		// try {
		// String encoding = getEncoding();
		// log.trace("Import database, use encoding {}", encoding);
		// runScripts(encoding);
		// // TODO: run updates Updates.checkAndRun(data);
		// return db;
		// } catch (Exception e) {
		// throw new DataProviderException(e);
		// }
	}

	private String getEncoding() {
		ZipEntry entry = archive.getEntry("encoding");
		if (entry == null)
			return Charset.defaultCharset().name();
		try (InputStream is = archive.getInputStream(entry)) {
			byte[] bytes = new byte[512];
			int length = is.read(bytes);
			return new String(bytes, 0, length, "UTF-8");
		} catch (Exception e) {
			log.error("Failed to read encoding");
			return "UTF-8";
		}
	}
	//
	// private void runScripts(String encoding)
	// throws Exception, IOException {
	// log.trace("Run scripts");
	// ZipEntry schemaEntry = archive.getEntry("schema.sql");
	// if (schemaEntry == null) {
	// log.trace("no schema entry found, using arche-schema");
	// db.execute("arche_schema.sql");
	// } else {
	// log.trace("execute schema file");
	// db.execute(archive.getInputStream(schemaEntry), encoding);
	// }
	// ZipEntry dataEntry = archive.getEntry("script.sql");
	// if (dataEntry != null) {
	// log.trace("execute data file");
	// db.execute(archive.getInputStream(dataEntry), encoding);
	// } else {
	// log.trace("no data file 'script.sql' found");
	// }
	// }
}
