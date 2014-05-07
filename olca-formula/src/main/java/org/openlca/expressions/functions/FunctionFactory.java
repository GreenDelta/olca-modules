package org.openlca.expressions.functions;

import java.util.HashMap;

import org.openlca.expressions.Expression;
import org.openlca.expressions.OpExponentiation;

public class FunctionFactory {

	protected static FunctionFactory instance = new FunctionFactory();

	protected HashMap<String, Class<? extends Expression>> functions = new HashMap<>();

	public static FunctionFactory getInstance() {
		return instance;
	}

	public FunctionFactory() {

		// constant functions
		registerFunction("pi", Pi.class);
		registerFunction("e", E.class);
		registerFunction("true", True.class);
		registerFunction("false", False.class);

		// arithmetic functions
		registerFunction("abs", Abs.class);
		registerFunction("avg", Avg.class);
		registerFunction("mean", Avg.class);
		registerFunction("ceil", Ceil.class);
		registerFunction("floor", Floor.class);
		registerFunction("frac", Frac.class);
		registerFunction("int", Int.class);
		registerFunction("min", Min.class);
		registerFunction("max", Max.class);
		registerFunction("round", Round.class);
		registerFunction("trunc", Int.class);
		registerFunction("sqr", Sqr.class);
		registerFunction("sqrt", Sqrt.class);
		registerFunction("sum", Sum.class);
		registerFunction("random", Random.class);
		registerFunction("rand", Random.class);

		// exponential and logarithmic functions
		registerFunction("exp", Exp.class);
		registerFunction("ipower", IPow.class);
		registerFunction("ln", Ln.class);
		registerFunction("log", Log.class);
		registerFunction("lg", Log.class);
		registerFunction("power", OpExponentiation.class);
		registerFunction("pow", OpExponentiation.class);

		// trigonometric functions
		registerFunction("acos", Acos.class);
		registerFunction("arccos", Acos.class);
		registerFunction("asin", Asin.class);
		registerFunction("arcsin", Asin.class);
		registerFunction("atan", Atan.class);
		registerFunction("arctan", Atan.class);
		registerFunction("cos", Cos.class);
		registerFunction("cosh", Cosh.class);
		registerFunction("cotan", Cotan.class);
		registerFunction("cot", Cotan.class);
		registerFunction("sin", Sin.class);
		registerFunction("sinh", Sinh.class);
		registerFunction("tan", Tan.class);
		registerFunction("tanh", Tanh.class);

		// boolean functions
		registerFunction("not", Not.class);
		registerFunction("if", If.class);
		registerFunction("iff", If.class);
		registerFunction("iif", If.class);
		registerFunction("and", And.class);
		registerFunction("or", Or.class);
	}

	public void registerFunction(String name, Class<? extends Expression> clazz) {
		if (!Expression.class.isAssignableFrom(clazz))
			throw new RuntimeException(
					"Tried to register an expression which does not implement the Expression inteface");
		functions.put(name, clazz);
	}

	public Expression createFunction(String name) throws Exception {
		Class<? extends Expression> clazz = functions.get(name);
		if (clazz == null)
			return null;
		return clazz.newInstance();
	}
}
