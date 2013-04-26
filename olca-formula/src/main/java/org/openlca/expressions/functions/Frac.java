// generated with func1_templ.py
package org.openlca.expressions.functions;

class Frac extends Function1 {

	@Override
	protected double eval(double arg) {
		return arg - ((int) arg);
	}

	@Override
	public String getName() {
		return "frac";
	}
}
