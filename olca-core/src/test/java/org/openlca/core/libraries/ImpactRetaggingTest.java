package org.openlca.core.libraries;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryExport;
import org.openlca.core.library.MountAction;
import org.openlca.core.library.Mounter;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Dirs;

public class ImpactRetaggingTest {

	private final IDatabase db = Tests.getDb();

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

		assertTrue(db.hasLibraries());
		assertTrue(db.getLibraries().contains("lib"));
		impact = db.get(ImpactCategory.class, impact.id);
		assertEquals("lib", impact.library);
		assertEquals(Direction.INPUT, impact.direction);
		assertTrue(impact.impactFactors.isEmpty());

		var factors = lib.getImpactFactors(Descriptor.of(impact), db);
		assertEquals(2, factors.size());
		for (var f : factors) {
			if (r1.equals(f.flow)) {
				assertEquals(2, f.value, 1e-16);
			} else {
				assertEquals(3, f.value, 1e-16);
			}
		}

		db.delete(impact, r2, r1, mass, units);
		db.removeLibrary("lib");
		Dirs.delete(tempDir);
	}

}
