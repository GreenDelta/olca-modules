// generated with func1_templ.py
package org.openlca.expressions.functions;

class Sinh extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.sinh(arg);
	}

	@Override
	public String getName() {
		return "sinh";
	}
}
