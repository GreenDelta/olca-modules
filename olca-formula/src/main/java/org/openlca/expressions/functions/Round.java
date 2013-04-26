// generated with func1_templ.py
package org.openlca.expressions.functions;

class Round extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.round(arg);
	}

	@Override
	public String getName() {
		return "round";
	}
}
