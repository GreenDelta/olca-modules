// generated with func1_templ.py
package org.openlca.expressions.functions;

class Acos extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.acos(arg);
	}

	@Override
	public String getName() {
		return "acos";
	}
}
