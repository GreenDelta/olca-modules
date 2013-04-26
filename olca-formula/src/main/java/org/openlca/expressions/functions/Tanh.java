// generated with func1_templ.py
package org.openlca.expressions.functions;

class Tanh extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.tanh(arg);
	}

	@Override
	public String getName() {
		return "tanh";
	}
}
