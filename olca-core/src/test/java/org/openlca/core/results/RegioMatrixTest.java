package org.openlca.core.results;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class RegioMatrixTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRegioMatrix() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var e = Flow.elementary("e", mass);
		var loc = Location.of("LOC");

		var regCat = ImpactCategory.of("reg");
		regCat.factor(e, 2.0);
		regCat.factor(e, 4.0).location = loc;
		var nonReg = ImpactCategory.of("nonReg");
		nonReg.factor(e, 2.0);

		db.insert(units, mass, e, loc, regCat, nonReg);

		var enviFlow = EnviFlow.outputOf(Descriptor.of(e), Descriptor.of(loc));
		var enviIdx = EnviIndex.createRegionalized();
		enviIdx.add(enviFlow);
		var impactIdx = ImpactIndex.of(
				List.of(Descriptor.of(regCat), Descriptor.of(nonReg)));
		var matrix = ImpactBuilder.of(db, enviIdx)
				.withImpacts(impactIdx)
				.build()
				.impactMatrix;

		db.delete(nonReg, regCat, loc, e, mass, units);

		assertEquals(4, matrix.get(0, 0), 1e-10); // regionalized category
		assertEquals(2, matrix.get(1, 0), 1e-10); // non-regionalized category
	}
}
