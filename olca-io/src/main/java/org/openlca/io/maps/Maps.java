package org.openlca.io.maps;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * A helper class for using import / export maps. We (currently) store the
 * mappings in CSV files attached as resources. See the mapping specification
 * for the details (REF_DATA.md).
 */
public class Maps {

	private Maps() {
	}

	public static List<List<Object>> readAll(InputStream stream,
			CellProcessor... cellProcessors) throws Exception {
		try (CsvListReader reader = createReader(stream)) {
			List<List<Object>> results = new ArrayList<>();
			List<Object> nextRow = null;
			while ((nextRow = reader.read(cellProcessors)) != null) {
				results.add(nextRow);
			}
			return results;
		}
	}

	public static CsvListReader createReader(InputStream stream)
			throws Exception {
		CsvPreference pref = new CsvPreference.Builder('"', ';', "\n").build();
		// exclude the byte order mark, if there is any
		BOMInputStream bom = new BOMInputStream(stream, false,
				ByteOrderMark.UTF_8);
		InputStreamReader reader = new InputStreamReader(bom, "utf-8");
		BufferedReader buffer = new BufferedReader(reader);
		CsvListReader csvReader = new CsvListReader(buffer, pref);
		return csvReader;
	}

	public static String getString(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return null;
		Object val = values.get(i);
		if (val == null)
			return null;
		else
			return val.toString();
	}

	public static Double getOptionalDouble(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return null;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		else
			return null;
	}

	public static double getDouble(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		else
			return 0;
	}

	public static int getInt(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).intValue();
		else
			return 0;
	}
}
