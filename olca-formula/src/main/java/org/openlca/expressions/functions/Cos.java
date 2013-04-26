// generated with func1_templ.py
package org.openlca.expressions.functions;

class Cos extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.cos(arg);
	}

	@Override
	public String getName() {
		return "cos";
	}
}
