package org.openlca.core.database.references;

import org.junit.After;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ParameterDao;
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

	@After
	public void deleteParameterAndFlow() {
		new ParameterDao(Tests.getDb()).deleteAll();
		new FlowDao(Tests.getDb()).deleteAll();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.PROJECT;
	}

	@Override
	protected Project createModel() {
		Project project = new Project();
		project.setCategory(addExpected(new Category()));
		project.setAuthor(addExpected(new Actor()));
		project.setImpactMethodId(addExpected(new ImpactMethod()).getId());
		project.getVariants().add(
				createProjectVariant(project.getImpactMethodId(), true));
		project.getVariants().add(
				createProjectVariant(project.getImpactMethodId(), false));
		Parameter globalUnreferenced = createParameter("p1", "3*3", true);
		Parameter globalUnreferenced2 = createParameter("p3", "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		return project;
	}

	private ProjectVariant createProjectVariant(long methodId,
			boolean createParameters) {
		ProjectVariant variant = new ProjectVariant();
		variant.setProductSystem(addExpected(new ProductSystem()));
		variant.getParameterRedefs().add(
				createParameterRedef("p1", methodId, createParameters));
		// formula with parameter to see if added as reference (unexpected)
		variant.getParameterRedefs().add(
				createParameterRedef("p2", "p3*5", createParameters));
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(addExpected(new FlowProperty()));
		variant.setFlowPropertyFactor(factor);
		UnitGroup unitGroup = new UnitGroup();
		Unit unit = new Unit();
		unitGroup.getUnits().add(unit);
		variant.setUnit(unit);
		addExpected(unitGroup);
		Flow flow = new Flow();
		flow.getFlowPropertyFactors().add(factor);
		// don't add flow to expected references, just for persisting the factor
		Tests.insert(flow);
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
			addExpected(createParameter(name, contextOrValue.toString(), true));
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
