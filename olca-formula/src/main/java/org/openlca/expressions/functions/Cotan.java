// generated with func1_templ.py
package org.openlca.expressions.functions;

class Cotan extends Function1 {

	@Override
	protected double eval(double arg) {
		return 1 / Math.tan(arg);
	}

	@Override
	public String getName() {
		return "cotan";
	}
}
