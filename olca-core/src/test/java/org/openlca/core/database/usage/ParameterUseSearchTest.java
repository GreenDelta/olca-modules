package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;

public class ParameterUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<ParameterDescriptor> search;

	@Before
	public void setup() {
		search = IUseSearch.FACTORY.createFor(ModelType.PARAMETER, database);
	}

	@Test
	public void testFindNoUsage() {
		Parameter parameter = new Parameter();
		parameter.name = "testNoUsage";
		Tests.insert(parameter);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(parameter));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		Tests.delete(parameter);
	}

	@Test
	public void testFindGlobal() {
		Parameter p1 = createParameter("p1", 5d, ParameterScope.GLOBAL);
		Parameter p2 = createParameter("p2", 5d, ParameterScope.GLOBAL);
		Parameter p3 = createParameter("p3", "5*3", ParameterScope.GLOBAL);
		Parameter p4 = createParameter("p4", "5*p1", ParameterScope.GLOBAL);
		Tests.insert(p1);
		Tests.insert(p2);
		Tests.insert(p3);
		Tests.insert(p4);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(p1));
		Tests.delete(p1);
		Tests.delete(p2);
		Tests.delete(p3);
		Tests.delete(p4);
		Descriptor expected = Descriptor.of(p4);
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(expected, models.get(0));
	}

	@Test
	public void testFindInProcess() {
		Parameter p1 = createParameter("p1", 5d, ParameterScope.GLOBAL);
		Parameter p2 = createParameter("p2", "5*p1", ParameterScope.PROCESS);
		Process process = new Process();
		process.parameters.add(p2);
		Tests.insert(p1);
		Tests.insert(process);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(p1));
		Tests.delete(p1);
		Tests.delete(process);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(expected, models.get(0));
	}

	@Test
	public void testFindInImpactCategory() {
		Parameter p1 = createParameter("p1", 5d,
				ParameterScope.GLOBAL);
		Parameter p2 = createParameter("p2", "5*p1",
				ParameterScope.IMPACT_CATEGORY);
		ImpactCategory impact = new ImpactCategory();
		impact.parameters.add(p2);
		Tests.insert(p1);
		Tests.insert(impact);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(p1));
		Tests.delete(p1);
		Tests.delete(impact);
		Descriptor expected = Descriptor.of(impact);
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(expected, models.get(0));
	}

	@Test
	public void testFindNotInProcess() {
		Parameter p = createParameter("p1", 5d, ParameterScope.GLOBAL);
		Parameter p1 = createParameter("p1", 5d, ParameterScope.PROCESS);
		Parameter p2 = createParameter("p2", "5*p1", ParameterScope.PROCESS);
		Process process = new Process();
		process.parameters.add(p1);
		process.parameters.add(p2);
		Tests.insert(p);
		Tests.insert(process);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(p));
		Tests.delete(p);
		Tests.delete(process);
		Assert.assertEquals(0, models.size());
	}

	@Test
	public void testFindNotInImpactCategory() {
		Parameter p = createParameter("p1", 5d, ParameterScope.GLOBAL);
		Parameter p1 = createParameter("p1", 5d, ParameterScope.IMPACT_CATEGORY);
		Parameter p2 = createParameter("p2", "5*p1",
				ParameterScope.IMPACT_CATEGORY);
		ImpactCategory impact = new ImpactCategory();
		impact.parameters.add(p1);
		impact.parameters.add(p2);
		Tests.insert(p);
		Tests.insert(impact);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(p));
		Tests.delete(p);
		Tests.delete(impact);
		Assert.assertEquals(0, models.size());
	}

	@Test
	public void testFindProductSystemRedef() {
		Parameter parameter = createParameter("p1", 5d, ParameterScope.GLOBAL);
		Tests.insert(parameter);
		ProductSystem system = new ProductSystem();
		system.parameterRedefs.add(createParameterRedef("p1"));
		Tests.insert(system);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(parameter));
		Tests.delete(parameter);
		Tests.delete(system);
		Descriptor expected = Descriptor.of(system);
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(expected, models.get(0));
	}

	@Test
	public void testFindProjectRedef() {
		Parameter parameter = createParameter("p1", 5d, ParameterScope.GLOBAL);
		Tests.insert(parameter);
		Project project = new Project();
		ProjectVariant variant = new ProjectVariant();
		variant.parameterRedefs.add(createParameterRedef("p1"));
		project.variants.add(variant);
		Tests.insert(project);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(parameter));
		Tests.delete(parameter);
		Tests.delete(project);
		Descriptor expected = Descriptor.of(project);
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(expected, models.get(0));
	}

	private Parameter createParameter(String name, Object value,
			ParameterScope scope) {
		Parameter parameter = new Parameter();
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

	private ParameterRedef createParameterRedef(String name) {
		ParameterRedef redef = new ParameterRedef();
		redef.name = name;
		redef.value = 5d;
		return redef;
	}

}
