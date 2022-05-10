package org.openlca.validation;

import java.sql.ResultSet;

import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ModelType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.Strings;

class AllocationCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;
	private FormulaInterpreter interpreter;

	AllocationCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkFactors();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked allocation factors");
			}
		} catch (Exception e) {
			v.error("error in validation of allocation factors", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkFactors() {
		if (v.wasCanceled())
			return;
		var physicals = new TLongDoubleHashMap();
		var economics = new TLongDoubleHashMap();
		var causals = new TLongObjectHashMap<TLongDoubleHashMap>();

		var sql = "select " +
			/* 1 */ "allocation_type, " +
			/* 2 */ "value, " +
			/* 3 */ "formula, " +
			/* 4 */ "f_process, " +
			/* 5 */ "f_exchange from tbl_allocation_factors";
		NativeSql.on(v.db).query(sql, r -> {
			var method = AllocationMethod.valueOf(r.getString(1));
			var value = valueOf(r);
			if (value == 0)
				return !v.wasCanceled();
			var process = r.getLong(4);
			if (method == AllocationMethod.PHYSICAL) {
				physicals.adjustOrPutValue(process, value, value);
			} else if (method == AllocationMethod.ECONOMIC) {
				economics.adjustOrPutValue(process, value, value);
			} else if (method == AllocationMethod.CAUSAL) {
				var exchange = r.getLong(5);
				var map = causals.get(process);
				if (map == null) {
					map = new TLongDoubleHashMap();
					causals.put(process, map);
				}
				map.adjustOrPutValue(exchange, value, value);
			}

			return !v.wasCanceled();
		});

		if (v.wasCanceled())
			return;

		var errors = new TLongHashSet();
		check(physicals, errors);
		check(economics, errors);
		for (var it = causals.iterator(); it.hasNext();) {
			it.advance();
			var processId = it.key();
			var sums = it.value();
			if (errors.contains(processId) || sums == null)
				continue;
			for (var sum = sums.iterator(); sum.hasNext();) {
				sum.advance();
				if (isNotOne(sum.value())) {
					addError(processId, errors);
					break;
				}
			}
		}
	}

	private double valueOf(ResultSet r) {
		try {
			var formula = r.getString(3);
			if (Strings.nullOrEmpty(formula))
				return r.getDouble(2);

			if (interpreter == null) {
				interpreter = Util.interpreterOf(v.db);
			}
			var process = r.getLong(4);
			return interpreter.getOrCreate(process)
				.eval(formula);
		} catch (Exception e) {
			return -1;
		}
	}

	private void check(TLongDoubleHashMap map, TLongHashSet errors) {
		for (var it = map.iterator(); it.hasNext(); ) {
			it.advance();
			var processId = it.key();
			if (errors.contains(processId))
				continue;
			if (isNotOne(it.value())) {
				addError(processId, errors);
			}
		}
	}

	private void addError(long processId, TLongHashSet errors) {
		foundErrors = true;
		errors.add(processId);
		v.error(processId, ModelType.PROCESS,
			"allocation factors do not sum up to 1");
	}

	private boolean isNotOne(double factor) {
		var v = Math.abs(factor - 1);
		return v > 1e-4;
	}

}
