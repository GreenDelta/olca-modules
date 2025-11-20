package org.openlca.io.hestia;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.openlca.commons.Res;

/// A glossary file is a CSV file that contains key-value pairs of Hestia terms
/// of a term type mapped to columns.
public class GlossaryFile {

	private final FieldIndex fields;
	private final List<Entry> entries;

	private GlossaryFile(FieldIndex fields, List<Entry> entries) {
		this.fields = fields;
		this.entries = entries;
	}

	public static Res<GlossaryFile> readFrom(File file) {
		try (var reader = new FileReader(file);
				 var csv = CSVFormat.RFC4180.parse(reader)) {
			boolean first = true;
			FieldIndex fields = null;
			var entries = new ArrayList<Entry>();
			for (var row : csv) {
				if (first) {
					fields = new FieldIndex(row.values());
					first = false;
					continue;
				}
				entries.add(new Entry(fields, row.values()));
			}
			return Res.ok(new GlossaryFile(fields, entries));
		} catch (Exception e) {
			return Res.error("Failed to read glossary file: " + file, e);
		}
	}

	public int size() {
		return entries.size();
	}

	public List<String> fields() {
		return Arrays.asList(fields.fields);
	}

	public List<Entry> entries() {
		return entries;
	}

	public static class Entry {

		private final FieldIndex fields;
		private final String[] values;

		private Entry(FieldIndex fields, String[] values) {
			this.fields = fields;
			this.values = values;
		}

		public String getId() {
			return getField("term.id");
		}

		public String getName() {
			return getField("term.name");
		}

		public String getEcoinventMapping() {
			return getField("ecoinventMapping");
		}

		public String getField(String field) {
			int i = fields.get(field);
			return i < 0 || i > values.length
				? null
				: values[i];
		}
	}

	private record FieldIndex(String[] fields) {

		int get(String field) {
			if (field == null) {
				return -1;
			}
			for (int i = 0; i < fields.length; i++) {
				if (field.equals(fields[i])) {
					return i;
				}
			}
			return -1;
		}
	}
}
