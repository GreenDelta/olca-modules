package org.openlca.io.ecospold2.input;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.Tests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public class EcoSpold2ImportTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final String REF_ID = "e926dd9b-7045-3a90-9702-03e0b1376607";
	private File tempFile;
	private ProcessDao dao = new ProcessDao(Tests.getDb());

	@Before
	public void setUp() throws Exception {
		Tests.getDb().clear();
		createUnit("20aadc24-a391-41cf-b340-3e4529f44bde",
				"93a60a56-a3c8-11da-a746-0800200b9a66", "kg");
		createUnit("ee5f2241-18af-4444-b457-b275660e5a20",
				"441238a3-ba09-46ec-b35b-c30cfba746d1", "km");
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		tempFile = new File(tempDir, UUID.randomUUID().toString() + ".spold");
		log.trace("copy ecospold 2 file to {}", tempFile);
		Files.copy(getClass().getResourceAsStream("sample_ecospold2.xml"),
				tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		ImportConfig config = new ImportConfig(Tests.getDb());
		EcoSpold2Import eImport = new EcoSpold2Import(config);
		eImport.setFiles(new File[] { tempFile });
		eImport.run();
	}

	private void createUnit(String unitRefId, String propertyRefId, String name) {
		Unit unit = new Unit();
		unit.name = name;
		unit.refId = unitRefId;
		UnitGroup group = new UnitGroup();
		group.refId = UUID.randomUUID().toString();
		group.referenceUnit = unit;
		group.units.add(unit);
		group = new UnitGroupDao(Tests.getDb()).insert(group);
		FlowProperty prop = new FlowProperty();
		prop.unitGroup = group;
		prop.name = "property for " + name;
		prop.refId = propertyRefId;
		prop = new FlowPropertyDao(Tests.getDb()).insert(prop);
		group.defaultFlowProperty = prop;
		group = new UnitGroupDao(Tests.getDb()).update(group);
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
		String formula = process.quantitativeReference.formula;
		Assert.assertEquals("p", formula); // a parameter p = 23 + SUM(8;2) is
											// created
	}

	@Test
	public void testParameterImported() {
		Process process = dao.getForRefId(REF_ID);
		List<Parameter> parameters = process.parameters;
		Assert.assertEquals(3, parameters.size());
		for (Parameter parameter : parameters) {
			String name = parameter.name;
			switch (name) {
			case "vehicle_life":
				Assert.assertEquals(23, parameter.value, 1e-16);
				break;
			case "p":
				Assert.assertEquals("23 + SUM(8;2)", parameter.formula);
				break;
			case "allard_mine_area_yearly_growth":
				Assert.assertEquals(1, parameter.value, 1e-16);
				break;
			default:
				Assert.fail("unknown parameter: " + parameter.name);
				break;
			}
		}
	}

	@Test
	public void testUncertaintyImported() {
		Process process = dao.getForRefId(REF_ID);
		Uncertainty uncertainty = process.quantitativeReference.uncertainty;
		Assert.assertEquals(UncertaintyType.LOG_NORMAL,
				uncertainty.distributionType);
		Assert.assertEquals(33, uncertainty.parameter1, 1e-16);
	}

}
