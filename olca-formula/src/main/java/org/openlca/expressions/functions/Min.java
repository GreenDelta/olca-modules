// generated with funcN_templ.py
package org.openlca.expressions.functions;

class Min extends FunctionN {

	@Override
	protected double getDefault() {
		return 0;
	}

	@Override
	protected double eval(double[] args) {
		double min = Double.MAX_VALUE;
		for (double arg : args)
			if (arg < min)
				min = arg;
		return min;
	}

	@Override
	public String getName() {
		return "min";
	}
}
