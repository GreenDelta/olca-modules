package org.openlca.core.database.usage;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Creates a simple product system with 2 processes p and q. Process p has 3
 * outputs and process q has 3 inputs with the same flows. The product system
 * has the first output of p as quantitative reference. The second output of p
 * and the second input of q are linked in this product system. The tests search
 * for the the usage of the exchanges of p.
 */
public class ExchangeUseSearchTest {

	private final String SYS_NAME = "ExchangeUseSearchTest_System";

	private Process p;
	private Process q;
	private final Stack<RootEntity> entities = new Stack<>();
	private final IDatabase db = Tests.getDb();

	@Before
	public void setUp() {
		p = new Process();
		q = new Process();
		addExchanges();
		ProcessDao dao = new ProcessDao(db);
		p = dao.insert(p);
		q = dao.insert(q);
		entities.push(p);
		entities.push(q);
		createSystem();
	}

	@After
	public void tearDown() {
		while (!entities.isEmpty()) {
			var entity = entities.pop();
			db.delete(entity);
		}
	}

	private void addExchanges() {
		for (int i = 1; i < 4; i++) {
			Flow flow = new Flow();
			flow.name = "flow_" + 1;
			flow = db.insert(flow);
			entities.push(flow);
			Exchange ep = new Exchange();
			ep.flow = flow;
			ep.isInput = false;
			p.exchanges.add(ep);
			Exchange eq = ep.copy();
			eq.isInput = true;
			q.exchanges.add(eq);
		}
	}

	private void createSystem() {
		ProductSystem system = new ProductSystem();
		system.name = SYS_NAME;
		system.referenceProcess = p;
		system.referenceExchange = p.exchanges.get(0);
		Flow linkFlow = p.exchanges.get(1).flow;
		ProcessLink link = new ProcessLink();
		link.providerId = p.id;
		link.processId = q.id;
		link.flowId = linkFlow.id;
		system.processLinks.add(link);
		system = new ProductSystemDao(db).insert(system);
		entities.push(system);
	}

	@Test
	public void testSingleFindNothing() {
		ExchangeUseSearch search = new ExchangeUseSearch(db, p);
		List<RootDescriptor> list = search.findUses(p.exchanges.get(2));
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testMultiFindNothing() {
		ExchangeUseSearch search = new ExchangeUseSearch(db, q);
		List<Exchange> exchanges = Arrays.asList(
				q.exchanges.get(0), q.exchanges.get(2));
		List<RootDescriptor> list = search.findUses(exchanges);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testFindInReference() {
		ExchangeUseSearch search = new ExchangeUseSearch(db, p);
		List<RootDescriptor> list = search.findUses(p.exchanges.get(0));
		Assert.assertEquals(list.get(0).name, SYS_NAME);
		Assert.assertEquals(list.size(), 1);
	}

	@Test
	public void testFindInLinks() {
		ExchangeUseSearch search = new ExchangeUseSearch(db, p);
		List<RootDescriptor> list = search.findUses(p.exchanges.get(1));
		Assert.assertEquals(list.get(0).name, SYS_NAME);
		Assert.assertEquals(list.size(), 1);
	}

	@Test
	public void testFindAllDistinct() {
		ExchangeUseSearch search = new ExchangeUseSearch(db, p);
		List<RootDescriptor> list = search.findUses(p.exchanges);
		Assert.assertEquals(list.get(0).name, SYS_NAME);
		Assert.assertEquals(list.size(), 1);
	}

}
