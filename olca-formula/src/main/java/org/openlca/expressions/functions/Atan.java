// generated with func1_templ.py
package org.openlca.expressions.functions;

class Atan extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.atan(arg);
	}

	@Override
	public String getName() {
		return "atan";
	}
}
