// generated with func1_templ.py
package org.openlca.expressions.functions;

class Sqrt extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.sqrt(arg);
	}

	@Override
	public String getName() {
		return "sqrt";
	}
}
