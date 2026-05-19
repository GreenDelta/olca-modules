package org.openlca.io.ecospold1;

import static org.junit.Assert.*;

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
import org.openlca.ecospold.IExchange;
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
		process.processType = ProcessType.UNIT_PROCESS;
		process.input(coal, 42).defaultProviderId = provider.id;
		process.output(e, 2);
		db.insert(e, steel, process);
	}

	@After
	public void cleanup() {
		db.clear();
		Dirs.delete(dir);
	}

	@Test
	public void testBaseNames() {
		var config = EcoSpold1Export.of(db, dir);
		assertNames(config, "steel", "coal");
	}

	@Test
	public void testProcessSuffixes() {
		var config = EcoSpold1Export.of(db, dir)
			.withProcessSuffixes(true);
		assertNames(config,
			"steel | steel production", "coal | coal production");
	}

	@Test
	public void testLocationSuffixes() {
		var config = EcoSpold1Export.of(db, dir)
			.withLocationSuffixes(true);
		assertNames(config, "steel {DE}", "coal {PL}");
	}

	@Test
	public void testTypeSuffixes() {
		var config = EcoSpold1Export.of(db, dir)
			.withTypeSuffixes(true);
		assertNames(config, "steel, U", "coal, S");
	}

	@Test
	public void testFullNames() {
		var config = EcoSpold1Export.of(db, dir)
			.withProcessSuffixes(true)
			.withLocationSuffixes(true)
			.withTypeSuffixes(true);
		assertNames(config,
			"steel | steel production {DE}, U",
			"coal | coal production {PL}, S");
	}

	private void assertNames(EcoSpold1Config config, String ref, String input) {
		var spold = spoldOf(config);
		assertEquals(ref, spold.getMetaInformation()
			.getProcessInformation()
			.getReferenceFunction()
			.getName());
		assertEquals(input, exchangeOf(spold, "coal").getName());
		assertEquals("carbon dioxide",
			exchangeOf(spold, "carbon dioxide").getName());
	}

	private IExchange exchangeOf(IDataSet ds, String prefix) {
		for (var e : ds.getFlowData().getFirst().getExchange()) {
			var name = e.getName();
			if (name != null && name.startsWith(prefix))
				return e;
		}
		throw new RuntimeException(
			"Could not find exchange with prefix: " + prefix);
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
