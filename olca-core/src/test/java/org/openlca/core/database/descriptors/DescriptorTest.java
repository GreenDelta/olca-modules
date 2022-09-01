package org.openlca.core.database.descriptors;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.TLongSets;

public class DescriptorTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRootDescriptors() throws Exception {

		var cat = db.insert(Category.of("some", null));
		var refId = UUID.randomUUID().toString();
		var version = Version.of(1, 2, 3).getValue();
		var lastChange = System.nanoTime();

		Consumer<RootDescriptor> test = d -> {
			assertEquals(refId, d.refId);
			assertEquals(d.type.name(), d.name);
			assertEquals("description", d.description);
			assertEquals("tags", d.tags);
			assertEquals(version, d.version);
			assertEquals(lastChange, d.lastChange);
			assertEquals("lib7", d.library);
			assertEquals(cat.id, d.category.longValue());
		};

		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;

			var clazz = type.getModelClass();
			var e = clazz.getConstructor().newInstance();
			e.refId = refId;
			e.name = type.name();
			e.category = cat;
			e.description = "description";
			e.tags = "tags";
			e.version = version;
			e.lastChange = lastChange;
			e.library = "lib7";
			db.insert(e);
			check(e, test);
			db.delete(e);
		}

		db.delete(cat);
	}

	@Test
	public void testCategoryDescriptor() {
		var category = db.insert(Category.of("elem. flows", ModelType.FLOW));
		check(category, d -> {
			if (!(d instanceof CategoryDescriptor cd)) {
				fail("expected type was CategoryDescriptor");
			} else {
				assertEquals(ModelType.FLOW, cd.categoryType);
			}
		});
		db.delete(category);
	}

	@Test
	public void testFlowDescriptor() {
		var units = db.insert(UnitGroup.of("mass units", "kg"));
		var mass = db.insert(FlowProperty.of("mass", units));
		var loc = db.insert(Location.of("France", "FR"));
		var flow = Flow.product("Steel", mass);
		flow.location = loc;
		db.insert(flow);
		check(flow, d -> {
			var fd = (FlowDescriptor) d;
			assertEquals(loc.id, fd.location.longValue());
			assertEquals(FlowType.PRODUCT_FLOW, fd.flowType);
			assertEquals(mass.id, fd.refFlowPropertyId);
		});
		db.delete(units, mass, loc, flow);
	}


	@Test
	public void testImpactDescriptor() {
		var impact = ImpactCategory.of("GWP", "CO2eq");
		impact.direction = Direction.OUTPUT;
		db.insert(impact);
		check(impact, d -> {
			var imp = (ImpactDescriptor) d;
			assertEquals("CO2eq", imp.referenceUnit);
			assertEquals(Direction.OUTPUT, imp.direction);
		});
		db.delete(impact);
	}

	@Test
	public void testLocationDescriptor() {
		var location = db.insert(Location.of("France", "FR"));
		check(location, d -> {
			var loc = (LocationDescriptor) d;
			assertEquals("FR", loc.code);
		});
		db.delete(location);
	}

	@Test
	public void testProcessDescriptor() {
		var empty = new Process();
		empty.refId = UUID.randomUUID().toString();
		db.insert(empty);
		check(empty, d -> {
			var p = (ProcessDescriptor) d;
			assertEquals(ProcessType.UNIT_PROCESS, p.processType);
			assertNull(p.flowType);
			assertNull(p.location);
		});

		var units = db.insert(UnitGroup.of("mass units", "kg"));
		var mass = db.insert(FlowProperty.of("mass", units));
		var loc = db.insert(Location.of("France", "FR"));
		var flow = db.insert(Flow.product("Steel", mass));
		var full = Process.of("Steel production", flow);
		full.processType = ProcessType.LCI_RESULT;
		full.location = loc;
		db.insert(full);
		check(full, d -> {
			var p = (ProcessDescriptor) d;
			assertEquals(ProcessType.LCI_RESULT, p.processType);
			assertEquals(FlowType.PRODUCT_FLOW, p.flowType);
			assertEquals(loc.id, p.location.longValue());
		});

		db.delete(full, loc, flow, mass, units, empty);
	}

	private <T extends RootEntity> void check(T e, Consumer<RootDescriptor> f) {

		Consumer<Collection<? extends Descriptor>> fs = coll -> {
			var d = coll.stream()
					.filter(x -> Objects.equals(x.refId, e.refId))
					.findAny()
					.map(RootDescriptor.class::cast)
					.orElseThrow();
			f.accept(d);
		};

		var clazz = e.getClass();

		f.accept(db.getDescriptor(clazz, e.id));
		f.accept(db.getDescriptor(clazz, e.refId));
		fs.accept(db.getDescriptors(clazz));
		fs.accept(db.getDescriptors(clazz, Set.of(e.id)));
		fs.accept(db.getDescriptors(clazz, TLongSets.singleton(e.id)));

		var dao = Daos.root(db, clazz);
		f.accept(dao.getDescriptor(e.id));
		f.accept(dao.getDescriptorForRefId(e.refId));

		fs.accept(dao.getDescriptors());
		fs.accept(dao.getDescriptors(Set.of(e.id)));
		fs.accept(dao.getDescriptors(Optional.ofNullable(e.category)));
	}

}
