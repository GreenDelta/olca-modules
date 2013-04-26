// generated with func1_templ.py
package org.openlca.expressions.functions;

class Tan extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.tan(arg);
	}

	@Override
	public String getName() {
		return "tan";
	}
}
