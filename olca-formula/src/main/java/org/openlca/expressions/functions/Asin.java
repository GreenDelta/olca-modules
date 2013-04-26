// generated with func1_templ.py
package org.openlca.expressions.functions;

class Asin extends Function1 {

	@Override
	protected double eval(double arg) {
		return Math.asin(arg);
	}

	@Override
	public String getName() {
		return "asin";
	}
}
