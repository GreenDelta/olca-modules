package org.openlca.core.database.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ModelReferences;
import org.openlca.core.database.ModelReferences.ModelReference;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.TypedRefId;
import org.openlca.core.model.UnitGroup;

public class ResultRefTest {

	private final IDatabase db = Tests.getDb();
	private UnitGroup units;
	private FlowProperty mass;
	private Flow product;
	private Result result;

	@Before
	public void setup() {
		units = UnitGroup.of("Units of mass", "kg");
		mass = FlowProperty.of("Mass", units);
		product = Flow.product("p", mass);
		result = Result.of("result", product);
		db.insert(units, mass, product, result);
	}

	@After
	public void cleanup() {
		db.delete(result, product, mass, units);
	}

	@Test
	public void testDirectReferences() {
		var refId = new TypedRefId(ModelType.RESULT, result.refId);
		var refs = new ArrayList<ModelReference>();
		ModelReferences.scan(db).iterateReferences(refId,  ref -> {
			refs.add(ref);
		});
		assertRefs(refs, product);
	}

	@Test
	public void testDirectUsages() {
		var usages = resultUsages();
		assertTrue(usages.isEmpty());
	}

	@Test
	public void testProcessAndSystemUsages() {
		var q = Flow.product("q", mass);
		var Q = Process.of("Q", q);
		var input = Q.input(product, 1.0);
		input.defaultProviderId = result.id;
		input.defaultProviderType = ProviderType.RESULT;
		db.insert(q, Q);

		var sys = ProductSystem.of("sys", Q);
		sys.link(TechFlow.of(result), Q);
		db.insert(sys);

		var usages = resultUsages();
		assertRefs(usages, Q, sys);
		db.delete(sys, Q, q);
	}

	@Test
	public void testEpdUsages() {
		var epd = Epd.of("EPD", product);
		var mod = EpdModule.of("mod", result);
		epd.modules.add(mod);
		db.insert(epd);

		var usages = resultUsages();
		assertRefs(usages, epd);

		db.delete(epd);
	}

	private ArrayList<ModelReference> resultUsages() {
		var refs = ModelReferences.scan(db);
		var resultRef = new TypedRefId(ModelType.RESULT, result.refId);
		var usages = new ArrayList<ModelReference>();
		refs.iterateUsages(resultRef, ref -> {
			usages.add(ref);
		});
		return usages;
	}

	private void assertRefs(List<ModelReference> refs, RootEntity... es) {
		assertEquals(refs.size(), es.length);
		for (var ref : refs) {
			boolean found = false;
			for (var e : es) {
				if (ModelType.of(e) == ref.type
						&& e.refId.equals(ref.refId)
						&& e.id == ref.id) {
					found = true;
				}
			}
			assertTrue("Could not find expected ref " + ref, found);
		}
	}

}
