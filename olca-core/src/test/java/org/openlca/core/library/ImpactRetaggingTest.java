package org.openlca.core.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Dirs;

public class ImpactRetaggingTest {

	private final IDatabase db = Tests.getDb();

	@Before
	public void setup() {
		// todo: this is currently necessary because other
		// tests do not delete their data (but they should)
		db.clear();
	}

	@Test
	public void testImpactRetagging() throws IOException {
		var tempDir = Files.createTempDirectory("olca_").toFile();
		var libDir = LibraryDir.of(tempDir);
		var lib = libDir.create("lib");

		var units = db.insert(UnitGroup.of("Units of mass", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var r1 = db.insert(Flow.elementary("r1", mass));
		var r2 = db.insert(Flow.elementary("r1", mass));
		var impact = ImpactCategory.of("Resource depletion", "kg Au-eq.");
		impact.direction = Direction.INPUT;
		impact.factor(r1, 2);
		impact.factor(r2, 3);
		db.insert(impact);

		new LibraryExport(db, lib.folder()).run();
		Mounter.of(db, lib)
			.apply(lib, MountAction.RETAG)
			.run();

		var dataPackages = db.getDataPackages();
		assertTrue(!dataPackages.isEmpty());
		assertTrue(dataPackages.isLibrary("lib"));
		impact = db.get(ImpactCategory.class, impact.id);
		assertNotNull(impact.dataPackage);
		assertTrue(dataPackages.isLibrary(impact.dataPackage));
		assertEquals("lib", impact.dataPackage);
		assertEquals(Direction.INPUT, impact.direction);
		assertTrue(impact.impactFactors.isEmpty());

		var r = LibReader.of(lib, db).create();
		var factors = r.getImpactFactors(Descriptor.of(impact), db);
		assertEquals(2, factors.size());
		for (var f : factors) {
			if (r1.equals(f.flow)) {
				assertEquals(2, f.value, 1e-16);
			} else {
				assertEquals(3, f.value, 1e-16);
			}
		}

		db.delete(impact, r2, r1, mass, units);
		db.removeDataPackage("lib");
		Dirs.delete(tempDir);
	}

}
