package org.openlca.io.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.Exchange;
import org.openlca.ecospold2.Property;
import org.openlca.util.Strings;

final class Parameters {

	private Parameters() {
	}

	/**
	 * Creates openLCA process parameters from the parameters and formulas in
	 * the given data set.
	 */
	static List<Parameter> fetch(DataSet dataSet, ImportConfig config) {
		List<Parameter> params = new ArrayList<>();
		fetchProcessParameters(dataSet, params, config);
		fetchFromExchanges(dataSet.getElementaryExchanges(), params, config);
		fetchFromExchanges(dataSet.getIntermediateExchanges(), params, config);
		return params;

	}

	private static void fetchProcessParameters(DataSet dataSet,
			List<Parameter> parameters, ImportConfig config) {
		for (org.openlca.ecospold2.Parameter param : dataSet.getParameters()) {
			if (!canCreate(param.getVariableName(), parameters))
				continue;
			Parameter olcaParam = new Parameter();
			parameters.add(olcaParam);
			olcaParam.setDescription(param.getUnitName());
			olcaParam.setName(param.getVariableName());
			olcaParam.setScope(ParameterScope.PROCESS);
			olcaParam.setValue(param.getAmount());
			String formula = param.getMathematicalRelation();
			if (config.withParameterFormulas && isValid(formula, config)) {
				olcaParam.setFormula(formula.trim());
				olcaParam.setInputParameter(false);
			} else {
				olcaParam.setInputParameter(true);
			}
		}
	}

	private static void fetchFromExchanges(List<? extends Exchange> exchanges,
			List<Parameter> params, ImportConfig config) {
		for (Exchange exchange : exchanges) {
			fetchFromProperties(exchange.getProperties(), params, config);
			if (!canCreate(exchange.getVariableName(), params))
				continue;
			Parameter olcaParam = new Parameter();
			olcaParam.setName(exchange.getVariableName());
			olcaParam.setScope(ParameterScope.PROCESS);
			olcaParam.setValue(exchange.getAmount());
			olcaParam.setDescription(exchange.getUnitName());
			String formula = exchange.getMathematicalRelation();
			if (config.withParameterFormulas && isValid(formula, config)) {
				olcaParam.setFormula(formula.trim());
				olcaParam.setInputParameter(false);
			} else
				olcaParam.setInputParameter(true);
			params.add(olcaParam);
		}
	}

	private static void fetchFromProperties(List<Property> properties,
			List<Parameter> parameters, ImportConfig config) {
		for (Property property : properties) {
			if (!canCreate(property.getVariableName(), parameters))
				continue;
			Parameter olcaParam = new Parameter();
			olcaParam.setName(property.getVariableName());
			olcaParam.setScope(ParameterScope.PROCESS);
			olcaParam.setValue(property.getAmount());
			olcaParam.setDescription(property.getUnitName());
			String formula = property.getMathematicalRelation();
			if (config.withParameterFormulas && isValid(formula, config)) {
				olcaParam.setFormula(formula.trim());
				olcaParam.setInputParameter(false);
			} else
				olcaParam.setInputParameter(true);
			parameters.add(olcaParam);
		}
	}

	private static boolean canCreate(String name, List<Parameter> parameters) {
		if (Strings.nullOrEmpty(name))
			return false;
		if (contains(name, parameters))
			return false;
		return true;
	}

	static boolean isValid(String formula, ImportConfig config) {
		if (formula == null)
			return false;
		if (formula.trim().isEmpty())
			return false;
		if (!config.checkFormulas)
			return true;
		if (formula.startsWith("LiveLink"))
			return false;
		if (formula.contains(","))
			return false;
		if (formula.contains("UnitConversion("))
			return false;
		if (formula.contains("Ref("))
			return false;
		else
			return true;
	}

	static boolean contains(String parameterName, List<Parameter> parameters) {
		for (Parameter param : parameters) {
			if (Strings.nullOrEqual(parameterName, param.getName()))
				return true;
		}
		return false;
	}
}
