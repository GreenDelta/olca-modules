package org.openlca.io.ecospold1;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.ecospold.DataSetType;
import org.openlca.ecospold.EcoSpold;
import org.openlca.ecospold.model.DataSet;
import org.openlca.io.Tests;
import org.openlca.io.ecospold1.input.EcoSpold1Import;
import org.openlca.io.ecospold1.input.ImportConfig;
import org.openlca.io.ecospold1.output.EcoSpold1Export;
import org.openlca.util.Dirs;

public class ImportExportTest {

	private final IDatabase db = Tests.getDb();
	private File dir;

	@Before
	public void setup() throws Exception {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		db.insert(units, mass);

		// copy the test file
		dir = Files.createTempDirectory("_olca_").toFile();
		var file = new File(dir, UUID.randomUUID() + ".xml").toPath();
		var stream = getClass().getResourceAsStream("example.xml");
		assertNotNull(stream);
		try (stream) {
			Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
		}

		// import the test file
		var config = new ImportConfig(Tests.getDb());
		var imp = new EcoSpold1Import(config);
		imp.setFiles(new File[]{file.toFile()});
		imp.run();

		Files.delete(file);
	}

	@After
	public void tearDown() {
		db.clear();
		Dirs.delete(dir);
	}

	@Test
	public void testProcess() {
		var proc = getProcess();
		var doc = proc.documentation;
		assertNotNull(doc);
		assertEquals(
			"Values were created solely for software tests and documentation examples.",
			doc.samplingProcedure);
		assertEquals(
			"No extrapolation was applied because the dataset is synthetic.",
			doc.dataTreatment);
		assertEquals("None.", doc.dataSelection);
		assertEquals("1500 kg/year", doc.dataCompleteness);

		assertNotNull(doc.validUntil);
		var calendar = Calendar.getInstance();
		calendar.setTime(doc.validUntil);
		assertEquals(2025, calendar.get(Calendar.YEAR));
		assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH));
		assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH));

		var version = new Version(proc.version);
		assertEquals(12, version.getMajor());
		assertEquals(34, version.getMinor());
		assertEquals(67, version.getUpdate());
	}

	@Test
	public void testRoundTrip() throws Exception {

		// export the process
		var process = getProcess();
		var config = EcoSpold1Export.of(Tests.getDb(), dir)
			.writeDefaultValues(true);
		try (var exporter = config.create().orElseThrow()) {
			exporter.export(process);
		}

		// find the exported file
		var files = dir.listFiles(
			(dir, name) -> name.startsWith("process_") && name.endsWith(".xml"));
		assertNotNull("No exported files found", files);
		assertTrue("No exported files found", files.length > 0);
		var file = files[0];

		// read the exported EcoSpold dataset
		var spold = EcoSpold.read(file, DataSetType.PROCESS).orElseThrow();
		assertFalse("No datasets in exported file", spold.getDataSets().isEmpty());
		var ds = new DataSet(
			spold.getDataSets().getFirst(), DataSetType.PROCESS.getFactory());
		EcoSpold.write(new File("target/spold.xml"), spold);

		// check the fields
		var info = ds.getDataSetInformation();
		assertNotNull(info);
		assertEquals(12.34f, info.getVersion(), 0.001f);
		assertEquals(67.0f, info.getInternalVersion(), 0.001f);

		var repr = ds.getRepresentativeness();
		assertNotNull(repr);
		assertEquals(
			"Values were created solely for software tests and documentation examples.",
			repr.getSamplingProcedure());
		assertEquals(
			"No extrapolation was applied because the dataset is synthetic.",
			repr.getExtrapolations());
		assertEquals("None.", repr.getUncertaintyAdjustments());
		assertEquals("1500 kg/year", repr.getProductionVolume());

		var refFun = ds.getReferenceFunction();
		assertNotNull(refFun);
		assertEquals("community garden compost service", refFun.getName());
		assertEquals("kg", refFun.getUnit());
		assertEquals("waste treatment", refFun.getCategory());
		assertEquals("community composting", refFun.getSubCategory());

		var exchanges = ds.getExchanges();
		assertEquals("Expected 2 exchanges", 2, exchanges.size());
		var output = exchanges.getFirst();
		assertEquals("community garden compost service", output.getName());
		assertEquals("kg", output.getUnit());
		assertEquals(0, output.getOutputGroup().intValue());
	}

	private Process getProcess() {
		var name = "community garden compost service";
		for (var d : db.getDescriptors(Process.class)) {
			if (name.equals(d.name)) {
				return db.get(Process.class, d.id);
			}
		}
		throw new RuntimeException("Process '" + name + "' not found");
	}
}
