package org.openlca.core.database.references;

import java.util.HashMap;
import java.util.Map;

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

	private final Map<String, Parameter> parameters = new HashMap<>();

	@Override
	protected ModelType getModelType() {
		return ModelType.PROJECT;
	}

	@Override
	protected Project createModel() {
		Project project = new Project();
		project.category = insertAndAddExpected("category", new Category());
		project.impactMethod = insertAndAddExpected(
				"impactMethodId", new ImpactMethod());
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n3, "3*3", true);
		// must be inserted manually
		globalUnreferenced = db.insert(globalUnreferenced);
		globalUnreferenced2 = db.insert(globalUnreferenced2);
		project.variants.add(
				createProjectVariant(n1, n2, n3, project.impactMethod.id));
		project.variants.add(
				createProjectVariant(n1, n2, n3, project.impactMethod.id));
		project = db.insert(project);
		for (ProjectVariant v : project.variants) {
			addExpected("productSystem", v.productSystem, "variants", ProjectVariant.class, v.id);
			addExpected("unit", v.unit, "variants", ProjectVariant.class, v.id);
			addExpected("flowPropertyFactor", v.flowPropertyFactor, "variants", ProjectVariant.class, v.id);
			for (ParameterRedef p : v.parameterRedefs) {
				if (p.contextType == null) {
					addExpected(p.name, parameters.get(p.name), "variants", ProjectVariant.class, v.id);
				}
			}
		}
		return project;
	}

	private ProjectVariant createProjectVariant(String p1Name, String p2Name,
			String p3Name, long methodId) {
		ProjectVariant variant = new ProjectVariant();
		variant.productSystem = db.insert(new ProductSystem());
		variant.parameterRedefs.add(
				createParameterRedef(p1Name, methodId));
		variant.parameterRedefs.add(
				createParameterRedef(p2Name, p3Name + "*5"));
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.flowProperty = db.insert(new FlowProperty());
		variant.flowPropertyFactor = factor;
		UnitGroup unitGroup = new UnitGroup();
		Unit unit = new Unit();
		unit.name = "unit";
		unitGroup.units.add(unit);
		unitGroup = db.insert(unitGroup);
		unit = unitGroup.getUnit(unit.name);
		variant.unit = unit;
		Flow flow = new Flow();
		flow.flowPropertyFactors.add(factor);
		// don't add flow to expected references, just for persisting the factor
		flow = db.insert(flow);
		return variant;
	}

	private ParameterRedef createParameterRedef(String name, Object contextOrValue) {
		ParameterRedef redef = new ParameterRedef();
		redef.name = name;
		redef.value = 1d;
		if (contextOrValue instanceof Long) {
			redef.contextType = ModelType.IMPACT_METHOD;
			redef.contextId = (long) contextOrValue;
		} else {
			if (!parameters.containsKey(name)) {
				Parameter parameter = createParameter(name, contextOrValue.toString(), true);
				parameters.put(name, db.insert(parameter));
			}
		}
		return redef;
	}

	private Parameter createParameter(String name, Object value, boolean global) {
		Parameter parameter = new Parameter();
		parameter.name = name;
		boolean formula = value instanceof String;
		parameter.isInputParameter = !formula;
		if (formula)
			parameter.formula = value.toString();
		else
			parameter.value = (double) value;
		if (global)
			parameter.scope = ParameterScope.GLOBAL;
		else
			parameter.scope = ParameterScope.PROCESS;
		return parameter;
	}
}
