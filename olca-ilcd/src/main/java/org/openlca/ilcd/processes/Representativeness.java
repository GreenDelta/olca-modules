
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSourcesTreatmentAndRepresentativenessType", propOrder = {
		"completeness",
		"completenessComment",
		"dataSelection",
		"dataSelectionComment",
		"dataTreatment",
		"datatTreatmentComment",
		"dataHandlingSources",
		"sources",
		"coveredProduction",
		"productionVolume",
		"samplingProcedure",
		"dataCollectionPeriod",
		"uncertaintyAdjustments",
		"useAdvice",
		"other"
})
public class Representativeness implements Serializable {

	private final static long serialVersionUID = 1L;

	@FreeText
	@XmlElement(name = "dataCutOffAndCompletenessPrinciples")
	public final List<LangString> completeness = new ArrayList<>();

	@FreeText
	@XmlElement(name = "deviationsFromCutOffAndCompletenessPrinciples")
	public final List<LangString> completenessComment = new ArrayList<>();

	@FreeText
	@XmlElement(name = "dataSelectionAndCombinationPrinciples")
	public final List<LangString> dataSelection = new ArrayList<>();

	@FreeText
	@XmlElement(name = "deviationsFromSelectionAndCombinationPrinciples")
	public final List<LangString> dataSelectionComment = new ArrayList<>();

	@FreeText
	@XmlElement(name = "dataTreatmentAndExtrapolationsPrinciples")
	public final List<LangString> dataTreatment = new ArrayList<>();

	@FreeText
	@XmlElement(name = "deviationsFromTreatmentAndExtrapolationPrinciples")
	public final List<LangString> datatTreatmentComment = new ArrayList<>();

	@XmlElement(name = "referenceToDataHandlingPrinciples")
	public final List<Ref> dataHandlingSources = new ArrayList<>();

	@XmlElement(name = "referenceToDataSource")
	public final List<Ref> sources = new ArrayList<>();

	@XmlElement(name = "percentageSupplyOrProductionCovered")
	public Double coveredProduction;

	@Label
	@XmlElement(name = "annualSupplyOrProductionVolume")
	public final List<LangString> productionVolume = new ArrayList<>();

	@FreeText
	@XmlElement(name = "samplingProcedure")
	public final List<LangString> samplingProcedure = new ArrayList<>();

	@Label
	@XmlElement(name = "dataCollectionPeriod")
	public final List<LangString> dataCollectionPeriod = new ArrayList<>();

	@FreeText
	@XmlElement(name = "uncertaintyAdjustments")
	public final List<LangString> uncertaintyAdjustments = new ArrayList<>();

	@FreeText
	@XmlElement(name = "useAdviceForDataSet")
	public final List<LangString> useAdvice = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Representativeness clone() {
		Representativeness clone = new Representativeness();
		LangString.copy(completeness, clone.completeness);
		LangString.copy(completenessComment, clone.completenessComment);
		LangString.copy(dataSelection, clone.dataSelection);
		LangString.copy(dataSelectionComment, clone.dataSelectionComment);
		LangString.copy(dataTreatment, clone.dataTreatment);
		LangString.copy(datatTreatmentComment, clone.datatTreatmentComment);
		Ref.copy(dataHandlingSources, clone.dataHandlingSources);
		Ref.copy(sources, clone.sources);
		clone.coveredProduction = coveredProduction;
		LangString.copy(productionVolume, clone.productionVolume);
		LangString.copy(samplingProcedure, clone.samplingProcedure);
		LangString.copy(dataCollectionPeriod, clone.dataCollectionPeriod);
		LangString.copy(uncertaintyAdjustments, clone.uncertaintyAdjustments);
		LangString.copy(useAdvice, clone.useAdvice);
		if (other != null)
			clone.other = other.clone();
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
