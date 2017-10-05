package org.openlca.core.matrix.io.olcamat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Csv {

	static String techIndexHeader() {
		String[] header = {
				"index",
				"process ID",
				"process name",
				"process type",
				"process location",
				"process category",
				"process sub-category",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow category",
				"flow sub-category",
				"flow property ID",
				"flow property name",
				"unit ID",
				"unit name" };
		return Csv.toLine(header);
	}

	static String enviIndexHeader() {
		String[] header = {
				"index",
				"flow ID",
				"flow name",
				"flow type",
				"flow location",
				"flow category",
				"flow sub-category",
				"flow property ID",
				"flow property name",
				"unit ID",
				"unit name" };
		return Csv.toLine(header);
	}

	static List<String[]> readFile(File file) throws Exception {
		if (file == null)
			return Collections.emptyList();
		List<String[]> list = new ArrayList<>();
		for (String line : Files.readAllLines(file.toPath())) {
			list.add(readLine(line));
		}
		return list;
	}

	static String[] readLine(String line) throws Exception {
		if (line == null)
			return null;
		boolean inQuotes = false;
		StringBuilder word = null;
		List<String> words = new ArrayList<>();
		char[] chars = line.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == ',') {
				if (inQuotes && word != null) {
					word.append(c);
					continue;
				}
				if (word == null) {
					words.add("");
				} else {
					words.add(word.toString());
					word = null;
				}
				if (i == (chars.length - 1)) {
					words.add("");
				}
				continue;
			}

			if (c == '"') {
				if (word == null) {
					word = new StringBuilder();
					inQuotes = true;
					continue;
				}
				if (word != null && inQuotes) {
					inQuotes = false;
					continue;
				}
				throw new Exception("Syntax exception col=" + i);
			}

			if (word == null)
				word = new StringBuilder();
			word.append(c);
		}
		if (word != null)
			words.add(word.toString());
		return words.toArray(new String[words.size()]);
	}

	static String toLine(String[] entries) {
		if (entries == null)
			return "";
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < entries.length; i++) {
			String e = entries[i];
			boolean last = i == entries.length - 1;
			if (e == null) {
				if (!last)
					b.append(',');
				continue;
			}
			e = e.trim().replace('"', '\'');
			if (e.indexOf(',') >= 0) {
				b.append('"').append(e).append('"');
			} else {
				b.append(e);
			}
			if (!last)
				b.append(',');
		}
		return b.toString();
	}

	static void writeFile(List<String> lines, File file) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fos,
						"utf-8");
				BufferedWriter buffer = new BufferedWriter(writer)) {
			for (String line : lines) {
				buffer.write(line);
				buffer.newLine();
			}
		}
	}
}
