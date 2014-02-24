package org.openlca.simapro.csv;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.simapro.csv.model.SPDataSet;
import org.openlca.simapro.csv.model.SPDocumentation;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.model.SPUnit;
import org.openlca.simapro.csv.model.types.ProcessCategory;
import org.openlca.simapro.csv.model.types.ProcessType;

public class ReadWriteTest {

	/** Set this to false if you want to inspect the created files. */
	private final boolean DELETE_FILES = true;

	private Logger log = Logger.getLogger("ReadWriteTest");
	private String project;
	private File tempDir;

	@Before
	public void setUp() {
		String tempDirPath = System.getProperty("java.io.tmpdir");
		tempDir = new File(tempDirPath);
		project = "sp-project-" + UUID.randomUUID().toString();
	}

	@After
	public void tearDown() {
		File file = csvFile();
		if (!DELETE_FILES) {
			log.info("none-delete config; file " + file + " is not deleted");
			return;
		}
		if (!file.exists())
			log.warning("no csv-file created, but " + file + " should exist");
		else {
			log.info("delete csv file: " + file);
			file.delete();
		}
	}

	@Test
	@Ignore
	public void testEmpty() throws Exception {
		SPDataSet dataSet = new SPDataSet(project);
		// SPDataSet dataSetCopy = writeRead(dataSet);
		// assertEquals(project, dataSetCopy.getProject());
		// char c1 = 127;
		// char c2 = 127;
		// System.out.println(c1 + c2);
	}

	@Test
	@Ignore
	public void testProcess() throws Exception {
		SPDataSet dataSet = new SPDataSet(project);
		SPUnit unit = new SPUnit("kg");
		SPProduct product = new SPProduct("product", "unit", "1");
		SPDocumentation doc = new SPDocumentation("process",
				ProcessCategory.MATERIAL, ProcessType.UNIT_PROCESS);
		SPProcess process = new SPProcess(product, "test", doc);
		dataSet.add(process);
		// SPDataSet dataSetCopy = writeRead(dataSet);
		// assertEquals("product",
		// dataSetCopy.getProcesses()[0].getByProducts()[0].getName());
	}

	// private SPDataSet writeRead(SPDataSet dataSet) throws IOException {
	// CSVWriter writer = new CSVWriter();
	// writer.write(tempDir, dataSet);
	// CSVReader reader = new CSVReader();
	// SPDataSet dataSetCopy = reader.read(csvFile());
	// return dataSetCopy;
	// }

	private File csvFile() {
		return new File(tempDir, project + ".csv");
	}

}
