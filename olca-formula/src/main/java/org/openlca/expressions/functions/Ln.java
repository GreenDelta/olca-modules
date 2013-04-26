// generated with func1_templ.py
package org.openlca.expressions.functions;

class Ln extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.log(arg);
	}

	@Override
	public String getName() {
		return "ln";
	}
}
