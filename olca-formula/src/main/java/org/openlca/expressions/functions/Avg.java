// generated with funcN_templ.py
package org.openlca.expressions.functions;

class Avg extends FunctionN {

	@Override
	protected double getDefault() {
		return 0;
	}

	@Override
	protected double eval(double[] args) {

		double sum = 0;
		for (double arg : args)
			sum += arg;
		return sum / args.length;

	}

	@Override
	public String getName() {
		return "avg";
	}
}
