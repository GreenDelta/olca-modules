// generated with func1_templ.py
package org.openlca.expressions.functions;

class Cosh extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.cosh(arg);
	}

	@Override
	public String getName() {
		return "cosh";
	}
}
