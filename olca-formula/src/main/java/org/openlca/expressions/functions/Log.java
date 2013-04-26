// generated with func1_templ.py
package org.openlca.expressions.functions;

class Log extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.log10(arg);
	}

	@Override
	public String getName() {
		return "log";
	}
}
