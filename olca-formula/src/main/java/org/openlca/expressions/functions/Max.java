// generated with funcN_templ.py
package org.openlca.expressions.functions;

class Max extends FunctionN {

	@Override
	protected double getDefault() {
		return 0;
	}

	@Override
	protected double eval(double[] args) {

		double max = -Double.MAX_VALUE;
		for (double arg : args)
			if (arg > max)
				max = arg;
		return max;

	}

	@Override
	public String getName() {
		return "max";
	}
}
