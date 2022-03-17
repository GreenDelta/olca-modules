package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;

public class ParameterUseSearchTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testFindNoUsage() {
		var param = db.insert(Parameter.global("testNoUsage", 1));
		UsageTests.expectEmpty(param);
		db.delete(param);
	}

	@Test
	public void testFindGlobal() {
		var p1 = createParameter("p1", 5d, ParameterScope.GLOBAL);
		var p2 = createParameter("p2", 5d, ParameterScope.GLOBAL);
		var p3 = createParameter("p3", "5*3", ParameterScope.GLOBAL);
		var p4 = createParameter("p4", "5*p1", ParameterScope.GLOBAL);
		db.insert(p1, p2, p3, p4);
		UsageTests.expectOne(p1, p4);
		List.of(p2, p3, p4).forEach(UsageTests::expectEmpty);
		db.delete(p1, p2, p3, p4);
	}

	@Test
	public void testFindInProcess() {
		var p1 = createParameter("p1", 5d, ParameterScope.GLOBAL);
		var p2 = createParameter("p2", "5*p1", ParameterScope.PROCESS);
		var process = new Process();
		process.parameters.add(p2);
		db.insert(p1);
		db.insert(process);
		UsageTests.expectOne(p1, process);
		db.delete(p1, process);
	}

	@Test
	public void testFindInImpactCategory() {
		var p1 = createParameter("p1", 5d, ParameterScope.GLOBAL);
		var p2 = createParameter("p2", "5*p1", ParameterScope.IMPACT);
		var impact = new ImpactCategory();
		impact.parameters.add(p2);
		db.insert(p1, impact);
		UsageTests.expectOne(p1, impact);
		db.delete(p1, impact);
	}

	@Test
	public void testFindNotInProcess() {
		var p = createParameter("p1", 5d, ParameterScope.GLOBAL);
		var p1 = createParameter("p1", 5d, ParameterScope.PROCESS);
		var p2 = createParameter("p2", "5*p1", ParameterScope.PROCESS);
		var process = new Process();
		process.parameters.add(p1);
		process.parameters.add(p2);
		db.insert(p, process);
		UsageTests.expectEmpty(p);
		db.delete(p, process);
	}

	@Test
	public void testFindNotInImpactCategory() {
		var p = createParameter("p1", 5d, ParameterScope.GLOBAL);
		var p1 = createParameter("p1", 5d, ParameterScope.IMPACT);
		var p2 = createParameter("p2", "5*p1", ParameterScope.IMPACT);
		var impact = new ImpactCategory();
		impact.parameters.add(p1);
		impact.parameters.add(p2);
		db.insert(p);
		db.insert(impact);
		UsageTests.expectEmpty(p);
		db.delete(p, impact);
	}

	@Test
	public void testFindProductSystemRedef() {
		var param = db.insert(Parameter.global("p1", 5d));
		var system = new ProductSystem();
		system.parameterSets.add(
			ParameterRedefSet.of("baseline", ParameterRedef.of(param, 5)));
		db.insert(system);
		UsageTests.expectOne(param, system);
		db.delete(param, system);
	}

	@Test
	public void testFindProjectRedef() {
		var parameter = db.insert(Parameter.global("p1", 5d));
		var project = Project.of("project");
		var variant = new ProjectVariant();
		variant.parameterRedefs.add(ParameterRedef.of(parameter));
		project.variants.add(variant);
		db.insert(project);
		UsageTests.expectOne(parameter, project);
		db.delete(parameter, project);
	}

	private Parameter createParameter(
		String name, Object value, ParameterScope scope) {
		var parameter = new Parameter();
		parameter.name = name;
		boolean formula = value instanceof String;
		parameter.isInputParameter = !formula;
		if (formula)
			parameter.formula = value.toString();
		else
			parameter.value = (double) value;
		parameter.scope = scope;
		return parameter;
	}

}
