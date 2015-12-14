package org.openlca.core.database.references;

import org.junit.After;
import org.openlca.core.Tests;
import org.openlca.core.database.ParameterDao;
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

	@After
	public void deleteParameter() {
		new ParameterDao(Tests.getDb()).deleteAll();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.IMPACT_METHOD;
	}

	@Override
	protected ImpactMethod createModel() {
		ImpactMethod method = new ImpactMethod();
		method.setCategory(addExpected(new Category()));
		method.getImpactCategories().add(createImpactCategory());
		method.getImpactCategories().add(createImpactCategory());
		method.getParameters().add(createParameter("p1", 3d, false));
		method.getParameters().add(createParameter("p2", "p1*2*p3", false));
		addExpected(createParameter("p3", "5*5", true));
		// formula with parameter to see if added as reference (unexpected)
		addExpected(createParameter("p4", "3*p5", true));
		Parameter globalUnreferenced = createParameter("p1", "3*3", true);
		Parameter globalUnreferenced2 = createParameter("p5", "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		return method;
	}

	private ImpactCategory createImpactCategory() {
		ImpactCategory category = new ImpactCategory();
		category.getImpactFactors().add(createImpactFactor(3d));
		category.getImpactFactors().add(createImpactFactor("2*p4"));
		return category;
	}

	private ImpactFactor createImpactFactor(Object value) {
		ImpactFactor factor = new ImpactFactor();
		factor.setFlow(createFlow());
		factor.setFlowPropertyFactor(factor.getFlow().getFlowPropertyFactors()
				.get(0));
		factor.setUnit(factor.getFlowPropertyFactor().getFlowProperty()
				.getUnitGroup().getUnits().get(0));
		boolean formula = value instanceof String;
		if (formula)
			factor.setFormula(value.toString());
		else
			factor.setValue((double) value);
		return factor;
	}

	private Flow createFlow() {
		Flow flow = new Flow();
		FlowProperty property = new FlowProperty();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(property);
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		group.getUnits().add(unit);
		property.setUnitGroup(group);
		flow.getFlowPropertyFactors().add(factor);
		addExpected(group);
		addExpected(property);
		return addExpected(flow);
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
