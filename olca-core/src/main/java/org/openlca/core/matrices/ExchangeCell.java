package org.openlca.core.matrices;

import org.openlca.core.indices.CalcExchange;
import org.openlca.core.math.NumberGenerator;
import org.openlca.core.model.UncertaintyDistributionType;
import org.openlca.expressions.InterpreterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeCell {

	private final CalcExchange exchange;
	private NumberGenerator generator;
	private double allocationFactor = 1d;

	public ExchangeCell(CalcExchange exchange) {
		this.exchange = exchange;
	}

	public void setAllocationFactor(double allocationFactor) {
		this.allocationFactor = allocationFactor;
	}

	public void eval(FormulaInterpreter interpreter) {
		if (interpreter == null)
			return;
		try {
			tryEval(interpreter);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error(
					"Formula evaluation failed, exchange "
							+ exchange.getExchangeId(), e);
		}
	}

	private void tryEval(FormulaInterpreter interpreter)
			throws InterpreterException {
		if (exchange.getAmountFormula() != null) {
			double v = interpreter.eval(exchange.getAmountFormula());
			exchange.setAmount(v);
		}
		if (exchange.getParameter1Formula() != null) {
			double v = interpreter.eval(exchange.getParameter1Formula());
			exchange.setParameter1(v);
		}
		if (exchange.getParameter2Formula() != null) {
			double v = interpreter.eval(exchange.getParameter2Formula());
			exchange.setParameter2(v);
		}
		if (exchange.getParameter3Formula() != null) {
			double v = interpreter.eval(exchange.getParameter3Formula());
			exchange.setParameter3(v);
		}
	}

	public double getMatrixValue() {
		if (exchange == null)
			return 0;
		double amount = exchange.getAmount() * allocationFactor
				* exchange.getConversionFactor();
		if (exchange.isInput())
			return -amount;
		else
			return amount;
	}

	public double getNextSimulationValue() {
		if (generator == null)
			generator = createGenerator();
		double amount = generator.next() * allocationFactor
				* exchange.getConversionFactor();
		if (exchange.isInput())
			return -amount;
		else
			return amount;
	}

	private NumberGenerator createGenerator() {
		UncertaintyDistributionType type = exchange.getUncertaintyType();
		if (type == null && type == UncertaintyDistributionType.NONE)
			return NumberGenerator.discrete(exchange.getAmount()
					* exchange.getConversionFactor());
		final CalcExchange e = exchange;
		switch (exchange.getUncertaintyType()) {
		case LOG_NORMAL:
			return NumberGenerator.logNormal(e.getParameter1(),
					e.getParameter2());
		case NORMAL:
			return NumberGenerator.normal(e.getParameter1(), e.getParameter2());
		case TRIANGLE:
			return NumberGenerator.triangular(e.getParameter1(),
					e.getParameter2(), e.getParameter3());
		case UNIFORM:
			return NumberGenerator
					.uniform(e.getParameter1(), e.getParameter2());
		default:
			return NumberGenerator.discrete(e.getAmount());
		}
	}

}
