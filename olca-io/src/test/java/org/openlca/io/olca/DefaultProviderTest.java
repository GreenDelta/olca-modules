package org.openlca.io.olca;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.Tests;

public class DefaultProviderTest {

	private final IDatabase source = Tests.getDb();
	private final IDatabase target = Derby.createInMemory();

	@Before
	public void setup() {

		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		source.insert(units, mass);

		// process P
		var p = Flow.product("p", mass);
		var P = Process.of("P", p);
		source.insert(p, P);

		// result R
		var r = Flow.product("r", mass);
		var R = Result.of("R", r);
		source.insert(r, R);

		// product system S
		var s = Flow.product("s", mass);
		var PS = Process.of("PS", s);
		source.insert(s, PS);
		var S = ProductSystem.of("S", PS);
		source.insert(S);

		// result T that is linked to a product system Q
		var t = Flow.product("t", mass);
		var PT = Process.of("PT", t);
		source.insert(t, PT);
		var Q = ProductSystem.of("Q", PT);
		source.insert(Q);
		var T = Result.of("T", t);
		T.productSystem = Q;
		source.insert(T);

		// the Root process
		var rootFlow = Flow.product("rootf", mass);
		Process root = Process.of("Root", rootFlow);
		linkProvider(root, p, P);
		linkProvider(root, r, R);
		linkProvider(root, s, S);
		linkProvider(root, t, T);
		source.insert(rootFlow, root);
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
	public void cleanup() throws IOException {
		source.clear();
		target.close();
	}

	@Test
	public void testDefaultProviders() {
		new DatabaseImport(source, target).run();

		var root = target.getForName(Process.class, "Root");
		assertNotNull(root);

		// check that default providers are set correctly
		var P = target.getForName(Process.class, "P");
		var R = target.getForName(Result.class, "R");
		var S = target.getForName(ProductSystem.class, "S");
		var T = target.getForName(Result.class, "T");

		for (var provider : List.of(P, R, S, T)) {
			assertNotNull(provider);
			boolean found = false;
			byte type = ProviderType.of(ModelType.of(provider));
			for (var e : root.exchanges) {
				if (e.defaultProviderId == provider.id
						&& e.defaultProviderType == type) {
					found = true;
					break;
				}
			}
			assertTrue("Default provider not found: " + provider, found);
		}

		// also check that the referenced datasets of the providers are
		// present in the target database
		var PS = target.getForName(Process.class, "PS");
		var PT = target.getForName(Process.class, "PT");
		var Q = target.getForName(ProductSystem.class, "Q");
		for (var dep : List.of(PS, PT, Q)) {
			assertNotNull(dep);
		}

	}
}
