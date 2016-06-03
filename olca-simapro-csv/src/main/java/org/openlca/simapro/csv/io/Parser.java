package org.openlca.simapro.csv.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Parser implements Closeable {

	private final BufferedReader buffer;

	public Parser(File file) throws IOException {
		this(file, "windows-1252");
	}

	public Parser(File file, String charset) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		Reader reader = new InputStreamReader(stream, charset);
		this.buffer = new BufferedReader(reader);
	}

	public Parser(Reader reader) {
		this.buffer = new BufferedReader(reader);
	}

	@Override
	public void close() throws IOException {
		buffer.close();
	}

	public void parse(EventHandler handler) throws IOException {
		String block = null;
		String section = null;
		boolean inSections = false;
		String line = null;
		while ((line = buffer.readLine()) != null) {

			line = line.trim().replace((char) 127, '\n');

			if (line.startsWith("{") && line.endsWith("}")) {
				handler.accept(Event.COMMENT, line);
				continue;
			}
			if (line.equalsIgnoreCase("End")) {
				handler.accept(Event.END_BLOCK, block);
				block = null;
				section = null;
				inSections = false;
				continue;
			}

			if (line.isEmpty() && block == null) {
				// empty line before block
				continue;
			}

			if (!line.isEmpty() && block == null) {
				handler.accept(Event.START_BLOCK, line);
				block = line;
				continue;
			}

			if (line.isEmpty() && block != null) {
				// within block sections
				if (section != null) {
					handler.accept(Event.END_SECTION, section);
				}
				section = null;
				inSections = true;
				continue;
			}

			if (!inSections) {
				handler.accept(Event.DATA_ROW, line);
			} else if (section == null) {
				handler.accept(Event.START_SECTION, line);
				section = line;
			} else {
				handler.accept(Event.DATA_ROW, line);
			}
		}
	}
}
