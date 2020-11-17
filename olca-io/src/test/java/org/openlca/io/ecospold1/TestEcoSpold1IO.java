package org.openlca.io.ecospold1;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;
import org.openlca.io.Tests;
import org.openlca.io.ecospold1.input.EcoSpold01Import;
import org.openlca.io.ecospold1.input.ImportConfig;
import org.openlca.io.ecospold1.output.EcoSpold1Export;
import org.openlca.io.ecospold1.output.ExportConfig;

public class TestEcoSpold1IO {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProcessIO() throws Exception {

		// create the process model
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var co2 = Flow.elementary("CO2", mass);
		var steel = Flow.product("steel", mass);
		var process = Process.of("Steel production", steel);
		process.output(co2, 2);
		var entities = List.of(units, mass, co2, steel, process);
		entities.forEach(db::insert);

		var dir = Files.createTempDirectory("_spold_out").toFile();
		var export = new EcoSpold1Export(dir);
		export.export(process);
		Tests.clearDb();

		var files = dir.listFiles();
		assertNotNull(files);
		assertTrue(files.length > 0);
		for (var file : files) {
			var type = EcoSpoldIO.getType(file);
			if (type.isEmpty())
				continue;
		}
	}

	@Test
	public void testIO() throws Exception {
		InputStream in = openXml();
		DataSetType type = EcoSpoldIO.getType(in).orElse(null);
		ImportConfig config = new ImportConfig(db);
		EcoSpold01Import es1Import = new EcoSpold01Import(config);
		es1Import.run(openXml(), type);
		ProcessDao dao = new ProcessDao(db);
		List<Process> processes = dao.getForName("Bauxite, at mine");
		Process process = processes.get(0);
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File dir = new File(tmpDirPath);
		EcoSpold1Export export = new EcoSpold1Export(dir,
				ExportConfig.getDefault());
		export.export(process);
		export.close();
		dao.delete(process);
		File esDir = new File(dir, "EcoSpold01");
		File file = new File(esDir, "process_" + process.refId + ".xml");
		Assert.assertTrue(file.exists());
		Files.delete(file.toPath());
	}

	private InputStream openXml() {
		return getClass().getResourceAsStream("sample_ecospold01.xml");
	}

}
