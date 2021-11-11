package org.openlca.io.simapro.csv.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openlca.simapro.csv.SimaProCsv;

class TestCsv {

	private TestCsv() {
	}

	static File temporaryFileOf(String name) {
		var s = TestCsv.class.getResourceAsStream(name);
		try (var stream = Objects.requireNonNull(s);
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				 var buffer = new BufferedReader(reader)) {
			var text = buffer.lines().collect(Collectors.joining("\n"));
			var tempFile = Files.createTempFile("olca_test", ".csv");
			Files.writeString(tempFile, text, SimaProCsv.defaultCharset());
			return tempFile.toFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
