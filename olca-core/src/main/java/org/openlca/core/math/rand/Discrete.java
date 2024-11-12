package org.openlca.core.math.rand;

record Discrete(double val) implements NumberGenerator {

	@Override
	public double next() {
		return val;
	}
}
