package org.openlca.core.database.references;

import org.junit.After;
import org.openlca.core.Tests;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

public class ParameterReferenceSearchTest extends BaseReferenceSearchTest {

	@After
	public void deleteParameter() {
		new ParameterDao(Tests.getDb()).deleteAll();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.PARAMETER;
	}

	@Override
	protected Parameter createModel() {
		Parameter parameter = createParameter("p1", "3*p3");
		parameter.setCategory(addExpected(new Category()));
		addExpected(createParameter("p3", 5d));
		Parameter globalUnreferenced = createParameter("p2", "3*3");
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		return parameter;
	}

	private Parameter createParameter(String name, Object value) {
		Parameter parameter = new Parameter();
		parameter.setName(name);
		boolean formula = value instanceof String;
		parameter.setInputParameter(!formula);
		if (formula)
			parameter.setFormula(value.toString());
		else
			parameter.setValue((double) value);
		parameter.setScope(ParameterScope.GLOBAL);
		return parameter;
	}

}
