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
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		Parameter parameter = createParameter(n1, "3*" + n3);
		parameter.category = insertAndAddExpected("category", new Category());
		insertAndAddExpected(n3, createParameter(n3, 5d));
		Parameter globalUnreferenced = createParameter(n2, "3*3");
		// must be inserted manually
		globalUnreferenced = db.insert(globalUnreferenced);
		return db.insert(parameter);
	}

	private Parameter createParameter(String name, Object value) {
		Parameter parameter = new Parameter();
		parameter.name = name;
		boolean formula = value instanceof String;
		parameter.isInputParameter = !formula;
		if (formula)
			parameter.formula = value.toString();
		else
			parameter.value = (double) value;
		parameter.scope = ParameterScope.GLOBAL;
		return parameter;
	}

}
