package org.openlca.io.maps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.openlca.util.BinUtils;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * A helper class for using import / export maps. We store the mappings in CSV
 * files of a defined format (see the mapping specification for the details:
 * REF_DATA.md). A default file for each mapping should be always directly
 * attached as jar-resource in the package org.openlca.io.maps. Additionally, a
 * mapping file can be stored in the database (so that it can be updated without
 * updating the io-package).
 */
public class Maps {

	public static final String ES2_FLOW_IMPORT = "ecospold_2_flow_map.csv";

	/**
	 * Import map for flows from SimaPro CSV.
	 */
	public static final String SP_FLOW_IMPORT = "sp_flow_import_map.csv";

	/**
	 * Export map for units to EcoSpold 02.
	 */
	public static final String ES2_UNIT_EXPORT = "es2_unit_export_map.csv";

	/**
	 * Export map for locations to EcoSpold 2.
	 */
	public static final String ES2_LOCATION_EXPORT = "es2_location_export_map.csv";

	/**
	 * Export map for categories/compartments to EcoSpold 2.
	 */
	public static final String ES2_COMPARTMENT_EXPORT = "es2_compartment_export_map.csv";

	/**
	 * A mapping file that maps location codes to UUIDs of locations.
	 */
	public static final String LOCATION_IMPORT = "location_import.csv";

	private Maps() {
	}

	/**
	 * Reads all mappings from the given file using the given cell processors.
	 * It first tries to load the file from the database. If it does not exist
	 * it loads the mappings from the jar-internal resource file.
	 */
	public static List<List<Object>> readAll(String fileName,
			IDatabase database, CellProcessor... cellProcessors)
			throws Exception {
		try (CsvListReader reader = open(fileName, database)) {
			List<List<Object>> results = new ArrayList<>();
			List<Object> nextRow;
			while ((nextRow = reader.read(cellProcessors)) != null) {
				results.add(nextRow);
			}
			return results;
		}
	}

	/**
	 * Opens a CSV reader for the given. It first tries to load the file from
	 * the database. If it does not exist it loads the mapping from the
	 * jar-internal resource file.
	 */
	private static CsvListReader open(String fileName, IDatabase database) {
		CsvListReader reader = fromDatabase(fileName, database);
		return reader != null
				? reader
				: createReader(Maps.class.getResourceAsStream(fileName));
	}

	private static CsvListReader fromDatabase(String fileName, IDatabase db) {
		if (db == null)
			return null;
		MappingFileDao dao = new MappingFileDao(db);
		MappingFile file = dao.getForName(fileName);
		if (file == null || file.content == null)
			return null;
		byte[] bytes = BinUtils.gunzip(file.content);
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		return createReader(stream);
	}

	private static CsvListReader createReader(InputStream stream) {
		var pref = new CsvPreference.Builder('"', ';', "\n").build();
		// exclude the byte order mark, if there is any
		var bom = new BOMInputStream(stream, false, ByteOrderMark.UTF_8);
		var reader = new InputStreamReader(bom, StandardCharsets.UTF_8);
		var buffer = new BufferedReader(reader);
		return new CsvListReader(buffer, pref);
	}

	/**
	 * Stores the given mapping file in the database. If there is already a
	 * mapping file with the given name in the database, this file will be
	 * updated by this method. The given must be the raw CSV stream. The content
	 * of this stream will be compressed before storing it in the database.
	 */
	public static void store(String name, InputStream stream, IDatabase db) {
		try {
			var dao = new MappingFileDao(db);
			var oldFile = dao.getForName(name);
			if (oldFile != null) {
				dao.delete(oldFile);
			}
			byte[] bytes = IOUtils.toByteArray(stream);
			var file = new MappingFile();
			file.content = BinUtils.gzip(bytes);
			file.name = name;
			dao.insert(file);
		} catch (Exception e) {
			throw new RuntimeException("Failed to save mapping file " + name, e);
		}
	}

	public static String getString(List<?> values, int i) {
		if (values == null || i >= values.size())
			return null;
		var val = values.get(i);
		return val == null
				? null
				: val.toString();
	}

	public static Double getOptionalDouble(List<?> values, int i) {
		if (values == null || i >= values.size())
			return null;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val instanceof String) {
			if (Strings.nullOrEmpty((String) val))
				return null;
			try {
				return Double.parseDouble((String) val);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(Maps.class);
				log.error("{} is not a number; default to null", val);
				return null;
			}
		}
		return null;
	}

	public static double getDouble(List<?> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val instanceof String) {
			try {
				return Double.parseDouble((String) val);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(Maps.class);
				log.error("{} is not a number; default to 0.0", val);
				return 0;
			}
		}
		return 0;
	}

	public static int getInt(List<?> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).intValue();
		if (val instanceof String) {
			try {
				return Integer.parseInt((String) val);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(Maps.class);
				log.error("{} is not a number; default to 0", val);
				return 0;
			}
		}
		return 0;
	}

	/**
	 * Iterates over each row in the given mapping file.
	 */
	public static void each(File file, Consumer<List<String>> fn) {
		if (file == null || fn == null)
			return;
		try (var stream = new FileInputStream(file)){
		 	each(stream, fn);
		} catch (IOException e) {
			throw new RuntimeException("failed to read mapping file " + file, e);
		}
	}

	/**
	 * Iterates over each row in the given mapping file.
	 */
	public static void each(InputStream stream, Consumer<List<String>> fn) {
		if (stream == null || fn == null)
			return;
		var prefs = new CsvPreference.Builder('"', ';', "\n").build();
		try (var bom = new BOMInputStream(stream, false, ByteOrderMark.UTF_8);
			 var r = new InputStreamReader(bom, StandardCharsets.UTF_8);
			 var buf = new BufferedReader(r);
			 var reader = new CsvListReader(buf, prefs)) {
			List<String> row;
			while ((row = reader.read()) != null) {
				if (row.isEmpty())
					continue;
				fn.accept(row);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the given rows into the CSV mapping format and writes this to
	 * the given file.
	 */
	public static void write(File file, Stream<Object[]> rows) {
		if (file == null || rows == null)
			return;
		try (var out = new FileOutputStream(file)) {
			write(out, rows);
		} catch (IOException e) {
			throw new RuntimeException("failed to write mapping file " + file, e);
		}
	}

	/**
	 * Converts the given rows into the CSV mapping format and writes this to
	 * a byte array.
	 */
	public static byte[] write(Stream<Object[]> rows) {
		if (rows == null)
			return new byte[0];
		var bout = new ByteArrayOutputStream();
		write(bout, rows);
		return bout.toByteArray();
	}

	/**
	 * Converts the given rows into the CSV mapping format and writes this to
	 * the given output stream.
	 */
	public static void write(OutputStream out, Stream<Object[]> rows) {
		if (out == null || rows == null)
			return;
		var prefs = new CsvPreference.Builder('"', ';', "\n").build();
		try (var w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
			 var buf = new BufferedWriter(w);
			 var writer = new CsvListWriter(buf, prefs)) {
			rows.forEach(row -> {
				if (row == null)
					return;
				try {
					writer.write(row);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
