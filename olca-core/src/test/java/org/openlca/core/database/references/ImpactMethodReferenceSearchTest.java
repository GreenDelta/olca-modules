package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ImpactMethodReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.IMPACT_METHOD;
	}

	@Override
	protected ImpactMethod createModel() {
		ImpactMethod method = new ImpactMethod();
		method.setCategory(insertAndAddExpected("category", new Category()));
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		String n4 = generateName();
		String n5 = generateName();
		method.parameters.add(createParameter(n1, 3d, false));
		method.parameters.add(createParameter(n2, n1 + "*2*" + n3, false));
		insertAndAddExpected(null, createParameter(n3, "5*5", true));
		// formula with parameter to see if added as reference (unexpected)
		insertAndAddExpected(null, createParameter(n4, "3*" + n5, true));
		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n5, "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		method.impactCategories.add(createImpactCategory(n4));
		method.impactCategories.add(createImpactCategory(n4));
		method = Tests.insert(method);
		for (ImpactCategory category : method.impactCategories)
			for (ImpactFactor f : category.impactFactors) {
				addExpected("flow", f.flow, "impactFactors",
						ImpactFactor.class, f.getId());
				addExpected("flowPropertyFactor", f.flowPropertyFactor,
						"impactFactors", ImpactFactor.class, f.getId());
				addExpected("unit", f.unit, "impactFactors",
						ImpactFactor.class, f.getId());
			}
		return method;
	}

	private ImpactCategory createImpactCategory(String p4Name) {
		ImpactCategory category = new ImpactCategory();
		category.impactFactors.add(createImpactFactor(3d));
		category.impactFactors.add(createImpactFactor("2*" + p4Name));
		return category;
	}

	private ImpactFactor createImpactFactor(Object value) {
		ImpactFactor iFactor = new ImpactFactor();
		Flow flow = createFlow();
		FlowPropertyFactor factor = flow.getFlowPropertyFactors().get(0);
		Unit unit = factor.getFlowProperty().getUnitGroup().getUnits().get(0);
		iFactor.flow = flow;
		iFactor.flowPropertyFactor = factor;
		iFactor.unit = unit;
		boolean formula = value instanceof String;
		if (formula)
			iFactor.formula = value.toString();
		else
			iFactor.value = (double) value;
		return iFactor;
	}

	private Flow createFlow() {
		Flow flow = new Flow();
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		group.getUnits().add(unit);
		group = Tests.insert(group);
		FlowProperty property = new FlowProperty();
		property.setUnitGroup(group);
		property = Tests.insert(property);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(property);
		flow.getFlowPropertyFactors().add(factor);
		return Tests.insert(flow);
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
			parameter.setScope(ParameterScope.IMPACT_METHOD);
		return parameter;
	}

}
