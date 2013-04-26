// generated with func2_templ.py
package org.openlca.expressions.functions;

class Pow extends Function2 {

	@Override
	protected double eval(double arg1, double arg2) {
		return Math.pow(arg1, arg2);
	}

	@Override
	public String getName() {
		return "pow";
	}
}
