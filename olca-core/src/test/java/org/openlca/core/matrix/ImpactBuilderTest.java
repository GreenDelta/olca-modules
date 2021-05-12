package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class ImpactBuilderTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void buildEmptyMatrix() {
		// build the flow index
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var flowIdx = EnviIndex.create();
		Stream.of(
			Flow.elementary("e1", mass),
			Flow.elementary("e2", mass),
			Flow.elementary("e3", mass))
			.map(db::insert)
			.forEach(f -> flowIdx.add(
				EnviFlow.outputOf(Descriptor.of(f))));

		// build the impact index
		var impactIdx = new ImpactIndex();
		Stream.of(
			ImpactCategory.of("i1"),
			ImpactCategory.of("i2"))
			.map(db::insert)
			.forEach(i -> impactIdx.add(Descriptor.of(i)));

		// create and check the matrix
		var matrix = ImpactBuilder.of(db, flowIdx)
			.withImpacts(impactIdx)
			.build()
			.impactMatrix;
		assertEquals(impactIdx.size(), matrix.rows());
		assertEquals(flowIdx.size(), matrix.columns());
		for (int row = 0; row < matrix.rows(); row++) {
			for (int col = 0; col < matrix.columns(); col++) {
				assertEquals(0, matrix.get(row, col), 1e-16);
			}
		}

		// delete the things from the database
		impactIdx.content()
			.stream()
			.map(i -> db.get(ImpactCategory.class, i.id))
			.forEach(db::delete);
		flowIdx.content()
			.stream()
			.map(f -> db.get(Flow.class, f.flow().id))
			.forEach(db::delete);
		db.delete(mass, units);
	}

}
