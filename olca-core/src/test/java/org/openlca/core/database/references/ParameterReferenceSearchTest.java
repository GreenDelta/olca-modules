package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

public class ParameterReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.PARAMETER;
	}

	@Override
	protected Parameter createModel() {
		Parameter parameter = createParameter("p1", "3*p3");
		parameter.setCategory(insertAndAddExpected(new Category()));
		insertAndAddExpected(createParameter("p3", 5d));
		Parameter globalUnreferenced = createParameter("p2", "3*3");
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		return Tests.insert(parameter);
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
