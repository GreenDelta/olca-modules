package org.openlca.io.ecospold2.input;

/**
 * Import configuration for EcoSpold 02 data sets.
 */
public class ImportConfig {

	boolean skipNullExchanges = false;
	boolean withParameters = true;
	boolean withParameterFormulas = true;
	boolean checkFormulas = false;

	/**
	 * Creates a default configuration for the import which has no restrictions
	 * but may lead to systems that cannot be calculated using ecoinvent 3.
	 */
	public static ImportConfig createDefault() {
		return new ImportConfig();
	}

	/**
	 * If true, exchanges with a value of 0 will not be imported (in ecoinvent 3
	 * there are a lot of such exchanges).
	 */
	public void setSkipNullExchanges(boolean skipNullExchanges) {
		this.skipNullExchanges = skipNullExchanges;
	}

	/**
	 * If true, imports parameters from EcoSpold 02 data sets.
	 */
	public void setWithParameters(boolean withParameters) {
		this.withParameters = withParameters;
	}

	/**
	 * If true, parameter formulas are imported ((in ecoinvent 3 there are a lot
	 * of parameters that cannot be evaluated in openLCA).
	 */
	public void setWithParameterFormulas(boolean withParameterFormulas) {
		this.withParameterFormulas = withParameterFormulas;
	}

	/**
	 * If true, formulas that contain functions that are not available in
	 * openLCA are filtered.
	 */
	public void setCheckFormulas(boolean checkFormulas) {
		this.checkFormulas = checkFormulas;
	}

	@Override
	public String toString() {
		return "ImportConfig [skipNullExchanges=" + skipNullExchanges
				+ ", withParameters=" + withParameters
				+ ", withParameterFormulas=" + withParameterFormulas
				+ ", checkFormulas=" + checkFormulas + "]";
	}
}
