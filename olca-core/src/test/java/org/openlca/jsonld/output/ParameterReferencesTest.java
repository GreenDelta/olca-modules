package org.openlca.jsonld.output;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Uncertainty;
import org.openlca.jsonld.AbstractZipTest;

public class ParameterReferencesTest extends AbstractZipTest {

	private final IDatabase db = Tests.getDb();

	@After
	public void clearDb() {
		db.clear();
	}

	@Test
	public void testParameter() {
		var p1 = db.insert(Parameter.global("p1", "2*2"));
		var p2 = db.insert(Parameter.global("p2", "2*p1"));
		with(store -> {
			new JsonExport(db, store).write(p2);
			assertNotNull(store.get(ModelType.PARAMETER, p1.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p2.refId));
		});
	}

	@Test
	public void testExchange() {
		var p1 = db.insert(Parameter.global("p1", 1));
		var p1Intern = Parameter.process("p1", 1);
		var p2 = db.insert(Parameter.global("p2", 1));
		var e1 = exchangeOf("2*p1");
		var e2 = exchangeOf("2*p2");
		var process = createProcess(new Exchange[]{e1, e2},
			new Parameter[]{p1Intern});
		with((store) -> {
			new JsonExport(db, store).write(process);
			assertNotNull(store.get(ModelType.PROCESS, process.refId));
			assertNull(store.get(ModelType.PARAMETER, p1.refId));
			assertNull(store.get(ModelType.PARAMETER, p1Intern.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p2.refId));
		});
	}

	@Test
	public void testProcessParameter() {
		var p1Global = db.insert(Parameter.global("p1", "2*2"));
		var p1 = Parameter.process("p1", "2*2");
		var p2 = Parameter.process("p2", "2*p1*p3");
		var p3Global = db.insert(Parameter.global("p3", "2*p4"));
		var p4Global = db.insert(Parameter.global("p4", "2"));
		var process = createProcess(null, new Parameter[]{p1, p2});
		with((store) -> {
			new JsonExport(db, store).write(process);
			assertNotNull(store.get(ModelType.PROCESS, process.refId));
			assertNull(store.get(ModelType.PARAMETER, p1.refId));
			assertNull(store.get(ModelType.PARAMETER, p1Global.refId));
			assertNull(store.get(ModelType.PARAMETER, p2.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p3Global.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p4Global.refId));
		});
	}

	@Test
	public void testImpactFactor() {
		var p1 = db.insert(Parameter.global("p1", 1));
		var p1Intern = Parameter.process("p1",1);
		var p2 = db.insert(Parameter.global("p2", 1));
		var f1 = impactFactorOf("2*p1");
		var f2 = impactFactorOf("2*p2");
		var impact = impactCategoryOf(
			new ImpactFactor[]{f1, f2}, new Parameter[]{p1Intern});
		with((store) -> {
			JsonExport export = new JsonExport(db, store);
			export.write(impact);
			assertNotNull(store.get(ModelType.IMPACT_CATEGORY, impact.refId));
			assertNull(store.get(ModelType.PARAMETER, p1.refId));
			assertNull(store.get(ModelType.PARAMETER, p1Intern.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p2.refId));
		});
	}

	@Test
	public void testImpactMethodParameter() {
		Parameter p1Global = createParameter("p1", "2*2", null, null);
		Parameter p1 = createParameter("p1", "2*2", ParameterScope.PROCESS, null);
		Parameter p2 = createParameter("p2", "2*p1*p3", ParameterScope.PROCESS, null);
		Parameter p3Global = createParameter("p3", "2*p4", null, null);
		Parameter p4Global = createParameter("p4", "2", null, null);
		ImpactCategory impact = impactCategoryOf(
			null, new Parameter[]{p1, p2});
		with((store) -> {
			JsonExport export = new JsonExport(db, store);
			export.write(impact);
			assertNotNull(store.get(ModelType.IMPACT_CATEGORY, impact.refId));
			assertNull(store.get(ModelType.PARAMETER, p1.refId));
			assertNull(store.get(ModelType.PARAMETER, p1Global.refId));
			assertNull(store.get(ModelType.PARAMETER, p2.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p3Global.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p4Global.refId));
		});
	}

	@Test
	public void testProductSystemRedef() {
		Parameter p1 = createParameter("p1", "3", null, null);
		Parameter p2 = createParameter("p2", "3", ParameterScope.PROCESS, null);
		ParameterRedef redef1 = createRedef(p1);
		ParameterRedef redef2 = createRedef(p2);
		ProductSystem system = createSystem(redef1, redef2);
		with((store) -> {
			JsonExport export = new JsonExport(db, store);
			export.write(system);
			assertNotNull(store.get(ModelType.PRODUCT_SYSTEM,
				system.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p1.refId));
			assertNull(store.get(ModelType.PARAMETER, p2.refId));
		});
	}

	@Test
	public void testProjectRedef() {
		Parameter p1 = createParameter("p1", "3", null, null);
		Parameter p2 = createParameter("p2", "3", ParameterScope.PROCESS, null);
		ParameterRedef redef1 = createRedef(p1);
		ParameterRedef redef2 = createRedef(p2);
		Project project = createProject(redef1, redef2);
		with((store) -> {
			JsonExport export = new JsonExport(db, store);
			export.write(project);
			assertNotNull(store.get(ModelType.PROJECT, project.refId));
			assertNotNull(store.get(ModelType.PARAMETER, p1.refId));
			assertNull(store.get(ModelType.PARAMETER, p2.refId));
		});
	}

	private Parameter createParameter(String name, String formula,
		ParameterScope scope, Uncertainty u) {
		Parameter p = new Parameter();
		p.name = name;
		p.value = 1;
		if (formula != null) {
			p.isInputParameter = false;
			p.formula = formula;
		} else
			p.isInputParameter = true;
		if (scope != null)
			p.scope = scope;
		else
			p.scope = ParameterScope.GLOBAL;
		p.refId = UUID.randomUUID().toString();
		p.uncertainty = u;
		if (p.scope != ParameterScope.GLOBAL)
			return p;
		return new ParameterDao(db).insert(p);
	}

	private Exchange exchangeOf(String formula) {
		Exchange e = new Exchange();
		e.formula = formula;
		return e;
	}

	private Process createProcess(Exchange[] exchanges, Parameter[] parameters) {
		Process p = new Process();
		p.refId = UUID.randomUUID().toString();
		if (parameters != null)
			p.parameters.addAll(Arrays.asList(parameters));
		if (exchanges != null)
			p.exchanges.addAll(Arrays.asList(exchanges));
		return p;
	}

	private ImpactFactor impactFactorOf(String formula) {
		ImpactFactor f = new ImpactFactor();
		f.formula = formula;
		return f;
	}

	private ImpactCategory impactCategoryOf(ImpactFactor[] factors,
		Parameter[] parameters) {
		ImpactCategory c = new ImpactCategory();
		c.refId = UUID.randomUUID().toString();
		if (parameters != null) {
			c.parameters.addAll(Arrays.asList(parameters));
		}
		if (factors != null) {
			c.impactFactors.addAll(Arrays.asList(factors));
		}
		return c;
	}

	private ParameterRedef createRedef(Parameter p) {
		ParameterRedef redef = new ParameterRedef();
		redef.name = p.name;
		if (p.scope == ParameterScope.PROCESS)
			redef.contextType = ModelType.PROCESS;
		else if (p.scope == ParameterScope.IMPACT)
			redef.contextType = ModelType.IMPACT_CATEGORY;
		if (p.scope != ParameterScope.GLOBAL)
			redef.contextId = 1L;
		redef.value = 1;
		return redef;
	}

	private ProductSystem createSystem(ParameterRedef... redefs) {
		var system = new ProductSystem();
		system.refId = UUID.randomUUID().toString();
		system.parameterSets.add(ParameterRedefSet.of("baseline", redefs));
		return system;
	}

	private Project createProject(ParameterRedef... redefs) {
		Project p = new Project();
		p.refId = UUID.randomUUID().toString();
		ProjectVariant v = new ProjectVariant();
		p.variants.add(v);
		v.parameterRedefs.addAll(Arrays.asList(redefs));
		return p;
	}

}
