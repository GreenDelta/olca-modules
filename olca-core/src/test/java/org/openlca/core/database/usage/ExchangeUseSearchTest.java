package org.openlca.core.database.usage;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

/**
 * Creates a simple product system with 2 processes p and q. Process p has 3
 * outputs and process q has 3 inputs with the same flows. The product system
 * has the first output of p as quantitative reference. The second output of p
 * and the second input of q are linked in this product system. The tests search
 * for the usage of the exchanges of p.
 */
public class ExchangeUseSearchTest {

	private static final String SYS_NAME = "ExchangeUseSearchTest_System";

	private final IDatabase db = Tests.getDb();
	private final List<RootEntity> created = new ArrayList<>();

	private FlowProperty mass;
	private Process p;
	private Process q;

	@Before
	public void setUp() {
		var units = insert(UnitGroup.of("Units of mass", "kg"));
		mass = insert(FlowProperty.of("Mass", units));
		var f1 = insert(Flow.product("f1", mass));
		var f2 = insert(Flow.product("f2", mass));
		var f3 = insert(Flow.product("f3", mass));

		// p has 3 outputs; the first one is the quantitative reference
		p = Process.of("p", f1);
		p.output(f2, 1);
		p.output(f3, 1);
		p = insert(p);

		// q has 3 inputs with the same flows as the outputs of p
		q = Process.of("q", null);
		q.input(f1, 1);
		q.input(f2, 1);
		q.input(f3, 1);
		q = insert(q);

		// the second output of p is linked with the second input of q
		var system = ProductSystem.of(SYS_NAME, p);
		system.link(TechFlow.of(p, f2), q);
		insert(system);
	}

	@After
	public void tearDown() {
		for (int i = created.size() - 1; i >= 0; i--) {
			db.delete(created.get(i));
		}
		created.clear();
	}

	private <T extends RootEntity> T insert(T e) {
		created.add(e);
		return db.insert(e);
	}

	@Test
	public void testSingleFindNothing() {
		var search = new ExchangeUseSearch(db, p);
		assertTrue(search.findUses(p.exchanges.get(2)).isEmpty());
	}

	@Test
	public void testMultiFindNothing() {
		var search = new ExchangeUseSearch(db, q);
		var exchanges = List.of(q.exchanges.get(0), q.exchanges.get(2));
		assertTrue(search.findUses(exchanges).isEmpty());
	}

	@Test
	public void testFindInReference() {
		var search = new ExchangeUseSearch(db, p);
		var uses = search.findUses(p.exchanges.getFirst());
		assertEquals(1, uses.size());
		assertEquals(SYS_NAME, uses.getFirst().name);
	}

	@Test
	public void testFindInLinks() {
		var search = new ExchangeUseSearch(db, p);
		var uses = search.findUses(p.exchanges.get(1));
		assertEquals(1, uses.size());
		assertEquals(SYS_NAME, uses.getFirst().name);
	}

	@Test
	public void testFindAllDistinct() {
		var search = new ExchangeUseSearch(db, p);
		var uses = search.findUses(p.exchanges);
		assertEquals(1, uses.size());
		assertEquals(SYS_NAME, uses.getFirst().name);
	}

	@Test
	public void testDuplicateFlowInput() {
		var flow = insert(Flow.product("dup", mass));
		var provider = insert(Process.of("provider", flow));

		// the consumer has two inputs of the same flow; only the first one
		// is linked to the provider of that flow
		var consumer = Process.of(
			"consumer", insert(Flow.product("p", mass)));
		consumer.input(flow, 1).description = "A";
		consumer = insert(consumer);

		var system = ProductSystem.of("duplicate-flow-system", consumer);
		system.link(TechFlow.of(provider, flow), consumer);
		system = insert(system);

		// add a second input of the same flow that is not linked
		consumer.input(flow, 1).description = "B";
		consumer = db.update(consumer);

		var search = new ExchangeUseSearch(db, consumer);
		var linked = consumer.exchanges.stream()
			.filter(e -> "A".equals(e.description))
			.findAny()
			.orElseThrow();
		var unlinked = consumer.exchanges.stream()
			.filter(e -> "B".equals(e.description))
			.findAny()
			.orElseThrow();

		// the linked input is found in the product system
		var uses = search.findUses(linked);
		assertEquals(1, uses.size());
		assertEquals(system.refId, uses.getFirst().refId);

		// the unlinked input is not used, even though it has the same flow
		assertTrue(search.findUses(unlinked).isEmpty());
	}

}
