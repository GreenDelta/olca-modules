package org.openlca.jsonld.io;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.jsonld.output.JsonExport;

public class ProcessTest extends AbstractZipTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProcess() throws Exception {
		ProcessDao dao = new ProcessDao(db);
		Process process = createSimpleModel(dao);
		doExport(process);
		dao.delete(process);
		doImport();
		assertTestProcess(process, dao);
		delete(process, Tests.getDb());
	}

	private void assertTestProcess(Process process, ProcessDao dao) {
		Assert.assertTrue(dao.contains(process.refId));
		Process clone = dao.getForRefId(process.refId);
		Assert.assertEquals(process.name, clone.name);
		Assert.assertNotEquals(process.id, clone.id);
	}

	private Process createSimpleModel(ProcessDao dao) {
		Process process = new Process();
		process.name = "process";
		process.refId = UUID.randomUUID().toString();
		dao.insert(process);
		return process;
	}

	@Test
	public void testCyclicProvider() {
		IDatabase db = Tests.getDb();
		ProcessDao dao = new ProcessDao(db);
		Process[] processes = createCyclicModel(db);
		doExport(processes[0]);
		dao.delete(processes[0]);
		dao.delete(processes[1]);
		doImport();
		assertTestCyclicProvider(processes, dao);
		delete(processes[0], Tests.getDb());
		delete(processes[1], Tests.getDb());
	}

	private void assertTestCyclicProvider(Process[] processes, ProcessDao dao) {
		List<Process> clones = new ArrayList<>();
		for (int i = 0; i < processes.length; i++) {
			Process process = processes[i];
			Assert.assertTrue(dao.contains(process.refId));
			Process clone = dao.getForRefId(process.refId);
			Assert.assertEquals(process.name, clone.name);
			Assert.assertNotEquals(process.id, clone.id);
			clones.add(i, clone);
		}
		Exchange in = null;
		for (Exchange e : clones.get(1).exchanges) {
			if (e.isInput) {
				in = e;
			}
		}
		Assert.assertNotNull(in);
		Assert.assertEquals(clones.get(0).id, in.defaultProviderId);
		for (Exchange e : clones.get(0).exchanges)
			if (e.isInput)
				in = e;
		Assert.assertEquals(clones.get(1).id, in.defaultProviderId);
	}

	private Process[] createCyclicModel(IDatabase db) {
		UnitGroup ug = createUnitGroup(new UnitGroupDao(db));
		FlowProperty fp = createFlowProperty(ug, new FlowPropertyDao(db));
		Flow product1 = createProduct(fp, new FlowDao(db));
		Flow product2 = createProduct(fp, new FlowDao(db));
		ProcessDao dao = new ProcessDao(db);
		Process p1 = createProcess(product1, dao);
		Process p2 = createProcess(product2, dao);
		p1 = addProvider(p1, p2, dao);
		p2 = addProvider(p2, p1, dao);
		return new Process[] { p1, p2 };
	}

	private FlowProperty createFlowProperty(UnitGroup ug, FlowPropertyDao dao) {
		FlowProperty fp = new FlowProperty();
		fp.name = "flow property";
		fp.refId = UUID.randomUUID().toString();
		fp.unitGroup = ug;
		return dao.insert(fp);
	}

	private UnitGroup createUnitGroup(UnitGroupDao dao) {
		UnitGroup ug = new UnitGroup();
		ug.name = "unit group";
		ug.refId = UUID.randomUUID().toString();
		Unit u = new Unit();
		u.name = "unit";
		u.refId = UUID.randomUUID().toString();
		ug.units.add(u);
		ug.referenceUnit = u;
		return dao.insert(ug);
	}

	private Flow createProduct(FlowProperty fp, FlowDao dao) {
		Flow product = new Flow();
		product.name = "product";
		product.flowType = FlowType.PRODUCT_FLOW;
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.flowProperty = fp;
		product.flowPropertyFactors.add(factor);
		product.referenceFlowProperty = factor.flowProperty;
		return dao.insert(product);
	}

	private Process createProcess(Flow product, ProcessDao dao) {
		Process p = new Process();
		p.name = "process";
		p.refId = UUID.randomUUID().toString();
		Exchange out = createExchange(p, product, null);
		out.isInput = false;
		p.exchanges.add(out);
		p.quantitativeReference = out;
		return dao.insert(p);
	}

	private Exchange createExchange(Process process, Flow product, Process provider) {
		Exchange out = process.add(Exchange.of(product));
		if (provider != null)
			out.defaultProviderId = provider.id;
		return out;
	}

	private Process addProvider(Process p, Process provider, ProcessDao dao) {
		Exchange in = createExchange(p, provider.quantitativeReference.flow, provider);
		in.isInput = true;
		p.exchanges.add(in);
		return dao.update(p);
	}

	private void doExport(Process process) {
		with(zip -> {
			new JsonExport(Tests.getDb(), zip)
				.withDefaultProviders(true)
				.write(process);
		});
	}

	private void doImport() {
		with(zip -> new JsonImport(zip, Tests.getDb())
			.setUpdateMode(UpdateMode.ALWAYS)
			.run());
	}

	private void delete(Process p, IDatabase db) {
		new ProcessDao(db).delete(p);
		for (Exchange e : p.exchanges)
			new FlowDao(db).delete(e.flow);
		for (Exchange e : p.exchanges)
			new FlowPropertyDao(db).delete(e.flowPropertyFactor.flowProperty);
		for (Exchange e : p.exchanges)
			new UnitGroupDao(db).delete(e.flowPropertyFactor.flowProperty.unitGroup);
	}

}
