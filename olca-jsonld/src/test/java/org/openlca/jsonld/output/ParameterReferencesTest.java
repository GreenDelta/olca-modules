package org.openlca.jsonld.output;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Uncertainty;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;

public class ParameterReferencesTest extends AbstractZipTest {

	@After
	public void clearDb() {
		Tests.clearDb();
	}
	
	@Test
	public void testParameter() {
		Parameter p1 = createParameter("p1", "2*2", null, null);
		Parameter p2 = createParameter("p2", "2*p1", null, null);
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(p2);
			assertTrue(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p2.getRefId()));
		});
	}

	@Test
	public void testParameterUncertainty() {
		Uncertainty u2 = createUncertainty("1", "2", "3*p4");
		Uncertainty u4 = createUncertainty("p1", "2*p1", "3*p2");
		Parameter p1 = createParameter("p1", null, null, null);
		Parameter p2 = createParameter("p2", "2*2", null, u2);
		Parameter p3 = createParameter("p3", null, null, null);
		Parameter p4 = createParameter("p4", "2*2", null, u4);
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(p4);
			assertTrue(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p2.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p3.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p4.getRefId()));
		});
	}

	@Test
	public void testExchange() {
		Parameter p1 = createParameter("p1", null, null, null);
		Parameter p1Intern = createParameter("p1", null,
				ParameterScope.PROCESS, null);
		Parameter p2 = createParameter("p2", null, null, null);
		Exchange e1 = createExchange("2*p1", null);
		Exchange e2 = createExchange("2*p2", null);
		Process process = createProcess(new Exchange[] { e1, e2 },
				new Parameter[] { p1Intern });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(process);
			assertTrue(store.contains(ModelType.PROCESS, process.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Intern.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p2.getRefId()));
		});
	}

	@Test
	public void testExchangeUncertainty() {
		Parameter p1 = createParameter("p1", null, null, null);
		Parameter p1Intern = createParameter("p1", null,
				ParameterScope.PROCESS, null);
		Parameter p2 = createParameter("p2", null, null, null);
		Uncertainty u1 = createUncertainty("1", "2", "3*p2");
		Uncertainty u2 = createUncertainty("p1", "2*p1", "3*p2");
		Exchange e1 = createExchange("2", u1);
		Exchange e2 = createExchange("2", u2);
		Process process = createProcess(new Exchange[] { e1, e2 },
				new Parameter[] { p1Intern });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(process);
			assertTrue(store.contains(ModelType.PROCESS, process.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Intern.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p2.getRefId()));
		});
	}

	@Test
	public void testProcessParameter() {
		Parameter p1Global = createParameter("p1", "2*2", null, null);
		Parameter p1 = createParameter("p1", "2*2", ParameterScope.PROCESS,
				null);
		Parameter p2 = createParameter("p2", "2*p1*p3", ParameterScope.PROCESS,
				null);
		Parameter p3Global = createParameter("p3", "2*p4", null, null);
		Parameter p4Global = createParameter("p4", "2", null, null);
		Process process = createProcess(null, new Parameter[] { p1, p2 });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(process);
			assertTrue(store.contains(ModelType.PROCESS, process.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Global.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p2.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p3Global.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p4Global.getRefId()));
		});
	}

	@Test
	public void testProcessParameterUncertainty() {
		Uncertainty u1 = createUncertainty("1", "p2", "p3");
		Uncertainty u2 = createUncertainty("p1", "2", "3");
		Parameter p1 = createParameter("p1", null, null, null);
		Parameter p2 = createParameter("p2", null, null, null);
		Parameter p3 = createParameter("p3", null, null, null);
		Parameter p1Intern = createParameter("p1", "2*2",
				ParameterScope.PROCESS, u1);
		Parameter p2Intern = createParameter("p2", "2*2",
				ParameterScope.PROCESS, u2);
		Process process = createProcess(null, new Parameter[] { p1Intern,
				p2Intern });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(process);
			assertTrue(store.contains(ModelType.PROCESS, process.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Intern.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p2.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p2Intern.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p3.getRefId()));
		});
	}

	@Test
	public void testImpactFactor() {
		Parameter p1 = createParameter("p1", null, null, null);
		Parameter p1Intern = createParameter("p1", null,
				ParameterScope.PROCESS, null);
		Parameter p2 = createParameter("p2", null, null, null);
		ImpactFactor f1 = createImpactFactor("2*p1", null);
		ImpactFactor f2 = createImpactFactor("2*p2", null);
		ImpactMethod method = createImpactMethod(new ImpactFactor[] { f1, f2 },
				new Parameter[] { p1Intern });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(method);
			assertTrue(store.contains(ModelType.IMPACT_METHOD,
					method.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Intern.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p2.getRefId()));
		});
	}

	@Test
	public void testImpactFactorUncertainty() {
		Parameter p1 = createParameter("p1", null, null, null);
		Parameter p1Intern = createParameter("p1", null,
				ParameterScope.PROCESS, null);
		Parameter p2 = createParameter("p2", null, null, null);
		Uncertainty u1 = createUncertainty("1", "2", "3*p2");
		Uncertainty u2 = createUncertainty("p1", "2*p1", "3*p2");
		ImpactFactor f1 = createImpactFactor("2", u1);
		ImpactFactor f2 = createImpactFactor("2", u2);
		ImpactMethod method = createImpactMethod(new ImpactFactor[] { f1, f2 },
				new Parameter[] { p1Intern });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(method);
			assertTrue(store.contains(ModelType.IMPACT_METHOD,
					method.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Intern.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p2.getRefId()));
		});
	}

	@Test
	public void testImpactMethodParameter() {
		Parameter p1Global = createParameter("p1", "2*2", null, null);
		Parameter p1 = createParameter("p1", "2*2", ParameterScope.PROCESS,
				null);
		Parameter p2 = createParameter("p2", "2*p1*p3", ParameterScope.PROCESS,
				null);
		Parameter p3Global = createParameter("p3", "2*p4", null, null);
		Parameter p4Global = createParameter("p4", "2", null, null);
		ImpactMethod method = createImpactMethod(null,
				new Parameter[] { p1, p2 });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(method);
			assertTrue(store.contains(ModelType.IMPACT_METHOD,
					method.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Global.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p2.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p3Global.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p4Global.getRefId()));
		});
	}

	@Test
	public void testImpactMethodParameterUncertainty() {
		Uncertainty u1 = createUncertainty("1", "p2", "p3");
		Uncertainty u2 = createUncertainty("p1", "2", "3");
		Parameter p1 = createParameter("p1", null, null, null);
		Parameter p2 = createParameter("p2", null, null, null);
		Parameter p3 = createParameter("p3", null, null, null);
		Parameter p1Intern = createParameter("p1", "2*2",
				ParameterScope.PROCESS, u1);
		Parameter p2Intern = createParameter("p2", "2*2",
				ParameterScope.PROCESS, u2);
		ImpactMethod method = createImpactMethod(null, new Parameter[] {
				p1Intern, p2Intern });
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(method);
			assertTrue(store.contains(ModelType.IMPACT_METHOD,
					method.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p1Intern.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p2.getRefId()));
			assertFalse(store
					.contains(ModelType.PARAMETER, p2Intern.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p3.getRefId()));
		});
	}

	@Test
	public void testProductSystemRedef() {
		Parameter p1 = createParameter("p1", "3", null, null);
		Parameter p2 = createParameter("p2", "3", ParameterScope.PROCESS, null);
		ParameterRedef redef1 = createRedef(p1, null);
		ParameterRedef redef2 = createRedef(p2, null);
		ProductSystem system = createSystem(redef1, redef2);
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(system);
			assertTrue(store.contains(ModelType.PRODUCT_SYSTEM,
					system.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p2.getRefId()));
		});
	}

	@Test
	public void testProjectRedef() {
		Parameter p1 = createParameter("p1", "3", null, null);
		Parameter p2 = createParameter("p2", "3", ParameterScope.PROCESS, null);
		ParameterRedef redef1 = createRedef(p1, null);
		ParameterRedef redef2 = createRedef(p2, null);
		Project project = createProject(redef1, redef2);
		with((store) -> {
			JsonExport export = new JsonExport(Tests.getDb(), store);
			export.write(project);
			assertTrue(store.contains(ModelType.PROJECT, project.getRefId()));
			assertTrue(store.contains(ModelType.PARAMETER, p1.getRefId()));
			assertFalse(store.contains(ModelType.PARAMETER, p2.getRefId()));
		});
	}

	private Parameter createParameter(String name, String formula,
			ParameterScope scope, Uncertainty u) {
		Parameter p = new Parameter();
		p.setName(name);
		p.setValue(1);
		if (formula != null) {
			p.setInputParameter(false);
			p.setFormula(formula);
		} else
			p.setInputParameter(true);
		if (scope != null)
			p.setScope(scope);
		else
			p.setScope(ParameterScope.GLOBAL);
		p.setRefId(UUID.randomUUID().toString());
		p.setUncertainty(u);
		if (p.getScope() != ParameterScope.GLOBAL)
			return p;
		IDatabase db = Tests.getDb();
		return new ParameterDao(db).insert(p);
	}

	private Uncertainty createUncertainty(String f1, String f2, String f3) {
		if (f3 == null) {
			Uncertainty u = Uncertainty.logNormal(1, 1);
			u.setParameter1Formula(f1);
			u.setParameter2Formula(f2);
			return u;
		}
		Uncertainty u = Uncertainty.triangle(1, 1, 1);
		u.setParameter1Formula(f1);
		u.setParameter2Formula(f2);
		u.setParameter3Formula(f3);
		return u;
	}

	private Exchange createExchange(String formula, Uncertainty u) {
		Exchange e = new Exchange();
		e.amountFormula = formula;
		e.uncertainty = u;
		return e;
	}

	private Process createProcess(Exchange[] exchanges, Parameter[] parameters) {
		Process p = new Process();
		p.setRefId(UUID.randomUUID().toString());
		if (parameters != null)
			for (Parameter param : parameters)
				p.getParameters().add(param);
		if (exchanges != null)
			for (Exchange e : exchanges)
				p.getExchanges().add(e);
		return p;
	}

	private ImpactFactor createImpactFactor(String formula, Uncertainty u) {
		ImpactFactor f = new ImpactFactor();
		f.formula = formula;
		f.uncertainty = u;
		return f;
	}

	private ImpactMethod createImpactMethod(ImpactFactor[] factors,
			Parameter[] parameters) {
		ImpactMethod m = new ImpactMethod();
		m.setRefId(UUID.randomUUID().toString());
		if (parameters != null)
			for (Parameter p : parameters)
				m.parameters.add(p);
		if (factors != null) {
			ImpactCategory c = new ImpactCategory();
			c.setRefId(UUID.randomUUID().toString());
			m.impactCategories.add(c);
			for (ImpactFactor f : factors)
				c.impactFactors.add(f);
		}
		return m;
	}

	private ParameterRedef createRedef(Parameter p, Uncertainty u) {
		ParameterRedef redef = new ParameterRedef();
		redef.setName(p.getName());
		if (p.getScope() == ParameterScope.PROCESS)
			redef.setContextType(ModelType.PROCESS);
		else if (p.getScope() == ParameterScope.IMPACT_METHOD)
			redef.setContextType(ModelType.IMPACT_METHOD);
		if (p.getScope() != ParameterScope.GLOBAL)
			redef.setContextId(1l);
		redef.setValue(1);
		redef.setUncertainty(u);
		return redef;
	}

	private ProductSystem createSystem(ParameterRedef... redefs) {
		ProductSystem s = new ProductSystem();
		s.setRefId(UUID.randomUUID().toString());
		for (ParameterRedef redef : redefs)
			s.getParameterRedefs().add(redef);
		return s;
	}

	private Project createProject(ParameterRedef... redefs) {
		Project p = new Project();
		p.setRefId(UUID.randomUUID().toString());
		ProjectVariant v = new ProjectVariant();
		p.getVariants().add(v);
		for (ParameterRedef redef : redefs)
			v.getParameterRedefs().add(redef);
		return p;
	}

}
