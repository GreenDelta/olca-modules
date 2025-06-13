package org.openlca.core.library.export;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.Mounter;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Dirs;

public class NoMatrixDataTest {

	private final IDatabase db = Tests.getDb();
	private File libRoot;
	private File libDir;

	@Before
	public void setup() throws Exception {
		db.clear();
		var units = UnitGroup.of("*Units of mass*", "*kg*");
		var mass = FlowProperty.of("*Mass*", units);
		var flow = Flow.elementary("*Flow*", mass);
		db.insert(units, mass, flow);
		libRoot = Files.createTempDirectory("olca_test").toFile();
		libDir = new File(libRoot, "basic_lib");
		new LibraryExport(db, libDir).run();
		db.clear();
	}

	@After
	public void cleanup() {
		db.clear();
		Dirs.delete(libRoot);
	}

	@Test
	public void testMountBasicEntities() {
		var lib = Library.of(libDir);
		assertNotNull("Library could not be loaded", lib);
		Mounter.of(db, lib).run();

		var groups = db.getAll(UnitGroup.class);
		assertEquals(1, groups.size());
		var group = groups.getFirst();
		assertEquals("*Units of mass*", group.name);
		assertEquals("*kg*", group.referenceUnit.name);

		var props = db.getAll(FlowProperty.class);
		assertEquals(1, props.size());
		var prop = props.getFirst();
		assertEquals("*Mass*", prop.name);
		assertEquals(group, prop.unitGroup);

		var flows = db.getAll(Flow.class);
		assertEquals(1, flows.size());
		var flow = flows.getFirst();
		assertEquals("*Flow*", flow.name);
		assertEquals(FlowType.ELEMENTARY_FLOW, flow.flowType);
		assertEquals(prop, flow.referenceFlowProperty);
	}
}
