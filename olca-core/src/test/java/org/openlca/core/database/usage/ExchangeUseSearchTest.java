package org.openlca.core.database.usage;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

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
	private Stack<CategorizedEntity> modelStack = new Stack<>();
	private IDatabase database = Tests.getDb();

	@Before
	public void setUp() {
		p = new Process();
		q = new Process();
		addExchanges();
		ProcessDao dao = new ProcessDao(database);
		p = dao.insert(p);
		q = dao.insert(q);
		modelStack.push(p);
		modelStack.push(q);
		createSystem();
	}

	private void addExchanges() {
		for (int i = 1; i < 4; i++) {
			Flow flow = new Flow();
			flow.setName("flow_" + 1);
			flow = database.createDao(Flow.class).insert(flow);
			modelStack.push(flow);
			Exchange ep = new Exchange();
			final Flow flow1 = flow;
			ep.flow = flow1;
			ep.isInput = false;
			p.getExchanges().add(ep);
			Exchange eq = ep.clone();
			eq.isInput = true;
			q.getExchanges().add(eq);
		}
	}

	private void createSystem() {
		ProductSystem system = new ProductSystem();
		system.setName(SYS_NAME);
		system.setReferenceProcess(p);
		system.setReferenceExchange(p.getExchanges().get(0));
		Flow linkFlow = p.getExchanges().get(1).flow;
		ProcessLink link = new ProcessLink();
		link.providerId = p.getId();
		link.processId = q.getId();
		link.flowId = linkFlow.getId();
		system.getProcessLinks().add(link);
		system = database.createDao(ProductSystem.class).insert(system);
		modelStack.push(system);
	}

	@After
	public void tearDown() {
		while (!modelStack.isEmpty()) {
			CategorizedEntity entity = modelStack.pop();
			@SuppressWarnings("unchecked")
			BaseDao<CategorizedEntity> dao = (BaseDao<CategorizedEntity>) database
					.createDao(entity.getClass());
			dao.delete(entity);
		}
	}

	@Test
	public void testSingleFindNothing() {
		ExchangeUseSearch search = new ExchangeUseSearch(database, p);
		List<CategorizedDescriptor> list = search.findUses(p.getExchanges().get(2));
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testMultiFindNothing() {
		ExchangeUseSearch search = new ExchangeUseSearch(database, q);
		List<Exchange> exchanges = Arrays.asList(
				q.getExchanges().get(0), q.getExchanges().get(2));
		List<CategorizedDescriptor> list = search.findUses(exchanges);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testFindInReference() {
		ExchangeUseSearch search = new ExchangeUseSearch(database, p);
		List<CategorizedDescriptor> list = search.findUses(p.getExchanges().get(0));
		Assert.assertEquals(list.get(0).getName(), SYS_NAME);
		Assert.assertEquals(list.size(), 1);
	}

	@Test
	public void testFindInLinks() {
		ExchangeUseSearch search = new ExchangeUseSearch(database, p);
		List<CategorizedDescriptor> list = search.findUses(p.getExchanges().get(1));
		Assert.assertEquals(list.get(0).getName(), SYS_NAME);
		Assert.assertEquals(list.size(), 1);
	}

	@Test
	public void testFindAllDistinct() {
		ExchangeUseSearch search = new ExchangeUseSearch(database, p);
		List<CategorizedDescriptor> list = search.findUses(p.getExchanges());
		Assert.assertEquals(list.get(0).getName(), SYS_NAME);
		Assert.assertEquals(list.size(), 1);
	}

}
