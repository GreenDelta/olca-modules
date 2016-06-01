package org.openlca.simapro.csv.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.openlca.simapro.csv.model.Block;
import org.openlca.simapro.csv.model.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockReader implements Closeable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final BufferedReader buffer;

	public BlockReader(File file) throws IOException {
		this(file, "windows-1252");
	}

	public BlockReader(File file, String charset) throws IOException {
		log.trace("open file {}; encoding = {}", file, charset);
		FileInputStream stream = new FileInputStream(file);
		Reader reader = new InputStreamReader(stream, charset);
		this.buffer = new BufferedReader(reader);
	}

	public BlockReader(Reader reader) {
		this.buffer = new BufferedReader(reader);
	}

	@Override
	public void close() throws IOException {
		log.trace("close reader");
		buffer.close();
	}

	/**
	 * Reads the next block. Returns null if end of the file is reached or no
	 * block was found.
	 */
	public Block read() throws IOException {
		Block block = null;
		Section section = null;
		boolean inSections = false;
		String line = null;
		while ((line = buffer.readLine()) != null) {
			line = line.trim().replace((char) 127, '\n');
			if (line.startsWith("{") && line.endsWith("}"))
				continue; // header entry or comment
			if (line.equalsIgnoreCase("End"))
				break; // end of a block
			if (line.isEmpty() && block == null)
				continue; // empty line before block
			if (!line.isEmpty() && block == null) {
				log.trace("read next block {}", line);
				block = new Block(line);
			} else if (line.isEmpty() && block != null) {
				section = null;
				inSections = true;
			} else {
				if (!inSections)
					block.dataRows.add(line);
				else if (section == null) {
					log.trace("read next section {}", line);
					section = new Section(line);
					block.addSection(section);
				} else {
					section.dataRows.add(line);
				}
			}
		}
		log.trace("return block {}", block);
		return block;
	}

}
