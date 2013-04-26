// generated with func1_templ.py
package org.openlca.expressions.functions;

class Abs extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.abs(arg);
	}

	@Override
	public String getName() {
		return "abs";
	}
}
