// generated with func1_templ.py
package org.openlca.expressions.functions;

class Ceil extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.ceil(arg);
	}

	@Override
	public String getName() {
		return "ceil";
	}
}
