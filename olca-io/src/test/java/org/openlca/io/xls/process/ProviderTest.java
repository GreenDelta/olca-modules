package org.openlca.io.xls.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.store.InMemoryStore;
import org.openlca.io.Tests;
import org.openlca.util.Dirs;

public class ProviderTest {

	private final IDatabase db = Tests.getDb();

	@Before
	public void setup() throws IOException {
		var store = InMemoryStore.create();
		var mass = ProcTests.createMass(store);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var provider = Process.of("P", p);
		var receiver = Process.of("Q", q);
		var input = receiver.input(p, 1);

		store.insert(p, q, provider, receiver); // assigns IDs
		input.defaultProviderId = provider.id;

		var tempDir = Files.createTempDirectory("_olca_").toFile();
		XlsProcessWriter.of(store).writeAllToFolder(
			Stream.of(provider, receiver).map(Descriptor::of).toList(),
			tempDir);
		XlsProcessReader.of(db).syncAllFromFolder(tempDir);
		Dirs.delete(tempDir);
	}

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testProviderLink() {
		var provider = db.getForName(Process.class, "P");
		var receiver = db.getForName(Process.class, "Q");
		assertNotNull(provider);
		assertNotNull(receiver);

		var input = receiver.exchanges.stream()
			.filter(e -> e.flow.name.equals("p"))
			.findAny()
			.orElseThrow();
		assertTrue(input.isInput);
		assertEquals(provider.id, input.defaultProviderId);
	}

}
