package org.openlca.jsonld.output;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;

/// Tests how the export of default providers can be controlled. When
/// enabling the export of default providers (recursively), it can
/// lead to a huge amount of processes that are exported in databases
/// like ecoinvent, even if the user just wanted to export a single
/// foreground process. Also, results and even product systems could
/// be linked as default providers. Depending on the export
/// configuration these providers are exported or not.
public class DefaultProviderExportTest {

	private final IDatabase db = Tests.getDb();
	private final MemStore store = new MemStore();
	private Process root;

	@Before
	public void setup() {

		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		db.insert(units, mass);

		// process P
		var p = Flow.product("p", mass);
		var P = Process.of("P", p);
		db.insert(p, P);

		// result R
		var r = Flow.product("r", mass);
		var R = Result.of("R", r);
		db.insert(r, R);

		// product system S
		var s = Flow.product("s", mass);
		var PS = Process.of("PS", s);
		db.insert(s, PS);
		var S = ProductSystem.of("S", PS);
		db.insert(S);

		// result T that is linked to a product system Q
		var t = Flow.product("t", mass);
		var PT = Process.of("PT", t);
		db.insert(t, PT);
		var Q = ProductSystem.of("Q", PT);
		db.insert(Q);
		var T = Result.of("T", t);
		T.productSystem = Q;
		db.insert(T);

		// the Root process
		var rootFlow = Flow.product("rootf", mass);
		root = Process.of("Root", rootFlow);
		linkProvider(root, p, P);
		linkProvider(root, r, R);
		linkProvider(root, s, PS);
		linkProvider(root, t, T);
		db.insert(rootFlow, root);
	}

	private void linkProvider(Process root, Flow flow, RootEntity provider) {
		byte type = switch (provider) {
			case Result ignored -> ProviderType.RESULT;
			case ProductSystem ignored -> ProviderType.SUB_SYSTEM;
			default -> ProviderType.PROCESS;
		};
		var input = root.input(flow, 0.5);
		input.defaultProviderId = provider.id;
		input.defaultProviderType = type;
	}


	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testNoReferences() {
		doIO(new JsonExport(db, store).withReferences(false));
		hasNot(Process.class, "P");
		hasNot(Result.class, "R");
		hasNot(ProductSystem.class, "S");
		hasNot(Result.class, "T");
	}

	@Test
	public void testNoDefaultProviders() {
		doIO(new JsonExport(db, store).withDefaultProviders(false));
		// it should contain the result R, because this has no
		// reference to product system, and thus can be exported
		// without running into many recursions

		hasNot(Process.class, "P");
		has(Result.class, "R");
		hasNot(ProductSystem.class, "S");
		hasNot(Result.class, "T");
	}

	@Test
	public void testWithDefaultProviders() {
		doIO(new JsonExport(db, store).withDefaultProviders(true));
		has(Process.class, "P");
		has(Result.class, "R");
		has(ProductSystem.class, "S");
		has(Result.class, "T");
	}

	private void doIO(JsonExport export) {
		export.write(root);
		db.clear();
		new JsonImport(store, db)
				.run();
		root = db.get(Process.class, root.refId);
		// with all configurations, the root process should be there
		assertNotNull(root);
	}

	private void has(Class<? extends RootEntity> type, String name) {
		var e = db.getForName(type, name);
		assertNotNull(e);
	}

	private void hasNot(Class<? extends RootEntity> type, String name) {
		var e = db.getForName(type, name);
		assertNull(e);
	}

}
