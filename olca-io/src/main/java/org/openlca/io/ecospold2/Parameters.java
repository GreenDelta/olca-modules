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
	static List<Parameter> fetch(DataSet dataSet) {
		List<Parameter> parameters = new ArrayList<>();
		fetchProcessParameters(dataSet, parameters);
		fetchFromExchanges(dataSet.getElementaryExchanges(), parameters);
		fetchFromExchanges(dataSet.getIntermediateExchanges(), parameters);
		return parameters;

	}

	private static void fetchProcessParameters(DataSet dataSet,
			List<Parameter> parameters) {
		for (org.openlca.ecospold2.Parameter param : dataSet.getParameters()) {
			if (!canCreate(param.getVariableName(), parameters))
				continue;
			Parameter olcaParam = new Parameter();
			parameters.add(olcaParam);
			olcaParam.setDescription(param.getUnitName());
			olcaParam.setName(param.getVariableName());
			olcaParam.setScope(ParameterScope.PROCESS);
			olcaParam.setValue(param.getAmount());
			if (isValidFormula(param.getMathematicalRelation())) {
				olcaParam.setFormula(param.getMathematicalRelation().trim());
				olcaParam.setInputParameter(false);
			} else {
				olcaParam.setInputParameter(true);
			}
		}
	}

	private static void fetchFromExchanges(List<? extends Exchange> exchanges,
			List<Parameter> parameters) {
		for (Exchange exchange : exchanges) {
			fetchFromProperties(exchange.getProperties(), parameters);
			if (!canCreate(exchange.getVariableName(), parameters))
				continue;
			Parameter olcaParam = new Parameter();
			olcaParam.setName(exchange.getVariableName());
			olcaParam.setScope(ParameterScope.PROCESS);
			olcaParam.setValue(exchange.getAmount());
			olcaParam.setDescription(exchange.getUnitName());
			if (isValidFormula(exchange.getMathematicalRelation())) {
				olcaParam.setFormula(exchange.getMathematicalRelation().trim());
				olcaParam.setInputParameter(false);
			} else
				olcaParam.setInputParameter(true);
			parameters.add(olcaParam);
		}
	}

	private static void fetchFromProperties(List<Property> properties,
			List<Parameter> parameters) {
		for (Property property : properties) {
			if (!canCreate(property.getVariableName(), parameters))
				continue;
			Parameter olcaParam = new Parameter();
			olcaParam.setName(property.getVariableName());
			olcaParam.setScope(ParameterScope.PROCESS);
			olcaParam.setValue(property.getAmount());
			olcaParam.setDescription(property.getUnitName());
			if (isValidFormula(property.getMathematicalRelation())) {
				olcaParam.setFormula(property.getMathematicalRelation().trim());
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

	static boolean isValidFormula(String formula) {
		if (formula == null)
			return false;
		if (formula.trim().isEmpty())
			return false;
		// there are even links to system local Excel tables in the ecoinvent 3
		// database
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
