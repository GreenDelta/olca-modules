package org.openlca.jsonld.io;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
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
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.jsonld.output.JsonExport;

public class ProcessTest extends AbstractZipTest {

	@Test
	public void testProcess() throws Exception {
		ProcessDao dao = new ProcessDao(Tests.getDb());
		Process process = createSimpleModel(dao);
		doExport(process);
		dao.delete(process);
		doImport();
		assertTestProcess(process, dao);
		delete(process, Tests.getDb());
	}

	private void assertTestProcess(Process process, ProcessDao dao) {
		Assert.assertTrue(dao.contains(process.getRefId()));
		Process clone = dao.getForRefId(process.getRefId());
		Assert.assertEquals(process.getName(), clone.getName());
		Assert.assertNotEquals(process.getId(), clone.getId());
	}

	private Process createSimpleModel(ProcessDao dao) {
		Process process = new Process();
		process.setName("process");
		process.setRefId(UUID.randomUUID().toString());
		dao.insert(process);
		return process;
	}

	@Test
	public void testCyclicProvider() throws Exception {
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
			Assert.assertTrue(dao.contains(process.getRefId()));
			Process clone = dao.getForRefId(process.getRefId());
			Assert.assertEquals(process.getName(), clone.getName());
			Assert.assertNotEquals(process.getId(), clone.getId());
			clones.add(i, clone);
		}
		Exchange in = null;
		for (Exchange e : clones.get(1).getExchanges())
			if (e.isInput)
				in = e;
		Assert.assertEquals(clones.get(0).getId(), in.defaultProviderId);
		for (Exchange e : clones.get(0).getExchanges())
			if (e.isInput)
				in = e;
		Assert.assertEquals(clones.get(1).getId(), in.defaultProviderId);
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
		fp.setName("flow property");
		fp.setRefId(UUID.randomUUID().toString());
		fp.setUnitGroup(ug);
		return dao.insert(fp);
	}

	private UnitGroup createUnitGroup(UnitGroupDao dao) {
		UnitGroup ug = new UnitGroup();
		ug.setName("unit group");
		ug.setRefId(UUID.randomUUID().toString());
		Unit u = new Unit();
		u.setName("unit");
		u.setRefId(UUID.randomUUID().toString());
		ug.getUnits().add(u);
		ug.setReferenceUnit(u);
		return dao.insert(ug);
	}

	private Flow createProduct(FlowProperty fp, FlowDao dao) {
		Flow product = new Flow();
		product.setName("product");
		product.setFlowType(FlowType.PRODUCT_FLOW);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(fp);
		product.getFlowPropertyFactors().add(factor);
		product.setReferenceFlowProperty(factor.getFlowProperty());
		return dao.insert(product);
	}

	private Process createProcess(Flow product, ProcessDao dao) {
		Process p = new Process();
		p.setName("process");
		p.setRefId(UUID.randomUUID().toString());
		Exchange out = createExchange(product, null);
		out.isInput = false;
		p.getExchanges().add(out);
		p.setQuantitativeReference(out);
		return dao.insert(p);
	}

	private Exchange createExchange(Flow product, Process provider) {
		Exchange out = new Exchange();
		out.amount = (double) 1;
		final Flow flow = product;
		out.flow = flow;
		out.flowPropertyFactor = product.getReferenceFactor();
		out.unit = product.getReferenceFactor().getFlowProperty()
		.getUnitGroup().getReferenceUnit();
		if (provider != null)
			out.defaultProviderId = provider.getId();
		return out;
	}

	private Process addProvider(Process p, Process provider, ProcessDao dao) {
		Exchange in = createExchange(provider.getQuantitativeReference().flow, provider);
		in.isInput = true;
		p.getExchanges().add(in);
		return dao.update(p);
	}

	private void doExport(Process process) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.setExportDefaultProviders(true);
			export.write(process);
		});
	}

	private void doImport() {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.setUpdateMode(UpdateMode.ALWAYS);
			jImport.run();
		});
	}

	private void delete(Process p, IDatabase db) {
		new ProcessDao(db).delete(p);
		for (Exchange e : p.getExchanges())
			new FlowDao(db).delete(e.flow);
		for (Exchange e : p.getExchanges())
			new FlowPropertyDao(db).delete(e.flowPropertyFactor
					.getFlowProperty());
		for (Exchange e : p.getExchanges())
			new UnitGroupDao(db).delete(e.flowPropertyFactor
					.getFlowProperty().getUnitGroup());
	}

}
