// generated with func1_templ.py
package org.openlca.expressions.functions;

class Sin extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.sin(arg);
	}

	@Override
	public String getName() {
		return "sin";
	}
}
