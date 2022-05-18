package org.openlca.core.matrix;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactDirectionTest {

	private final IDatabase db = Tests.getDb();
	private Flow flow;
	private ImpactCategory impact;

	@Before
	public void setup() {
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		flow = db.insert(Flow.elementary("f", mass));
		impact = ImpactCategory.of("i");
		impact.factor(flow, 42);
		impact = db.insert(impact);
	}

	@After
	public void cleanup() {
		var mass = flow.referenceFlowProperty;
		var units = mass.unitGroup;
		db.delete(impact, flow, mass, units);
	}

	@Test
	public void testDescriptorDirection() {
		var dirs = new Direction[]{
			Direction.INPUT, Direction.OUTPUT, null};
		for (var dir : dirs) {
			impact.direction = dir;
			db.update(impact);
			var d = (ImpactDescriptor) db.getDescriptor(
				ImpactCategory.class, impact.id);
			assertEquals(dir, d.direction);
		}
	}

	@Test
	public void testMatrixValues() {
		var i = Direction.INPUT;
		var o = Direction.OUTPUT;
		Direction x = null;
		double pos = 42;
		double neg = -42;

		checkMatrix(i, x, neg);
		checkMatrix(o, x, pos);
		checkMatrix(i, i, neg);
		checkMatrix(o, i, neg);
		checkMatrix(i, o, pos);
		checkMatrix(o, o, pos);
	}

	private void checkMatrix(
		Direction flowDir, Direction impactDir, double expected) {

		var enviFlow = flowDir == Direction.INPUT
			? EnviFlow.inputOf(Descriptor.of(flow))
			: EnviFlow.outputOf(Descriptor.of(flow));
		var enviIdx = EnviIndex.create();
		enviIdx.add(enviFlow);
		impact.direction = impactDir;
		impact = db.update(impact);

		var impactIdx = ImpactIndex.of(
			List.of(Descriptor.of(impact)));
		var matrix = ImpactBuilder.of(db, enviIdx)
			.withImpacts(impactIdx)
			.build().impactMatrix;
		assertEquals(expected, matrix.get(0, 0), 1e-10);
	}
}
