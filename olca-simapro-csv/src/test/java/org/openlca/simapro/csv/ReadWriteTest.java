package org.openlca.simapro.csv;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.writer.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteTest {

	/**
	 * Set this to false if you want to inspect the created files.
	 */
	private final boolean DELETE_FILES = false;

	private Logger log = LoggerFactory.getLogger(getClass());

	private String project;
	private File tempFile;

	@Before
	public void setUp() throws Exception {
		tempFile = Files.createTempFile("simapro_csv_", ".csv").toFile();
		project = "sp-project-" + UUID.randomUUID().toString();
		log.trace("created CSV file {}", tempFile);
	}

	@After
	public void tearDown() {
		if (!DELETE_FILES) {
			log.info("none-delete config; file {} is not deleted", tempFile);
			return;
		}
		if (!tempFile.exists())
			log.warn("no csv-file created, but {} should exist", tempFile);
		else {
			log.info("delete csv file: {}", tempFile);
			tempFile.delete();
		}
	}

	@Test
	public void testEmpty() throws Exception {
		CSVWriter writer = new CSVWriter(tempFile);
		SPProduct product = new SPProduct();
		SPProcess process = new SPProcess(product);
		writer.write(process);
		writer.close();
	}

}
