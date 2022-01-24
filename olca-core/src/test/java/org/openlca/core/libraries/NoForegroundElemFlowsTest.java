package org.openlca.core.libraries;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class NoForegroundElemFlowsTest {

	@Test
	public void test() throws IOException {
		var db = Tests.getDb();

		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var e1 = Flow.elementary("e1", mass);
		var e2 = Flow.elementary("e2", mass);
		db.insert(units, mass, e1, e2);

		// create stubs for the library processes
		var libProviders = new ArrayList<TechFlow>();
		for (int i = 1; i < 4; i++) {
			var product = db.insert(Flow.product("p" + i, mass));
			var process = Process.of("p" + i, product);
			db.insert(process);
			libProviders.add(TechFlow.of(process));
		}

		// create library resources
		var tmpDir = Files.createTempDirectory("_olca_tests");
		var libDir = LibraryDir.of(tmpDir.toFile());
		var lib = Library.create()

		LibraryDir.of()

		db.clear();
	}

}
