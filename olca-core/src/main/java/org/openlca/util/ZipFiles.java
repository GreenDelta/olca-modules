package org.openlca.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipFiles {

	/**
	 * Opens the given file as zip file. It tries different character sets to
	 * decode entry names and comments in case of zip exceptions.
	 */
	public static ZipFile open(File file) throws IOException {

		// default encoding for zip entries is UTF-8
		try {
			return new ZipFile(file);
		} catch (ZipException ignored) {
		}

		// test the default platform encoding if it is not UTF-8
		if (!Objects.equals(Charset.defaultCharset(), StandardCharsets.UTF_8)) {
			try {
				return new ZipFile(file, Charset.defaultCharset());
			} catch (ZipException ignored) {
			}
		}

		// test all other available encodings
		var handled = new HashSet<String>();
		handled.add(StandardCharsets.UTF_8.name());
		handled.add(Charset.defaultCharset().name());
		for (var e : Charset.availableCharsets().entrySet()) {
			if (handled.contains(e.getKey()))
				continue;
			try {
				return new ZipFile(file, e.getValue());
			} catch (ZipException ignored) {
			}
			handled.add(e.getValue().name());
		}

		throw new ZipException(
				"failed to open archive after testing all possible encodings");
	}

}
