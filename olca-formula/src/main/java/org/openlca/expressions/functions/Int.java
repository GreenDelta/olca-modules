// generated with func1_templ.py
package org.openlca.expressions.functions;

class Int extends Function1 {

	@Override
	protected double eval(double arg) {
		return (int) arg;
	}

	@Override
	public String getName() {
		return "int";
	}
}
