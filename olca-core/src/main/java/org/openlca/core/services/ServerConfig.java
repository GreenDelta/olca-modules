package org.openlca.core.services;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.matrix.solvers.mkl.MKL;
import org.openlca.nativelib.NativeLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General configuration of a service interface. A server configuration is
 * typically parsed from the command line arguments of a server application
 * when the server starts. The following parameters are interpreted by the
 * parser, they are all optional and have a default value where possible:
 *
 * <pre>
 * {@code
 *
 *  -data <path to data folder>
 *  The path to the data folder that contains the database and possible
 *  libraries. The folder structure need to follow the openLCA workspace
 *  structure, means the sub-folder `databases` of that folder contains the
 *  database and the sub-folder `libraries` possible data libraries to which
 *  the database is linked. If this parameter is not provided, the default
 *  openLCA workspace (currently `~/openLCA-data-1.4`) is taken as data folder.
 *
 *  -db <database>
 *  The name of the database in the data folder (only the name, not a full
 *  file path, must be provided); defaults to 'database'.
 *
 *  -port <port>
 *  The port of the server; defaults to 8080.
 *
 *  -native <path to native library folder>
 *  The path to the folder from which the native libraries should be
 *  loaded; defaults to the data folder.
 *
 * 	-threads <number of calculation threads>
 * 	The number of parallel threads that can be used for calculations. Make sure
 * 	that the server has enough resources if you provide a larger number than 1
 * 	here; defaults to 1.
 *
 * 	-timeout <minutes after which results are disposed>
 * 	The time in minutes after which results are cleaned up if they were not
 * 	disposed by the user. A value of <=0 means that no timeout should be
 * 	applied; defaults to 0.
 *
 *  --readonly <true | false>?
 *  If this flag is set, the server will run in readonly mode and modifying the
 *  database will not be possible.
 *
 *  -static <path to folder with static files>
 *  A path to a folder with static files that should be hosted by the server.
 *  This only has an effect if the server supports hosting of static files.
 *
 * }
 * </pre>
 *
 * When parsing configuration options, everything that starts with a dash, -,
 * followed by a value is stored as a configuration pair in the {@code args}
 * map. Arguments that start with two dashes, --, that are not followed by a
 * value, are stored as {@code (--<flag>, "true")} pairs by default.
 *
 * @param dataDir    the data folder that contains the database and possible
 *                   data libraries.
 * @param db         the database that is used by the service.
 * @param port       the port of the server
 * @param isReadonly indicates if the server runs in read-only mode;
 *                   modification of the database is not supported in this case.
 * @param staticDir  an optional folder from which static files are hosted by
 *                   the server, if this is supported; {@code null} by default}
 * @param args       all configuration arguments as key-value pairs
 */
public record ServerConfig(
		DataDir dataDir,
		IDatabase db,
		int port,
		boolean isReadonly,
		File staticDir,
		int threadCount,
		int timeout,
		Map<String, String> args
) {

	public static Builder defaultOf(IDatabase db) {
		return new Builder(db);
	}

	public static ServerConfig parse(String[] args) {
		var parser = new Parser(args);
		return parser.parseArgs();
	}

	private static class Parser {

		private final Logger log = LoggerFactory.getLogger(getClass());
		private final Map<String, String> args;
		private final DataDir dataDir;

		private Parser(String[] args) {
			this.args = mapArgs(args);
			var path = this.args.get("-data");
			if (path == null) {
				dataDir = DataDir.get();
				log.info("no data folder provided; default to {}", dataDir.root());
			} else {
				dataDir = DataDir.get(new File(path));
				log.info("use data folder: {}", dataDir.root());
			}
		}

		ServerConfig parseArgs() {
			var db = openDatabase();
			var staticDir = checkStaticDir();
			loadNativeLibs();
			boolean readonly = "true".equalsIgnoreCase(args.get("--readonly"));
			return new ServerConfig(
					dataDir,
					db,
					getPort(),
					readonly,
					staticDir,
					getThreadCount(),
					getTimeout(),
					args);
		}

		private IDatabase openDatabase() {
			var name = args.get("-db");
			if (name == null) {
				name = args.get("-database");
			}
			if (name == null) {
				log.info("no database provided; took 'database' as default");
				name = "database";
			}
			log.info("use database {}", name);
			var db = dataDir.openDatabase(name);
			var dbVersion = db.getVersion();
			// check the database version
			if (dbVersion > IDatabase.CURRENT_VERSION) {
				throw new IllegalArgumentException(
						"database is newer than this server can handle");
			}
			if (dbVersion < IDatabase.CURRENT_VERSION) {
				Upgrades.on(db);
			}
			return db;
		}

		private int getPort() {
			var port = intOf("-port", 8080);
			log.info("use {} as server", port);
			return port;
		}

		private int getThreadCount() {
			int count = Math.max(intOf("-threads", 1), 1);
			log.info("use {} calculation threads", count);
			return count;
		}

		private int getTimeout() {
			int timeout =  Math.max(intOf("-timeout", 0), 0);
			log.info("set {} as result timeout", timeout);
			return timeout;
		}

		private int intOf(String flag, int defaultVal) {
			var str = args.get(flag);
			if (str == null)
				return defaultVal;
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				log.error("invalid value for {}: " +
						"{} is not an integer, took default of {}",
						flag, str, defaultVal);
				return defaultVal;
			}
		}

		private File checkStaticDir() {
			var path = args.get("-static");
			if (path == null)
				return null;
			var dir = new File(path);
			if (dir.exists() && dir.isDirectory()) {
				log.info("serve static files from: {}", path);
				return dir;
			}
			log.error("static folder '{}' is not a directory; skipped", path);
			return null;
		}

		private void loadNativeLibs() {
			var nativePath = args.get("-native");
			var nativeDir = nativePath != null
					? new File(nativePath)
					: dataDir.root();
			log.info("try to load native libraries from: {}", nativeDir);
			if (MKL.isLibraryDir(nativeDir) && MKL.loadFrom(nativeDir)) {
				log.info("loaded MKL libraries");
				return;
			}
			NativeLib.loadFrom(nativeDir);
			if (!NativeLib.isLoaded()) {
				log.warn("no native libraries could be loaded;" +
						" calculations could be very slow");
			}
		}

		private static Map<String, String> mapArgs(String[] args) {
			if (args == null)
				return Collections.emptyMap();
			var map = new HashMap<String, String>();
			String flag = null;
			for (var arg : args) {
				if (arg.startsWith("-")) {
					flag = arg;
					if (arg.startsWith("--")) {
						// for --flags we set the default value to true, but this can
						// be overwritten by the argument
						map.put(arg, "true");
					}
					continue;
				}
				if (flag != null) {
					map.put(flag, arg);
				}
				flag = null;
			}
			return map;
		}
	}

	public static class Builder {

		private final IDatabase db;

		private DataDir dataDir;
		private int port = 8080;
		private boolean isReadonly = false;
		private File staticDir;
		private int threadCount = 1;
		private int timeout = 0;
		private Map<String, String> args;

		private Builder(IDatabase db) {
			this.db = Objects.requireNonNull(db);
		}

		public Builder withDataDir(DataDir dataDir) {
			this.dataDir = dataDir;
			return this;
		}

		public Builder withPort(int port) {
			this.port = port;
			return this;
		}

		public Builder withReadOnly(boolean isReadonly) {
			this.isReadonly = isReadonly;
			return this;
		}

		public Builder withStaticDir(File staticDir) {
			this.staticDir = staticDir;
			return this;
		}

		public Builder withThreadCount(int threads) {
			if (threads >= 1) {
				this.threadCount = threads;
			}
			return this;
		}

		public Builder withTimeout(int timeout) {
			if (timeout >= 0) {
				this.timeout = timeout;
			}
			return this;
		}

		public Builder withArgs(Map<String, String> args) {
			this.args = args;
			return this;
		}

		public ServerConfig get() {
			var dataDir = this.dataDir == null
					? DataDir.get()
					: this.dataDir;
			if (!NativeLib.isLoaded()) {
				NativeLib.loadFrom(dataDir.root());
			}
			Map<String, String> args = this.args == null
					? java.util.Map.of()
					: this.args;
			return new ServerConfig(
					dataDir, db, port, isReadonly, staticDir, threadCount, timeout, args);
		}
	}
}
