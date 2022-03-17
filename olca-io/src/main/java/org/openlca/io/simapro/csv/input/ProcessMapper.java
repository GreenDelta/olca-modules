package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.openlca.io.maps.SyncFlow;
import org.openlca.simapro.csv.Numeric;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.slf4j.LoggerFactory;

interface ProcessMapper {

	ImportContext context();

	default IDatabase db() {
		return context().db();
	}

	default RefData refData() {
		return context().refData();
	}

	Process process();

	Scope formulaScope();

	/**
	 * Creates the respective process parameters and returns the evaluation
	 * scope for the process. This must be only called once per mapper.
	 */
	default Scope createFormulaScope() {

		// create the interpreter and bind the global parameters
		var interpreter = new FormulaInterpreter();
		var dao = new ParameterDao(db());
		for (var param : dao.getGlobalParameters()) {
			if (param.isInputParameter) {
				interpreter.bind(param.name, param.value);
			} else {
				interpreter.bind(param.name, param.formula);
			}
		}

		// create the evaluation scope for the process and the
		// process parameters
		var scope = interpreter.createScope(1);
		for (var row : inputParameterRows()) {
			var p = Parameters.create(row, ParameterScope.PROCESS);
			process().parameters.add(p);
			scope.bind(p.name, p.value);
		}
		for (var row : calculatedParameterRows()) {
			var p = Parameters.create(
				context().dataSet(), row, ParameterScope.PROCESS);
			process().parameters.add(p);
			scope.bind(p.name, p.formula);
		}

		// evaluate the calculated parameters of the process
		for (var param : process().parameters) {
			if (param.isInputParameter)
				continue;
			try {
				param.value = scope.eval(param.name);
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("failed to evaluate process parameter " + param
					+ "; set it as an input parameter with value 1", e);
				param.formula = null;
				param.value = 1;
				param.isInputParameter = true;
				scope.bind(param.name, 1);
			}
		}

		return scope;
	}

	List<InputParameterRow> inputParameterRows();

	List<CalculatedParameterRow> calculatedParameterRows();

	/**
	 * Tries to infer the category and location of the process from the reference
	 * flow of the process. It directly sets these attributes to the underlying
	 * process if these attributes can be found. It is important that this method
	 * is called after the exchanges of the process are mapped.
	 */
	default void inferCategoryAndLocation() {
		var qref = process().quantitativeReference;
		if (qref == null || qref.flow == null)
			return;
		var flow = qref.flow;
		process().location = flow.location;
		if (flow.category == null)
			return;
		var path = new ArrayList<String>();
		var c = flow.category;
		while (c != null) {
			path.add(0, c.name);
			c = c.category;
		}
		if (path.isEmpty())
			return;
		process().category = CategoryDao.sync(
			db(), ModelType.PROCESS, path.toArray(String[]::new));
	}

	default double eval(Numeric numeric) {
		if (numeric == null)
			return 0;
		if (!numeric.hasFormula())
			return numeric.value();
		try {
			var formula = formulaOf(numeric.formula());
			return formulaScope().eval(formula);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to evaluate formula " + numeric.formula()
				+ "; set value to " + numeric.value(), e);
			return numeric.value();
		}
	}

	default Exchange exchangeOf(SyncFlow f, ExchangeRow row) {
		if (f == null || f.flow() == null) {
			var log = context().log();
			log.error("could not create exchange " +
				"as there was now flow found for: "+  row.name());
			return null;
		}

		// init the exchange
		var e = new Exchange();
		process().lastInternalId++;
		e.internalId = process().lastInternalId;
		process().exchanges.add(e);
		e.description = row.comment();
		e.flow = f.flow();
		e.uncertainty = Uncertainties.of(row);

		// mapped flows
		if (f.isMapped()) {
			double factor = f.mapFactor();
			if (e.uncertainty != null) {
				e.uncertainty.scale(factor);
			}
			e.amount = factor * eval(row.amount());
			if (row.amount().hasFormula()) {
				var formula = formulaOf(row.amount().formula());
				e.formula = factor + " * (" + formula + ")";
			}
			e.flowPropertyFactor = f.flow().getReferenceFactor();
			e.unit = f.flow().getReferenceUnit();

			// unmapped flows
		} else {
			e.amount = eval(row.amount());
			if (row.amount().hasFormula()) {
				e.formula = formulaOf(row.amount().formula());
			}
			var quantity = refData().quantityOf(row.unit());
			if (quantity != null) {
				e.unit = quantity.unit;
				e.flowPropertyFactor = f.flow().getFactor(quantity.flowProperty);
			}
		}
		return e;
	}

	default String formulaOf(String expression) {
		return Parameters.formulaOf(context().dataSet(), expression);
	}
}
