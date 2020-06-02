package org.openlca.core.matrix.uncertainties;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.matrix.CalcAllocationFactor;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

public class UMatrixTest {

	@Test
	public void testSimpleExchange() {
		CalcExchange e = baseExchange();
		UMatrix u = new UMatrix();
		u.add(42, 42, e);
		HashPointMatrix m = new HashPointMatrix(100, 100);
		u.generate(m, new FormulaInterpreter());
		Assert.assertEquals(42.0, m.get(42, 42), 1e-16);
	}

	@Test
	public void testUniformExchange() {
		CalcExchange e = baseExchange();
		e.parameter1 = 10;
		e.parameter2 = 20;
		e.uncertaintyType = UncertaintyType.UNIFORM;
		UMatrix u = new UMatrix();
		u.add(42, 42, e);
		HashPointMatrix m = new HashPointMatrix(100, 100);
		for (int i = 0; i < 10; i++) {
			u.generate(m, new FormulaInterpreter());
			double val = m.get(42, 42);
			Assert.assertTrue(val >= 10 && val <= 20);
		}
	}

	@Test
	public void testAddExchanges() {
		CalcExchange e1 = baseExchange();
		e1.parameter1 = 10;
		e1.parameter2 = 20;
		e1.uncertaintyType = UncertaintyType.UNIFORM;
		CalcExchange e2 = baseExchange();
		e2.parameter1 = 10;
		e2.parameter2 = 20;
		e2.uncertaintyType = UncertaintyType.UNIFORM;
		UMatrix u = new UMatrix();
		u.add(42, 42, e1);
		u.add(42, 42, e2);
		HashPointMatrix m = new HashPointMatrix(100, 100);
		for (int i = 0; i < 10; i++) {
			u.generate(m, new FormulaInterpreter());
			double val = m.get(42, 42);
			Assert.assertTrue(val >= 20 && val <= 40);
		}
	}

	@Test
	public void testAllocation() {
		CalcExchange e1 = baseExchange();
		e1.parameter1 = 10;
		e1.parameter2 = 20;
		e1.uncertaintyType = UncertaintyType.UNIFORM;
		CalcExchange e2 = baseExchange();
		e2.parameter1 = 10;
		e2.parameter2 = 20;
		e2.uncertaintyType = UncertaintyType.UNIFORM;
		UMatrix u = new UMatrix();
		u.add(42, 42, e1, CalcAllocationFactor.of(0L, 0.5));
		u.add(42, 42, e2, CalcAllocationFactor.of(0L, 0.5));
		HashPointMatrix m = new HashPointMatrix(100, 100);
		for (int i = 0; i < 10; i++) {
			u.generate(m, new FormulaInterpreter());
			double val = m.get(42, 42);
			Assert.assertTrue(val >= 10 && val <= 20);
		}
	}

	private CalcExchange baseExchange() {
		CalcExchange e = new CalcExchange();
		e.amount = 42.0;
		e.conversionFactor = 1.0;
		e.flowType = FlowType.ELEMENTARY_FLOW;
		e.exchangeId = e.flowId = e.processId = 42L;
		return e;
	}
}
