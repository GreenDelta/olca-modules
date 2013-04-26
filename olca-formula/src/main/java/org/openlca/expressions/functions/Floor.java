// generated with func1_templ.py
package org.openlca.expressions.functions;

class Floor extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.floor(arg);
	}

	@Override
	public String getName() {
		return "floor";
	}
}
