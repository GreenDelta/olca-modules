package org.openlca.core.library.export;

import static org.junit.Assert.assertEquals;

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
import org.openlca.core.model.Process;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.Dirs;

public class ExportScalerTest {

	private final IDatabase db = Tests.getDb();
	private File libRoot;
	private File libDir;

	@Before
	public void setup() throws Exception {
		db.clear();

		// create reference data
		var timeUnits = UnitGroup.of("Units of time", "h");
		var time = FlowProperty.of("Time", timeUnits);
		var massUnits = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", massUnits);

		var socialIndicator = SocialIndicator.of("SI", time);
		socialIndicator.unitOfMeasurement = "%";
		var socialFlow = Flow.elementary("SI", time);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);

		db.insert(
				timeUnits, time, massUnits, mass, socialIndicator, socialFlow, p, q);

		// create processes and social aspects
		var P = Process.of("P", p);
		P.input(p, 0.1);
		P.input(q, 0.2);
		P.output(socialFlow, 5.0);
		var ap = SocialAspect.of(P, socialIndicator);
		ap.riskLevel = RiskLevel.HIGH_RISK;
		ap.rawAmount = "70";
		ap.activityValue = 5.0;

		var Q = Process.of("Q", q);
		Q.input(q, 0.2);
		Q.input(p, 0.9);
		Q.output(socialFlow, 3.0);
		SocialAspect aq = SocialAspect.of(Q, socialIndicator);
		aq.riskLevel = RiskLevel.LOW_RISK;
		aq.rawAmount = "30";
		aq.activityValue = 3.0;

		db.insert(P, Q);

		// create the library
		libRoot = Files.createTempDirectory("_olca_tests").toFile();
		libDir = new File(libRoot, "sca-lib");
		new LibraryExport(db, libDir)
				.withInversion(true)
				.run();
	}

	@After
	public void cleanup() {
		db.clear();
		Dirs.delete(libRoot);
	}

	@Test
	public void testLibraryScaling() {
		db.clear();
		var lib = Library.of(libDir);
		Mounter.of(db, lib).run();

		var P = db.getForName(Process.class, "P");
		var ap = P.socialAspects.getFirst();
		assertEquals(5.0 * 1 / 0.9, ap.activityValue, 1e-16);

		var Q = db.getForName(Process.class, "Q");
		var aq = Q.socialAspects.getFirst();
		assertEquals(3.0 * 1 / 0.8, aq.activityValue, 1e-16);
	}

}
