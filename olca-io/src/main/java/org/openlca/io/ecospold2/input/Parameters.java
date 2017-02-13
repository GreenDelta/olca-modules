package org.openlca.io.ecospold2.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.io.ecospold2.UncertaintyConverter;
import org.openlca.util.Strings;

import spold2.DataSet;
import spold2.Exchange;
import spold2.IntermediateExchange;
import spold2.Property;
import spold2.Spold2;

final class Parameters {

	private Parameters() {
	}

	/**
	 * Creates openLCA process parameters from the parameters and formulas in
	 * the given data set.
	 */
	static List<Parameter> fetch(DataSet ds, ImportConfig config) {
		List<Parameter> params = new ArrayList<>();
		fetchProcessParameters(ds, params, config);
		fetchFromExchanges(Spold2.getElemFlows(ds), params, config);
		fetchFromExchanges(Spold2.getProducts(ds), params, config);
		return params;
	}

	private static void fetchProcessParameters(DataSet ds,
			List<Parameter> parameters, ImportConfig config) {
		for (spold2.Parameter param : Spold2.getParameters(ds)) {
			if (!canCreate(param.variableName, parameters))
				continue;
			Parameter olcaParam = new Parameter();
			parameters.add(olcaParam);
			olcaParam.setDescription(param.unitName);
			olcaParam.setName(param.variableName);
			setScope(param, olcaParam);
			olcaParam.setValue(param.amount);
			olcaParam.setUncertainty(UncertaintyConverter.toOpenLCA(param.uncertainty, 1));
			String formula = param.mathematicalRelation;
			if (config.withParameterFormulas && isValid(formula, config)) {
				olcaParam.setFormula(formula.trim());
				olcaParam.setInputParameter(false);
			} else {
				olcaParam.setInputParameter(true);
			}
		}
	}

	private static void setScope(spold2.Parameter param,
			Parameter olcaParam) {
		String scope = param.scope;
		String global = ParameterScope.GLOBAL.name();
		if (scope != null && global.equalsIgnoreCase(scope))
			olcaParam.setScope(ParameterScope.GLOBAL);
		else
			olcaParam.setScope(ParameterScope.PROCESS);
	}

	private static void fetchFromExchanges(List<? extends Exchange> exchanges,
			List<Parameter> params, ImportConfig config) {
		for (Exchange exchange : exchanges) {
			fetchFromExchange(exchange, params, config);
			fetchFromProperties(exchange.properties, params, config);
			if (exchange instanceof IntermediateExchange)
				fetchFromProductionVolume((IntermediateExchange) exchange,
						params, config);
		}
	}

	private static void fetchFromExchange(Exchange exchange,
			List<Parameter> params, ImportConfig config) {
		if (!canCreate(exchange.variableName, params))
			return;
		Parameter olcaParam = new Parameter();
		olcaParam.setName(exchange.variableName);
		olcaParam.setScope(ParameterScope.PROCESS);
		olcaParam.setValue(exchange.amount);
		olcaParam.setDescription(exchange.unit);
		String formula = exchange.mathematicalRelation;
		if (config.withParameterFormulas && isValid(formula, config)) {
			olcaParam.setFormula(formula.trim());
			olcaParam.setInputParameter(false);
		} else
			olcaParam.setInputParameter(true);
		params.add(olcaParam);
	}

	private static void fetchFromProductionVolume(
			IntermediateExchange exchange, List<Parameter> params,
			ImportConfig config) {
		String varName = exchange.productionVolumeVariableName;
		Double amount = exchange.productionVolumeAmount;
		String formula = exchange.productionVolumeMathematicalRelation;
		if (!canCreate(exchange.productionVolumeVariableName, params))
			return;
		Parameter param = new Parameter();
		param.setName(varName);
		param.setScope(ParameterScope.PROCESS);
		param.setValue(amount == null ? 0d : amount);
		if (config.withParameterFormulas && isValid(formula, config)) {
			param.setFormula(formula.trim());
			param.setInputParameter(false);
		} else
			param.setInputParameter(true);
		params.add(param);
	}

	private static void fetchFromProperties(List<Property> properties,
			List<Parameter> parameters, ImportConfig config) {
		for (Property property : properties) {
			if (!canCreate(property.variableName, parameters))
				continue;
			Parameter olcaParam = new Parameter();
			olcaParam.setName(property.variableName);
			olcaParam.setScope(ParameterScope.PROCESS);
			olcaParam.setValue(property.amount);
			olcaParam.setDescription(property.unit);
			String formula = property.mathematicalRelation;
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
		if (formula.contains("UnitConversion"))
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
