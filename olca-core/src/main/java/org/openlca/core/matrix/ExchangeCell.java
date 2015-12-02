package org.openlca.core.matrix;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeCell {

	final CalcExchange exchange;
	private double allocationFactor = 1d;
	private NumberGenerator generator;

	ExchangeCell(CalcExchange exchange) {
		this.exchange = exchange;
	}

	void setAllocationFactor(double allocationFactor) {
		this.allocationFactor = allocationFactor;
	}

	void eval(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		try {
			Scope scope = interpreter.getScope(exchange.processId);
			if (scope == null)
				scope = interpreter.getGlobalScope();
			tryEval(scope);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Formula evaluation failed, exchange "
					+ exchange.exchangeId, e);
		}
	}

	private void tryEval(Scope scope) throws InterpreterException {
		if (exchange.amountFormula != null) {
			double v = scope.eval(exchange.amountFormula);
			exchange.amount = v;
		}
		if (exchange.parameter1Formula != null) {
			double v = scope.eval(exchange.parameter1Formula);
			exchange.parameter1 = v;
		}
		if (exchange.parameter2Formula != null) {
			double v = scope.eval(exchange.parameter2Formula);
			exchange.parameter2 = v;
		}
		if (exchange.parameter3Formula != null) {
			double v = scope.eval(exchange.parameter3Formula);
			exchange.parameter3 = v;
		}
	}

	double getMatrixValue() {
		if (exchange == null)
			return 0;
		double amount = exchange.amount * allocationFactor
				* exchange.conversionFactor;
		if (exchange.input && !exchange.avoidedProduct)
			return -amount;
		else
			return amount;
	}

	double getCostValue() {
		if (exchange == null)
			return 0;
		double val = exchange.costValue * allocationFactor;
		if (exchange.flowType == FlowType.PRODUCT_FLOW && !exchange.input)
			return -val;
		else
			return val;
	}

	double getNextSimulationValue() {
		UncertaintyType type = exchange.uncertaintyType;
		if (type == null || type == UncertaintyType.NONE)
			return getMatrixValue();
		if (generator == null)
			generator = createGenerator(type);
		double amount = generator.next() * allocationFactor
				* exchange.conversionFactor;
		if (exchange.input && !exchange.avoidedProduct)
			return -amount;
		else
			return amount;
	}

	private NumberGenerator createGenerator(UncertaintyType type) {
		final CalcExchange e = exchange;
		switch (type) {
		case LOG_NORMAL:
			return NumberGenerator.logNormal(e.parameter1, e.parameter2);
		case NORMAL:
			return NumberGenerator.normal(e.parameter1, e.parameter2);
		case TRIANGLE:
			return NumberGenerator.triangular(e.parameter1,
					e.parameter2, e.parameter3);
		case UNIFORM:
			return NumberGenerator.uniform(e.parameter1, e.parameter2);
		default:
			return NumberGenerator.discrete(e.amount);
		}
	}

}
