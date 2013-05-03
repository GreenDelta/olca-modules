package org.openlca.core.database.internal;

/**
 * An internal process link class used for the creation of product systems.
 */
class ProductLink {

	private ProductExchange output;
	private ProductExchange input;

	public ProductLink(ProductExchange input, ProductExchange output) {
		this.input = input;
		this.output = output;
	}

	public ProductExchange getOutput() {
		return output;
	}

	public void setOutput(ProductExchange output) {
		this.output = output;
	}

	public ProductExchange getInput() {
		return input;
	}

	public void setInput(ProductExchange input) {
		this.input = input;
	}

	public double getScalingFactor() {
		if (output.getAmount() == 0)
			return 0;
		return input.getAmount() / output.getAmount();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((output == null) ? 0 : output.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductLink other = (ProductLink) obj;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (output == null) {
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		return true;
	}

}
