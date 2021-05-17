package org.openlca.core.matrix.index;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class FlowIndexTest {

	private final IDatabase db = Tests.getDb();
	private long nextID = 1L;

	@Test
	public void testIt() {
		EnviIndex idx = EnviIndex.createRegionalized();
		List<FlowDescriptor> flows = new ArrayList<>();
		List<LocationDescriptor> locations = new ArrayList<>();
		List<Boolean> isInput = new ArrayList<>();

		// build the index
		boolean b = false;
		for (int i = 0; i < 10_000; i++) {
			var iFlow = b
					? EnviFlow.inputOf(randFlow(), randLocation())
					: EnviFlow.outputOf(randFlow(), randLocation());
			flows.add(iFlow.flow());
			locations.add(iFlow.location());
			isInput.add(b);

			Assert.assertFalse(idx.contains(iFlow));
			Assert.assertFalse(idx.contains(iFlow.flow().id, iFlow.location().id));
			int j = idx.add(iFlow);
			Assert.assertEquals(i, j);
			b = !b;
		}

		// check the index
		for (int i = 0; i < 10_000; i++) {
			EnviFlow iflow = idx.at(i);
			Assert.assertNotNull(iflow);
			Assert.assertEquals(i, idx.of(iflow));
			Assert.assertEquals(flows.get(i), iflow.flow());
			Assert.assertNotNull(iflow.location());
			Assert.assertEquals(locations.get(i), iflow.location());

			Assert.assertEquals(i, idx.of(iflow.flow().id, iflow.location().id));
			Assert.assertEquals(i, idx.of(iflow.flow(), iflow.location()));
			Assert.assertTrue(idx.contains(iflow.flow().id, iflow.location().id));
			Assert.assertEquals(isInput.get(i), iflow.isInput());
		}
	}

	@Test
	public void testNonRegFlows() {

		EnviIndex idx = EnviIndex.createRegionalized();

		// add 500 flows with location and 500 without
		boolean isInput = true;
		for (int i = 0; i < 1000; i++) {
			if (i % 5 == 0) {
				isInput = !isInput;
			}
			int _i;
			if (i % 2 == 0) {
				_i = isInput
					? idx.add(EnviFlow.inputOf(randFlow()))
					: idx.add(EnviFlow.outputOf(randFlow()));
			} else {
				_i = isInput
					? idx.add(EnviFlow.inputOf(randFlow(), randLocation()))
					: idx.add(EnviFlow.outputOf(randFlow(), randLocation()));
			}
			Assert.assertEquals(i, _i);
		}

		// check the index
		Assert.assertEquals(1000, idx.size());
		for (int i = 0; i < 1000; i++) {
			EnviFlow iflow = idx.at(i);
			Assert.assertNotNull(iflow);
			if (i % 2 == 0) {
				Assert.assertNull(iflow.location());
			} else {
				Assert.assertNotNull(iflow.location());
			}
		}
	}

	private FlowDescriptor randFlow() {
		FlowDescriptor flow = new FlowDescriptor();
		flow.id = nextID++;
		flow.name = "Flow " + flow.id;
		return flow;
	}

	private LocationDescriptor randLocation() {
		LocationDescriptor loc = new LocationDescriptor();
		loc.id = nextID++;
		loc.name = "Location " + loc.id;
		return loc;
	}

	@Test
	public void testFlowIndexFromImpacts() {

		// build a simple regionalized impact category from scratch
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var nox = Flow.elementary("NOx", mass);
		nox.category = CategoryDao.sync(
			db, ModelType.FLOW, "emissions/to air");
		db.insert(nox);
		var de = db.insert(Location.of("DE"));
		var us = db.insert(Location.of("US"));
		var impacts = ImpactCategory.of("Impacts");
		impacts.factor(nox, 1); // default
		impacts.factor(nox, 0.5).location = de;
		impacts.factor(nox, 0.25).location = us;
		db.insert(impacts);

		// build a non-regionalized index
		var index = EnviIndex.create(db,
			ImpactIndex.of(List.of(Descriptor.of(impacts))));
		assertEquals(1, index.size());
		var iFlow = Objects.requireNonNull(index.at(0));
		assertEquals(nox.id, iFlow.flow().id);
		assertNull(iFlow.location());

		// build a regionalized index
		var regIndex = EnviIndex.createRegionalized(db,
			ImpactIndex.of(List.of(Descriptor.of(impacts))));
		assertEquals(3, regIndex.size());
		var found = new boolean[]{false, false, false};
		regIndex.each((_i, f) -> {
			assertEquals(nox.id, f.flow().id);
			if (f.location() == null) {
				found[0] = true;
			} else if (f.location().id == de.id) {
				found[1] = true;
			} else if (f.location().id == us.id) {
				found[2] = true;
			}
		});
		assertArrayEquals(
			new boolean[]{true, true, true}, found);

		// clean up the database
		db.delete(
			impacts,
			de,
			us,
			nox,
			mass,
			units);
	}
}
