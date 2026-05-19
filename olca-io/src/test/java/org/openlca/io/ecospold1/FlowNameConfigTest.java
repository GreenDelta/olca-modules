package org.openlca.io.ecospold1;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.UnitGroup;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.io.EcoSpold;
import org.openlca.io.Tests;
import org.openlca.io.ecospold1.output.EcoSpold1Export;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;
import org.openlca.util.Dirs;

public class FlowNameConfigTest {

	private final IDatabase db = Tests.getDb();
	private File dir;
	private Process process;

	@Before
	public void setup() throws Exception {
		dir = Files.createTempDirectory("_spold_out").toFile();
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var coal = Flow.product("coal", mass);
		var provider = Process.of("coal production", coal);
		provider.location = db.insert(Location.of("Poland", "PL"));
		provider.processType = ProcessType.LCI_RESULT;
		db.insert(units, mass, coal, provider);

		var e = Flow.elementary("carbon dioxide", mass);
		var steel = Flow.product("steel", mass);
		process = Process.of("steel production", steel);
		process.location = db.insert(Location.of("Germany", "DE"));
		process.input(coal, 42).defaultProviderId = provider.id;
		db.insert(e, steel, process);
	}

	@After
	public void cleanup() {
		db.clear();
		Dirs.delete(dir);
	}

	@Test
	public void testFullNames() {
		var config = EcoSpold1Export.of(db, dir)
			.withProcessSuffixes(true)
			.withLocationSuffixes(true)
			.withTypeSuffixes(true);
		var spold = spoldOf(config);
		// TODO: check
	}

	private IDataSet spoldOf(EcoSpold1Config config) {
		try {
			try (var export = config.create().orElseThrow()) {
				export.export(process);
			}

			var files = dir.listFiles();
			assertNotNull(files);
			for (var file : files) {
				if (file.getName().startsWith("process_")) {
					var spold = EcoSpold.read(file).orElseThrow();
					return spold.getDataset().getFirst();
				}
			}

			throw new RuntimeException("Could not find created process data set");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
