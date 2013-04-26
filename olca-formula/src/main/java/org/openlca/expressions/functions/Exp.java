// generated with func1_templ.py
package org.openlca.expressions.functions;

class Exp extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.exp(arg);
	}

	@Override
	public String getName() {
		return "exp";
	}
}
