package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.ShortText;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIAMethodNormalisationAndWeightingType", propOrder = {
		"type",
		"principles",
		"deviationsFromPrinciples",
		"normalisation",
		"usableNormalisationDataSets",
		"normalisationDescription",
		"includedNormalisationDataSets",
		"weighting",
		"usableWeightingDataSets",
		"weightingDescription",
		"includedWeightingDataSets" })
public class NormalisationAndWeighting implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "typeOfDataSet")
	public LCIAMethodType type;

	@XmlElement(name = "LCIAMethodPrinciple")
	public final List<LCIAMethodPrinciple> principles = new ArrayList<>();

	@FreeText
	@XmlElement(name = "deviationsFromLCIAMethodPrinciple")
	public final List<LangString> deviationsFromPrinciples = new ArrayList<>();

	public Boolean normalisation;

	@XmlElement(name = "referenceToUsableNormalisationDataSets")
	public final List<Ref> usableNormalisationDataSets = new ArrayList<>();

	@ShortText
	public final List<LangString> normalisationDescription = new ArrayList<>();

	@XmlElement(name = "referenceToIncludedNormalisationDataSets")
	public final List<Ref> includedNormalisationDataSets = new ArrayList<>();

	public Boolean weighting;

	@XmlElement(name = "referenceToUsableWeightingDataSets")
	public final List<Ref> usableWeightingDataSets = new ArrayList<>();

	@ShortText
	public final List<LangString> weightingDescription = new ArrayList<>();

	@XmlElement(name = "referenceToIncludedWeightingDataSets")
	public final List<Ref> includedWeightingDataSets = new ArrayList<>();

}
