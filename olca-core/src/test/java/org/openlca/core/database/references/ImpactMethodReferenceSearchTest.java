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
import org.openlca.core.model.Source;
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
		method.category = insertAndAddExpected("category", new Category());
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		String n4 = generateName();
		String n5 = generateName();
		method.parameters.add(createParameter(n1, 3d, false));
		method.parameters.add(createParameter(n2, n1 + "*2*" + n3, false));
		insertAndAddExpected(n3, createParameter(n3, "5*5", true));
		// formula with parameter to see if added as reference (unexpected)
		insertAndAddExpected(n4, createParameter(n4, "3*" + n5, true));
		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n5, "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		method.impactCategories.add(createImpactCategory(n4));
		method.impactCategories.add(createImpactCategory(n4));
		method.sources.add(Tests.insert(new Source()));
		method.sources.add(Tests.insert(new Source()));
		method = Tests.insert(method);
		for (ImpactCategory category : method.impactCategories)
			for (ImpactFactor f : category.impactFactors) {
				addExpected("flow", f.flow, "impactFactors",
						ImpactFactor.class, f.id);
				addExpected("flowPropertyFactor", f.flowPropertyFactor,
						"impactFactors", ImpactFactor.class, f.id);
				addExpected("flowProperty", f.flowPropertyFactor.flowProperty, "flowPropertyFactor",
						FlowPropertyFactor.class, f.flowPropertyFactor.id);
				addExpected("unit", f.unit, "impactFactors",
						ImpactFactor.class, f.id);
			}
		for (Source s : method.sources)
			addExpected("sources", s);
		return method;
	}

	private ImpactCategory createImpactCategory(String p4Name) {
		ImpactCategory category = new ImpactCategory();
		category.impactFactors.add(createImpactFactor(3d));
		category.impactFactors.add(createImpactFactor("2*" + p4Name));
		return Tests.insert(category);
	}

	private ImpactFactor createImpactFactor(Object value) {
		ImpactFactor iFactor = new ImpactFactor();
		Flow flow = createFlow();
		FlowPropertyFactor factor = flow.flowPropertyFactors.get(0);
		Unit unit = factor.flowProperty.unitGroup.units.get(0);
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
		unit.name = "unit";
		group.units.add(unit);
		group = Tests.insert(group);
		FlowProperty property = new FlowProperty();
		property.unitGroup = group;
		property = Tests.insert(property);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.flowProperty = property;
		flow.flowPropertyFactors.add(factor);
		return Tests.insert(flow);
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
			parameter.scope = ParameterScope.IMPACT_METHOD;
		return parameter;
	}

}
