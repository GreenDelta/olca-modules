// generated with func1_templ.py
package org.openlca.expressions.functions;

class Sqr extends Function1 {

	@Override
	protected double eval(double arg) {
		return arg * arg;
	}

	@Override
	public String getName() {
		return "sqr";
	}
}
