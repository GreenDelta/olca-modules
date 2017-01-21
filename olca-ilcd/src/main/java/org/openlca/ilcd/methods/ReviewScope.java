
package org.openlca.ilcd.methods;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ScopeOfReviewValues")
@XmlEnum
public enum ReviewScope {

	/**
	 * Considered physical and chemical properties used for the model.
	 * 
	 */
	@XmlEnumValue("Substance properties, physical and chemical")
	SUBSTANCE_PROPERTIES_PHYSICAL_AND_CHEMICAL("Substance properties, physical and chemical"),

	/**
	 * Considered biological properties of substances used for the model.
	 * 
	 */
	@XmlEnumValue("Substance properties, biological")
	SUBSTANCE_PROPERTIES_BIOLOGICAL("Substance properties, biological"),

	/**
	 * Model for transport and fate of substances in the environment.
	 * 
	 */
	@XmlEnumValue("Model for Transport and Fate")
	MODEL_FOR_TRANSPORT_AND_FATE("Model for Transport and Fate"),

	/**
	 * Model for exposure of included protection targets.
	 * 
	 */
	@XmlEnumValue("Model for Exposure")
	MODEL_FOR_EXPOSURE("Model for Exposure"),

	/**
	 * Model for effect to included protection targets.
	 * 
	 */
	@XmlEnumValue("Model for Effect")
	MODEL_FOR_EFFECT("Model for Effect"),

	/**
	 * Model for calculation of damage effects to included protection targets.
	 * 
	 */
	@XmlEnumValue("Model for Damage")
	MODEL_FOR_DAMAGE("Model for Damage"),

	/**
	 * Review of calculation and results of single characterisation factors
	 * resulting from the model as a whole.
	 * 
	 */
	@XmlEnumValue("Characterisation factors")
	CHARACTERISATION_FACTORS("Characterisation factors"),

	/**
	 * Review of the general application of the model to the LCIA method.
	 * 
	 */
	@XmlEnumValue("Application of model")
	APPLICATION_OF_MODEL("Application of model"),

	/**
	 * Included normalisation, if any.
	 * 
	 */
	@XmlEnumValue("Normalisation")
	NORMALISATION("Normalisation"),

	/**
	 * Included weighting scheme, if any.
	 * 
	 */
	@XmlEnumValue("Weighting")
	WEIGHTING("Weighting"),

	/**
	 * Verification of the appropriateness and correctness of the documentation.
	 * 
	 */
	@XmlEnumValue("Documentation")
	DOCUMENTATION("Documentation");
	private final String value;

	ReviewScope(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ReviewScope fromValue(String v) {
		for (ReviewScope c : ReviewScope.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
