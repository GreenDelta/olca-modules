package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ProjectReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.PROJECT;
	}

	@Override
	protected Project createModel() {
		Project project = new Project();
		project.setCategory(insertAndAddExpected("category", new Category()));
		project.setAuthor(insertAndAddExpected("author", new Actor()));
		project.setImpactMethodId(insertAndAddExpected("impactMethodId",
				new ImpactMethod()).getId());
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n3, "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		project.getVariants().add(
				createProjectVariant(n1, n2, n3, project.getImpactMethodId(),
						true));
		project.getVariants().add(
				createProjectVariant(n1, n2, n3, project.getImpactMethodId(),
						false));
		project = Tests.insert(project);
		for (ProjectVariant variant : project.getVariants()) {
			addExpected("productSystem", variant.getProductSystem(),
					"variants", ProjectVariant.class, variant.getId());
			addExpected("unit", variant.getUnit(), "variants",
					ProjectVariant.class, variant.getId());
			addExpected("flowPropertyFactor", variant.getFlowPropertyFactor(),
					"variants", ProjectVariant.class, variant.getId());
		}
		return project;
	}

	private ProjectVariant createProjectVariant(String p1Name, String p2Name,
			String p3Name, long methodId, boolean createParameters) {
		ProjectVariant variant = new ProjectVariant();
		variant.setProductSystem(Tests.insert(new ProductSystem()));
		variant.getParameterRedefs().add(
				createParameterRedef(p1Name, methodId, createParameters));
		// formula with parameter to see if added as reference (unexpected)
		variant.getParameterRedefs().add(
				createParameterRedef(p2Name, p3Name + "*5", createParameters));
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(Tests.insert(new FlowProperty()));
		variant.setFlowPropertyFactor(factor);
		UnitGroup unitGroup = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		unitGroup.getUnits().add(unit);
		unitGroup = Tests.insert(unitGroup);
		unit = unitGroup.getUnit(unit.getName());
		variant.setUnit(unit);
		Flow flow = new Flow();
		flow.getFlowPropertyFactors().add(factor);
		// don't add flow to expected references, just for persisting the factor
		flow = Tests.insert(flow);
		return variant;
	}

	private ParameterRedef createParameterRedef(String name,
			Object contextOrValue, boolean createParameter) {
		ParameterRedef redef = new ParameterRedef();
		redef.setName(name);
		redef.setValue(1d);
		if (contextOrValue instanceof Long) {
			redef.setContextType(ModelType.IMPACT_METHOD);
			redef.setContextId((long) contextOrValue);
		} else if (contextOrValue instanceof String && createParameter)
			insertAndAddExpected("parameterRedefs",
					createParameter(name, contextOrValue.toString(), true));
		return redef;
	}

	private Parameter createParameter(String name, Object value, boolean global) {
		Parameter parameter = new Parameter();
		parameter.setName(name);
		boolean formula = value instanceof String;
		parameter.setInputParameter(!formula);
		if (formula)
			parameter.setFormula(value.toString());
		else
			parameter.setValue((double) value);
		if (global)
			parameter.setScope(ParameterScope.GLOBAL);
		else
			parameter.setScope(ParameterScope.PROCESS);
		return parameter;
	}
}
