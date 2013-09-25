package org.openlca.io.ecospold2;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.io.TestSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class EcoSpold2ImportTest {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database = TestSession.getDerbyDatabase();
	private ProcessDao dao = new ProcessDao(database);
	private final String REF_ID = "e926dd9b-7045-3a90-9702-03e0b1376607";
	private File tempFile;

	@Before
	public void setUp() throws Exception {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		tempFile = new File(tempDir, UUID.randomUUID().toString() + ".spold");
		log.trace("copy ecospold 2 file to {}", tempFile);
		ByteStreams.copy(EcoSpold2ImportTest.class
				.getResourceAsStream("sample_ecospold2.xml"), Files
				.newOutputStreamSupplier(tempFile));
		EcoSpold2Import eImport = new EcoSpold2Import(database);
		eImport.setFiles(new File[] { tempFile });
		eImport.run();
	}

	@After
	public void tearDown() throws Exception {
		log.trace("delete file {}", tempFile);
		boolean success = tempFile.delete();
		log.trace("success? = {}", success);
		Process process = dao.getForRefId(REF_ID);
		dao.delete(process);
	}

	@Test
	public void testProcessExists() {
		Process process = dao.getForRefId(REF_ID);
		Assert.assertNotNull(process);
	}

	@Test
	public void testFormulaImported() {
		Process process = dao.getForRefId(REF_ID);
		String formula = process.getQuantitativeReference().getAmountFormula();
		Assert.assertEquals("23 + SUM(8;2)", formula);
	}

	@Test
	public void testParameterImported() {
		Process process = dao.getForRefId(REF_ID);
		List<Parameter> parameters = process.getParameters();
		Assert.assertEquals(2, parameters.size());
		for (Parameter parameter : parameters) {
			Assert.assertTrue(Objects.equal("vehicle_life", parameter.getName())
					|| Objects.equal("allard_mine_area_yearly_growth",
							parameter.getName()));
		}
	}

	@Test
	public void testUncertaintyImported() {
		Process process = dao.getForRefId(REF_ID);
		Uncertainty uncertainty = process.getQuantitativeReference()
				.getUncertainty();
		Assert.assertEquals(UncertaintyType.LOG_NORMAL,
				uncertainty.getDistributionType());
		Assert.assertEquals(33, uncertainty.getParameter1Value(), 1e-16);
	}

}
