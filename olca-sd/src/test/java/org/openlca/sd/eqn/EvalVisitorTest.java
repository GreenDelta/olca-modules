package org.openlca.sd.eqn;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.Auxil;
import org.openlca.sd.model.Id;

public class EvalVisitorTest {

	@Test
	public void testAddSub() {
		assertEquals(5.0, ev("2 + 3"), 1e-10);
		assertEquals(7.0, ev("7 + 0"), 1e-10);
		assertEquals(-1.0, ev("2 + -3"), 1e-10);
		assertEquals(-8.0, ev("-5 + -3"), 1e-10);
		assertEquals(5.5, ev("2.5 + 3.0"), 1e-10);
		assertEquals(1000000.0, ev("500000 + 500000"), 1e-10);
		assertEquals(0.75, ev("-1.25 + 2.0"), 1e-10);
		assertEquals(6.0, ev("1 + 2 + 3"), 1e-10);
		assertEquals(2.0, ev("5 - 3"), 1e-10);
		assertEquals(8.0, ev("8 - 0"), 1e-10);
		assertEquals(5.0, ev("2 - -3"), 1e-10);
		assertEquals(-8.0, ev("-5 - 3"), 1e-10);
		assertEquals(-2.0, ev("-5 - -3"), 1e-10);
		assertEquals(0.5, ev("3.0 - 2.5"), 1e-10);
		assertEquals(-5.0, ev("2 - 7"), 1e-10);
		assertEquals(500000.0, ev("1000000 - 500000"), 1e-10);
		assertEquals(4.0, ev("10 - 4 - 2"), 1e-10);
		assertEquals(4.0, ev("1 + 2 - 3 + 4"), 1e-10);
		assertEquals(1.0, ev("5 - 3 + 2 - 3"), 1e-10);
		assertEquals(-5.0, ev("-5"), 1e-10);
		assertEquals(-3.0, ev("-5 + 2"), 1e-10);
		assertEquals(8.0, ev("5 - -3"), 1e-10);
	}

	@Test
	public void testMulDivMod() {

		assertEquals(6.0, ev("2 * 3"), 1e-10);
		assertEquals(0.0, ev("7 * 0"), 1e-10);
		assertEquals(-6.0, ev("2 * -3"), 1e-10);
		assertEquals(15.0, ev("-5 * -3"), 1e-10);
		assertEquals(7.5, ev("2.5 * 3.0"), 1e-10);
		assertEquals(1000000.0, ev("1000 * 1000"), 1e-10);

		assertEquals(2.0, ev("6 / 3"), 1e-10);
		assertEquals(-2.0, ev("6 / -3"), 1e-10);
		assertEquals(0.5, ev("3 / 6"), 1e-10);
		assertEquals(-2.5, ev("-5 / 2"), 1e-10);
		assertEquals(1.0, ev("10 / 10"), 1e-10);
		assertEquals(0.2, ev("1.0 / 5.0"), 1e-10);

		assertThrows(Exception.class, () -> ev("10 / 0"));
		assertThrows(Exception.class, () -> ev("5.0 / 0.0"));

		assertEquals(1.0, ev("7 % 3"), 1e-10);
		assertEquals(0.0, ev("6 % 3"), 1e-10);
		assertEquals(2.0, ev("10 % 4"), 1e-10);
		assertEquals(1.0, ev("5 % 2"), 1e-10);
		assertEquals(-1.0, ev("-7 % 3"), 1e-10);
		assertEquals(-2.0, ev("-10 % 4"), 1e-10);
		assertEquals(1.0, ev("7 % -3"), 1e-10);

		assertThrows(Exception.class, () -> ev("10 % 0"));
		assertThrows(Exception.class, () -> ev("5.0 % 0.0"));

		assertEquals(1.0, ev("7 MOD 3"), 1e-10);
		assertEquals(0.0, ev("6 MOD 3"), 1e-10);
		assertEquals(2.0, ev("10 MOD 4"), 1e-10);

		assertEquals(1.0, ev("7 mod 3"), 1e-10);
		assertEquals(0.0, ev("6 mod 3"), 1e-10);

		assertEquals(1.0, ev("7 Mod 3"), 1e-10);
		assertEquals(0.0, ev("6 Mod 3"), 1e-10);

		assertEquals(14.0, ev("2 + 3 * 4"), 1e-10);
		assertEquals(7.0, ev("10 - 6 / 2"), 1e-10);
		assertEquals(9.0, ev("2 * 5 - 1"), 1e-10);
		assertEquals(1.0, ev("8 / 4 - 1"), 1e-10);
		assertEquals(2.0, ev("1 + 6 % 5"), 1e-10);
		assertEquals(10.0, ev("10 + 2 * 0"), 1e-10);
		assertEquals(1.5, ev("12 / 6 * 2 / 4 + 0.5"), 1e-10);

		assertEquals(20.0, ev("(2 + 3) * 4"), 1e-10);
		assertEquals(2.0, ev("10 / (2 + 3)"), 1e-10);
		assertEquals(13.0, ev("2 * (3 + 4) - 5 / (6 - 1)"), 1e-10);
		assertEquals(0.0, ev("(7 - 1) % 3"), 1e-10);
	}

	@Test
	public void testPower() {

		assertEquals(8.0, ev("2^3"), 1e-10);
		assertEquals(9.0, ev("3**2"), 1e-10);
		assertEquals(16.0, ev("2^4"), 1e-10);
		assertEquals(125.0, ev("5**3"), 1e-10);

		assertEquals(1.0, ev("5^0"), 1e-10);
		assertEquals(1.0, ev("123.45**0"), 1e-10);
		assertEquals(0.0, ev("0^5"), 1e-10);
		assertEquals(1.0, ev("1^10"), 1e-10);
		assertEquals(1.0, ev("1**100"), 1e-10);
		assertEquals(0.0, ev("0.0^5.0"), 1e-10);
		assertEquals(1.0, ev("0^0"), 1e-10);

		assertEquals(0.25, ev("2^-2"), 1e-10);
		assertEquals(0.125, ev("2.0**-3.0"), 1e-10);
		assertEquals(0.04, ev("5^-2"), 1e-10);

		assertEquals(3.0, ev("9^0.5"), 1e-10);
		assertEquals(2.0, ev("8**(1/3.0)"), 1e-10);
		assertEquals(2.0, ev("8**(0.333333333333333333)"), 1e-10);

		assertEquals(6.25, ev("2.5^2"), 1e-10);
		assertEquals(0.0625, ev("0.5**4"), 1e-10);

		assertEquals(-8.0, ev("(-2)^3"), 1e-10);
		assertEquals(4.0, ev("(-2)^2"), 1e-10);
		assertEquals(Double.NaN, ev("(-4)^0.5"), 1e-10);

		assertEquals(10.0, ev("2 + 2^3"), 1e-10);
		assertEquals(18.0, ev("2 * 3^2"), 1e-10);
		assertEquals(1.0, ev("16 / 2^3 + 1 - 2"), 1e-10);

		assertEquals(512.0, ev("2^3^2"), 1e-10);
		assertEquals(81.0, ev("3**2**2"), 1e-10);
		assertEquals(64.0, ev("(2^3)^2"), 1e-10);

		assertEquals(50.0, ev("(2 + 3)^2 * 2"), 1e-10);
		assertEquals(17.0, ev("3 + (2^3 - 1) * 2"), 1e-10);

		assertEquals(1.0E308, ev("10^308"), 1e-10);
		assertEquals(1.0E-308, ev("0.1^308"), 1e-10);
	}

	@Test
	public void testComp() {

		assertTrue(evb("5 == 5"));
		assertTrue(evb("5 = 5"));
		assertFalse(evb("5 == 6"));
		assertFalse(evb("5 = 6"));
		assertTrue(evb("3.14 == 3.14"));
		assertFalse(evb("3.14 == 3.141"));
		assertTrue(evb("0 == 0"));
		assertFalse(evb("0 == 1"));
		assertTrue(evb("(2 + 3) == 5"));
		assertFalse(evb("(2 + 3) == 6"));

		assertTrue(evb("5 != 6"));
		assertTrue(evb("5 <> 6"));
		assertFalse(evb("5 != 5"));
		assertFalse(evb("5 <> 5"));
		assertTrue(evb("3.14 != 3.141"));
		assertFalse(evb("3.14 != 3.14"));
		assertTrue(evb("0 != 1"));
		assertFalse(evb("0 != 0"));
		assertTrue(evb("(2 + 3) != 6"));
		assertFalse(evb("(2 + 3) != 5"));

		assertTrue(evb("5 < 6"));
		assertFalse(evb("6 < 5"));
		assertFalse(evb("5 < 5"));
		assertTrue(evb("3.0 < 3.1"));
		assertFalse(evb("3.1 < 3.0"));
		assertFalse(evb("3.0 < 3.0"));
		assertTrue(evb("-5 < -2"));
		assertFalse(evb("-2 < -5"));
		assertTrue(evb("2 * 3 < 7"));

		assertTrue(evb("6 > 5"));
		assertFalse(evb("5 > 6"));
		assertFalse(evb("5 > 5"));
		assertTrue(evb("3.1 > 3.0"));
		assertFalse(evb("3.0 > 3.1"));
		assertFalse(evb("3.0 > 3.0"));
		assertTrue(evb("-2 > -5"));
		assertFalse(evb("-5 > -2"));
		assertTrue(evb("10 / 2 > 4"));

		assertTrue(evb("5 <= 6"));
		assertTrue(evb("5 <= 5"));
		assertFalse(evb("6 <= 5"));
		assertTrue(evb("3.0 <= 3.1"));
		assertTrue(evb("3.0 <= 3.0"));
		assertFalse(evb("3.1 <= 3.0"));
		assertTrue(evb("-5 <= -2"));
		assertTrue(evb("-5 <= -5"));
		assertTrue(evb("2^3 <= 8"));

		assertTrue(evb("6 >= 5"));
		assertTrue(evb("5 >= 5"));
		assertFalse(evb("5 >= 6"));
		assertTrue(evb("3.1 >= 3.0"));
		assertTrue(evb("3.0 >= 3.0"));
		assertFalse(evb("3.0 >= 3.1"));
		assertTrue(evb("-2 >= -5"));
		assertTrue(evb("-5 >= -5"));
		assertTrue(evb("10 / 2 >= 5"));

		assertTrue(evb("2 + 3 == 5"));
		assertFalse(evb("2 * 3 > 7"));
		assertTrue(evb("10 - 2 <= 8"));
		assertFalse(evb("10 / 2 != 5"));
		assertTrue(evb("2^3 >= 7"));
		assertTrue(evb("5 + 2 * 3 > 10"));
		assertTrue(evb("15 % 4 <= 3"));
		assertFalse(evb("10 / 2 * 3 != 15"));
	}

	@Test
	public void testLogical() {

		assertTrue(evb("NOT (5 == 6)"));
		assertFalse(evb("NOT (5 == 5)"));
		assertTrue(evb("not (10 < 5)"));
		assertFalse(evb("not (10 > 5)"));
		assertTrue(evb("!(5 == 6)"));
		assertFalse(evb("!(5 == 5)"));

		assertTrue(evb("(5 == 5) AND (6 == 6)"));
		assertFalse(evb("(5 == 5) AND (6 == 7)"));
		assertFalse(evb("(5 == 6) AND (6 == 6)"));
		assertFalse(evb("(5 == 6) AND (6 == 7)"));
		assertTrue(evb("(2 < 3) and (4 > 1)"));
		assertFalse(evb("(2 < 3) and (4 < 1)"));
		assertTrue(evb("(5 == 5) & (6 == 6)"));

		assertTrue(evb("(5 == 5) OR (6 == 6)"));
		assertTrue(evb("(5 == 5) OR (6 == 7)"));
		assertTrue(evb("(5 == 6) OR (6 == 6)"));
		assertFalse(evb("(5 == 6) OR (6 == 7)"));
		assertTrue(evb("(2 < 3) or (4 < 1)"));
		assertTrue(evb("(5 == 5) | (6 == 7)"));

		assertTrue(evb("(2 + 3 == 5) AND (10 > 9)"));
		assertFalse(evb("(2 * 4 < 5) OR (7 == 7 AND 8 != 8)"));
		assertFalse(evb("NOT (5 * 2 == 10) OR (3 + 1 == 5)"));
		assertTrue(evb("!(10 / 2 < 3) AND (7 % 2 == 1)"));
		assertTrue(evb("(2^3 == 8) AND (1 = 1)"));

		assertTrue(evb("(5 > 3 AND 7 < 10) OR (2 == 2 AND 4 != 4)"));
		assertFalse(evb("NOT ((1 + 1 == 2) OR (3 * 3 == 8))"));
		assertTrue(evb("((10 > 5) AND !(12 < 10)) OR (1 + 1 == 3)"));
	}

	@Test
	public void testIfThenElse() {
		assertEquals(10.0, ev("IF 5 > 3 THEN 10 ELSE 20"), 1e-10);
		assertEquals(20.0, ev("IF (5 < 3) THEN 10 ELSE 20"), 1e-10);
		assertEquals(1.0, ev("IF 2 == 2 THEN 1 ELSE 0"), 1e-10);
		assertEquals(0.0, ev("IF (2 != 2) THEN 1 ELSE 0"), 1e-10);

		assertTrue(evb("IF (5 > 3) THEN (7 == 7) ELSE (8 == 9)"));
		assertFalse(evb("IF (5 < 3) THEN (7 == 7) ELSE (8 == 9)"));
		assertTrue(evb("IF (10 >= 10) THEN (1 == 1) ELSE (2 == 3)"));
		assertFalse(evb("IF (10 < 10) THEN (1 == 1) ELSE (2 == 3)"));

		assertEquals(100.0, ev("IF (5 > 3) THEN (IF (2 == 2) THEN 100 ELSE 200) ELSE 300"), 1e-10);
		assertEquals(200.0, ev("IF (5 > 3) THEN (IF (2 != 2) THEN 100 ELSE 200) ELSE 300"), 1e-10);
		assertEquals(300.0, ev("IF (5 < 3) THEN (IF (2 == 2) THEN 100 ELSE 200) ELSE 300"), 1e-10);

		assertEquals(15.0, ev("IF 3 * 2 == 6 THEN 10 + 5 ELSE 20 - 5"), 1e-10);
		assertEquals(15.0, ev("IF (3 * 2 != 6) THEN (10 + 5) ELSE (20 - 5)"), 1e-10);
		assertEquals(25.0, ev("IF (5^2 == 25) THEN (2 * 10 + 5) ELSE (100 / 4)"), 1e-10);

		assertEquals(42.0, ev("IF (5 > 3) AND (10 == 10) THEN 42 ELSE 99"), 1e-10);
		assertEquals(99.0, ev("IF ((5 < 3) AND (10 == 10)) THEN 42 ELSE 99"), 1e-10);
		assertEquals(42.0, ev("IF ((5 < 3) OR (10 == 10)) THEN 42 ELSE 99"), 1e-10);
		assertEquals(42.0, ev("IF (NOT (5 < 3)) THEN 42 ELSE 99"), 1e-10);

		assertEquals(7.0, ev("IF 2 + 3 == 5 THEN 3 + 4 ELSE 5 + 6"), 1e-10);
		assertEquals(11.0, ev("IF (2 + 3 != 5) THEN (3 + 4) ELSE (5 + 6)"), 1e-10);

		assertThrows(Exception.class, () -> ev("IF 5 THEN 10 ELSE 20"));
		assertThrows(Exception.class, () -> ev("IF (2 + 3) THEN 10 ELSE 20"));
	}

	@Test
	public void testVars() {
		var ctx = new EvalContext()
			.bind("a", 20)
			.bind("b", 2);
		assertEquals(42, eval("a * b + 2", ctx).asNum(), 1e-10);

		// basic variable substitution
		assertEquals(25.0, eval("x + y",
			new EvalContext()
				.bind("x", 10)
				.bind("y", 15)).asNum(), 1e-10);
		assertEquals(50.0, eval("price * quantity",
			new EvalContext()
				.bind("price", 5.0)
				.bind("quantity", 10)).asNum(), 1e-10);

		// variables in complex expressions
		assertEquals(11.025, eval("base * (1 + rate)^time",
			new EvalContext()
				.bind("base", 10)
				.bind("rate", 0.05)
				.bind("time", 2)).asNum(), 1e-10);

		// variable case insensitivity
		var caseCtx = new EvalContext()	.bind("Value", 42);
		assertEquals(42.0, eval("Value", caseCtx).asNum(), 1e-10);
		assertEquals(42.0, eval("value", caseCtx).asNum(), 1e-10);
		assertEquals(42.0, eval("VALUE", caseCtx).asNum(), 1e-10);

		// variables with special numeric values
		var specialCtx = new EvalContext()
			.bind("zero", 0)
			.bind("negative", -15.5)
			.bind("fraction", 0.333333)
			.bind("large", 1e6)
			.bind("small", 1e-6);
		assertEquals(0.0, eval("zero", specialCtx).asNum(), 1e-10);
		assertEquals(-15.5, eval("negative", specialCtx).asNum(), 1e-10);
		assertEquals(0.333333, eval("fraction", specialCtx).asNum(), 1e-10);
		assertEquals(1e6, eval("large", specialCtx).asNum(), 1e-10);
		assertEquals(1e-6, eval("small", specialCtx).asNum(), 1e-10);

		// variables in logical expressions
		var logicalCtx = new EvalContext()
			.bind("isTrue", 1)
			.bind("isFalse", 0)
			.bind("threshold", 50);
		assertTrue(eval("isTrue == 1", logicalCtx).asBool());
		assertFalse(eval("isFalse == 1", logicalCtx).asBool());
		assertTrue(eval("threshold > 25", logicalCtx).asBool());

		// variables in IF-THEN-ELSE
		var condCtx = new EvalContext()
			.bind("condition", 1)
			.bind("trueValue", 42)
			.bind("falseValue", 99);
		assertEquals(42.0,
			eval("IF (condition == 1) THEN trueValue ELSE falseValue", condCtx)
				.asNum(), 1e-10);

		condCtx.bind("condition", 0);
		assertEquals(99.0,
			eval("IF (condition == 1) THEN trueValue ELSE falseValue", condCtx)
				.asNum(), 1e-10);

		// variable shadowing/overriding
		var shadowCtx = new EvalContext()
			.bind("x", 10)
			.bind("x", 20); // override the previous value
		assertEquals(20.0, eval("x", shadowCtx).asNum(), 1e-10);

		// variables with underscores and numbers
		var namingCtx = new EvalContext()
			.bind("var_1", 10)
			.bind("var_2", 20)
			.bind("total_sum", 100)
			.bind("x1", 5)
			.bind("y2", 15);
		assertEquals(30.0, eval("var_1 + var_2", namingCtx).asNum(), 1e-10);
		assertEquals(100.0, eval("total_sum", namingCtx).asNum(), 1e-10);
		assertEquals(20.0, eval("x1 + y2", namingCtx).asNum(), 1e-10);

		// Test variables in modulo operations
		var modCtx = new EvalContext()
			.bind("dividend", 17)
			.bind("divisor", 5);
		assertEquals(2.0, eval("dividend % divisor", modCtx).asNum(), 1e-10);
		assertEquals(2.0, eval("dividend MOD divisor", modCtx).asNum(), 1e-10);

		// variables in power operations
		var powerCtx = new EvalContext()
			.bind("base_val", 2)
			.bind("exponent", 8);
		assertEquals(256.0, eval("base_val^exponent", powerCtx).asNum(), 1e-10);
		assertEquals(256.0, eval("base_val**exponent", powerCtx).asNum(), 1e-10);

		// multiple variables in nested operations
		var nestedCtx = new EvalContext()
			.bind("a1", 2)
			.bind("b1", 3)
			.bind("c1", 4)
			.bind("d1", 5);
		assertEquals(50.0, eval("(a1 + b1) * (c1 + d1) + (a1 * c1) - b1", nestedCtx)
			.asNum(), 1e-10);

		// undefined variable should throw exception
		assertThrows(Exception.class,
			() -> eval("undefined_var", new EvalContext()));
		assertThrows(Exception.class,
			() -> eval("x + y", new EvalContext().bind("x", 10)));

			// variables with extreme values
		var extremeCtx = new EvalContext()
			.bind("maxVal", Double.MAX_VALUE)
			.bind("minVal", Double.MIN_VALUE)
			.bind("posInf", Double.POSITIVE_INFINITY)
			.bind("negInf", Double.NEGATIVE_INFINITY)
			.bind("notANumber", Double.NaN);
		assertEquals(Double.MAX_VALUE, eval("maxVal", extremeCtx).asNum(), 1e-10);
		assertEquals(Double.MIN_VALUE, eval("minVal", extremeCtx).asNum(), 1e-10);
		assertEquals(Double.POSITIVE_INFINITY, eval("posInf", extremeCtx).asNum(), 1e-10);
		assertEquals(Double.NEGATIVE_INFINITY, eval("negInf", extremeCtx).asNum(), 1e-10);
		assertEquals(Double.NaN, eval("notANumber", extremeCtx).asNum(), 1e-10);
	}

	@Test
	public void testFunctions() {
		// Single argument functions
		assertEquals(5.0, ev("ABS(-5)"), 1e-10);
		assertEquals(5.0, ev("abs(-5)"), 1e-10);
		assertEquals(2.0, ev("SQRT(4)"), 1e-10);
		assertEquals(2.0, ev("sqrt(4)"), 1e-10);
		assertEquals(1.0, ev("SIN(1.5707963267948966)"), 1e-6); // π/2
		assertEquals(1.0, ev("sin(1.5707963267948966)"), 1e-6);
		assertEquals(1.0, ev("COS(0)"), 1e-10);
		assertEquals(1.0, ev("cos(0)"), 1e-10);
		assertEquals(1.0, ev("TAN(0.7853981633974483)"), 1e-6); // π/4
		assertEquals(2.718281828459045, ev("EXP(1)"), 1e-10);
		assertEquals(1.0, ev("LN(2.718281828459045)"), 1e-10);
		assertEquals(2.0, ev("LOG10(100)"), 1e-10);
		assertEquals(3.0, ev("INT(3.7)"), 1e-10);

		// Arc functions
		assertEquals(1.5707963267948966, ev("ARCCOS(0)"), 1e-10); // π/2
		assertEquals(1.5707963267948966, ev("ACOS(0)"), 1e-10);
		assertEquals(1.5707963267948966, ev("ARCSIN(1)"), 1e-10);
		assertEquals(1.5707963267948966, ev("ASIN(1)"), 1e-10);
		assertEquals(0.7853981633974483, ev("ARCTAN(1)"), 1e-10); // π/4
		assertEquals(0.7853981633974483, ev("ATAN(1)"), 1e-10);

		// Multi-argument functions
		assertEquals(8.0, ev("MAX(3, 8, 5)"), 1e-10);
		assertEquals(3.0, ev("MIN(3, 8, 5)"), 1e-10);
		assertEquals(16.0, ev("SUM(3, 8, 5)"), 1e-10);

		// Functions with single arguments (instead of no arguments)
		assertEquals(5.0, ev("SUM(5)"), 1e-10);

		// Nested function calls
		assertEquals(1.0, ev("ABS(SIN(-1.5707963267948966))"), 1e-6); // |sin(-π/2)| = |-1| = 1
		assertEquals(2.0, ev("SQRT(MAX(1, 4, 2))"), 1e-10);
		assertEquals(5.0, ev("SUM(ABS(-2), SQRT(9))"), 1e-10);

		// Functions in expressions
		assertEquals(7.0, ev("2 + ABS(-5)"), 1e-10);
		assertEquals(15.0, ev("3 * MIN(7, 5, 9)"), 1e-10);
		assertEquals(6.0, ev("SQRT(4) * MAX(1, 3, 2)"), 1e-10);

		// Functions with variables
		var funcCtx = new EvalContext().bind("x", -3).bind("y", 4);
		assertEquals(5.0, eval("SQRT(x * x + y * y)", funcCtx).asNum(), 1e-10);
		assertEquals(7.0, eval("ABS(x) + y", funcCtx).asNum(), 1e-10);
		assertEquals(4.0, eval("MAX(ABS(x), y)", funcCtx).asNum(), 1e-10);

		// Error cases - unknown functions
		try {
			ev("UNKNOWN(5)");
			fail("Should throw exception for unknown function");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("unknown function: UNKNOWN"));
		}
	}

	@Test
	public void testArrayAccess() {
		// Create a context with tensor variables
		var ctx = new EvalContext();

		// Create 1D tensor
		var dim1 = Dimension.of("Products", "PET", "PVC", "Nylon");
		var products = Tensor.of(dim1);
		products.set(Subscript.of("PET"), Cell.of(10.0));
		products.set(Subscript.of("PVC"), Cell.of(20.0));
		products.set(Subscript.of("Nylon"), Cell.of(30.0));
		ctx.bind(new Auxil(Id.of("products"), Cell.of(products), "-"));

		// Create 2D tensor
		var dim2 = Dimension.of("Location", "US", "DE", "FR");
		var prices = Tensor.of(dim1, dim2);
		prices.set(List.of(Subscript.of("PET"), Subscript.of("US")), Cell.of(1.0));
		prices.set(List.of(Subscript.of("PET"), Subscript.of("DE")), Cell.of(1.1));
		prices.set(List.of(Subscript.of("PVC"), Subscript.of("US")), Cell.of(2.0));
		prices.set(List.of(Subscript.of("Nylon"), Subscript.of("FR")), Cell.of(3.5));
		ctx.bind(new Auxil(Id.of("prices"), Cell.of(prices), "-"));

		// Test 1D array access with identifier subscripts
		assertEquals(10.0, eval("products[PET]", ctx).asNum(), 1e-10);
		assertEquals(20.0, eval("products[PVC]", ctx).asNum(), 1e-10);
		assertEquals(30.0, eval("products[Nylon]", ctx).asNum(), 1e-10);

		// Test 1D array access with integer subscripts (1-based indexing)
		assertEquals(10.0, eval("products[1]", ctx).asNum(), 1e-10);
		assertEquals(20.0, eval("products[2]", ctx).asNum(), 1e-10);
		assertEquals(30.0, eval("products[3]", ctx).asNum(), 1e-10);

		// Test 2D array access
		assertEquals(1.0, eval("prices[PET, US]", ctx).asNum(), 1e-10);
		assertEquals(1.1, eval("prices[PET, DE]", ctx).asNum(), 1e-10);
		assertEquals(2.0, eval("prices[PVC, US]", ctx).asNum(), 1e-10);
		assertEquals(3.5, eval("prices[Nylon, FR]", ctx).asNum(), 1e-10);

		// Test mixed subscripts (identifier and integer)
		assertEquals(1.0, eval("prices[1, 1]", ctx).asNum(), 1e-10);
		assertEquals(2.0, eval("prices[PVC, 1]", ctx).asNum(), 1e-10);

		// Error cases
		try {
			eval("products[0]", ctx); // zero index
			fail("Should throw exception for zero index");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("must be positive"));
		}

		try {
			eval("products[1.5]", ctx); // decimal subscript
			fail("Should throw exception for decimal subscript");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("must be a positive integer"));
		}

		try {
			eval("nonexistent[1]", ctx); // unknown variable
			fail("Should throw exception for unknown variable");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("unknown variable"));
		}

		// Test with regular numeric variable (should fail)
		ctx.bind("number", 42.0);
		try {
			eval("number[1]", ctx);
			fail("Should throw exception when accessing non-tensor as array");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("is not a tensor"));
		}
	}

	@Test
	public void testComments() {
		// Basic arithmetic with comments
		assertEquals(5.0, ev("2 {adding} + 3"), 1e-10);
		assertEquals(5.0, ev("{start} 2 + 3 {end}"), 1e-10);
		assertEquals(5.0, ev("2 + {some comment} 3"), 1e-10);
		assertEquals(5.0, ev("{prefix} 2 {middle} + {between} 3 {suffix}"), 1e-10);

		// Multiple operations with comments
		assertEquals(17.0, ev("2 {first} + 3 {second} * 4 {third} + 3 {fourth}"), 1e-10);
		assertEquals(20.0, ev("(2 {adding} + 3) {result} * 4 {multiplying}"), 1e-10);

		// Comments in parentheses
		assertEquals(5.0, ev("({start comment} 2 + 3 {end comment})"), 1e-10);
		assertEquals(15.0, ev("({left side} 2 + 3 {right side}) * 3"), 1e-10);

		// Comments in division and multiplication
		assertEquals(6.0, ev("2 {multiply} * 3 {by three}"), 1e-10);
		assertEquals(2.0, ev("6 {divide} / 3 {by three}"), 1e-10);
		assertEquals(1.0, ev("7 {remainder} % 3 {modulo three}"), 1e-10);
		assertEquals(1.0, ev("7 {remainder} MOD {keyword} 3 {modulo three}"), 1e-10);

		// Comments in power operations
		assertEquals(8.0, ev("2 {base} ^ 3 {exponent}"), 1e-10);
		assertEquals(9.0, ev("3 {base} ** {power operator} 2 {exponent}"), 1e-10);
		assertEquals(512.0, ev("2 {base} ^ 3 {first power} ^ 2 {second power}"), 1e-10);

		// Comments in unary operations
		assertEquals(-5.0, ev("{negative} - 5"), 1e-10);
		assertEquals(5.0, ev("{positive} + 5"), 1e-10);
		assertEquals(-3.0, ev("{negative} - 5 {minus} + 2"), 1e-10);

		// Comments in comparison operations
		assertTrue(evb("5 {left} == 5 {right}"));
		assertTrue(evb("5 {left} = {equals} 5 {right}"));
		assertFalse(evb("5 {left} != {not equal} 5 {right}"));
		assertTrue(evb("5 {left} != {not equal} 6 {right}"));
		assertTrue(evb("5 {left} < {less than} 6 {right}"));
		assertTrue(evb("6 {left} > {greater than} 5 {right}"));
		assertTrue(evb("5 {left} <= {less or equal} 5 {right}"));
		assertTrue(evb("5 {left} >= {greater or equal} 5 {right}"));

		// Comments in logical operations
		assertTrue(evb("NOT {operator} (5 {left} == 6 {right})"));
		assertTrue(evb("{negation} ! (5 {left} == 6 {right})"));
		assertTrue(evb("not {lowercase} (5 {left} == 6 {right})"));
		assertTrue(evb("(5 {left} == 5 {right}) AND {logical and} (6 {left} == 6 {right})"));
		assertTrue(evb("(5 {left} == 5 {right}) & {ampersand} (6 {left} == 6 {right})"));
		assertTrue(evb("(5 {left} == 5 {right}) OR {logical or} (6 {left} == 7 {right})"));
		assertTrue(evb("(5 {left} == 5 {right}) | {pipe} (6 {left} == 7 {right})"));

		// Comments in IF-THEN-ELSE
		assertEquals(10.0, ev("IF {condition start} 5 > 3 {condition end} THEN {then start} 10 {then end} ELSE {else start} 20 {else end}"), 1e-10);
		assertEquals(20.0, ev("IF {condition} (5 < 3) {end condition} THEN {true branch} 10 {end true} ELSE {false branch} 20 {end false}"), 1e-10);

		// Nested IF-THEN-ELSE with comments
		assertEquals(100.0, ev("IF {outer condition} (5 > 3) {end outer} THEN {outer then} (IF {inner condition} (2 == 2) {end inner} THEN {inner then} 100 {end inner then} ELSE {inner else} 200 {end inner else}) {end outer then} ELSE {outer else} 300 {end outer else}"), 1e-10);

		// Comments with variables
		var ctx = new EvalContext().bind("a", 20).bind("b", 2);
		assertEquals(42.0, eval("a {variable a} * {multiply} b {variable b} + {add} 2 {constant}", ctx).asNum(), 1e-10);

		// Complex expressions with many comments
		assertEquals(46.0, ev("{start} 2 {base} * {multiply} (3 {inner base} + {add} 4 {inner add}) {inner result} + {add to result} 10 {final add} * {multiply final} 3 {final multiplier} + {final add} 2 {final constant}"), 1e-10);

		// Comments in scientific notation
		assertEquals(1500.0, ev("1.5e3 {scientific notation}"), 1e-10);
		assertEquals(0.0015, ev("1.5E-3 {negative exponent}"), 1e-10);

		// Comments with nested parentheses
		assertEquals(26.0, ev("({outer start} 2 {first} + {add} 3 {second} {outer end}) {result} * {multiply} ({inner start} 4 {third} + {add} 1 {fourth} {inner end}) {final} + {final add} 1 {last}"), 1e-10);

		// Comments in complex arithmetic
		assertEquals(14.0, ev("2 {base} + {add} 3 {addend} * {multiply} 4 {multiplier} {following precedence rules}"), 1e-10);
		assertEquals(20.0, ev("({start group} 2 {first} + {add} 3 {second} {end group}) {result} * {multiply} 4 {multiplier} {override precedence}"), 1e-10);

		// Empty comments
		assertEquals(5.0, ev("2 {} + 3"), 1e-10);
		assertEquals(5.0, ev("{} 2 + 3 {}"), 1e-10);

		// Comments with special characters and spaces
		assertEquals(5.0, ev("2 {this is a long comment with spaces} + 3"), 1e-10);
		assertEquals(5.0, ev("2 {comment with numbers 123 and symbols !@#$%^&*()_+-=[]|;:,.<>?} + 3"), 1e-10);
		assertEquals(5.0, ev("2 {comment with\nnewlines\nand\ttabs} + 3"), 1e-10);

		// Test the spec example: a*b { take product of a and b } + c { then add c }
		var specCtx = new EvalContext().bind("a", 3).bind("b", 4).bind("c", 5);
		assertEquals(17.0, eval("a*b { take product of a and b } + c { then add c }", specCtx).asNum(), 1e-10);

		// Multiple consecutive comments
		assertEquals(5.0, ev("2 {first comment} {second comment} + {third comment} {fourth comment} 3"), 1e-10);

		// Comments that could be used to "turn off" parts of equations (as mentioned in spec)
		assertEquals(2.0, ev("2 {* 3 + 4} + 0"), 1e-10); // the multiplication and addition are commented out
		assertEquals(7.0, ev("2 + 3 {* 4} + 2"), 1e-10); // the multiplication is commented out
	}

	private double ev(String eqn) {
		return eval(eqn, new EvalContext()).asNum();
	}

	private boolean evb(String eqn) {
		return eval(eqn, new EvalContext()).asBoolCell().value();
	}

	private Cell eval(String eqn, EvalContext ctx) {
		return Interpreter.of(ctx).eval(eqn).orElseThrow();
	}

}
