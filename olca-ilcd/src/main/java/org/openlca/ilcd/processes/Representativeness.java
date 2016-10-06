
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.math.BigDecimal;
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

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.annotations.FreeText;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataSourcesTreatmentAndRepresentativenessType", propOrder = {
		"dataCutOffAndCompletenessPrinciples",
		"deviationsFromCutOffAndCompletenessPrinciples",
		"dataSelectionAndCombinationPrinciples",
		"deviationsFromSelectionAndCombinationPrinciples",
		"dataTreatmentAndExtrapolationsPrinciples",
		"deviationsFromTreatmentAndExtrapolationPrinciples",
		"referenceToDataHandlingPrinciples",
		"referenceToDataSource",
		"percentageSupplyOrProductionCovered",
		"annualSupplyOrProductionVolume",
		"samplingProcedure",
		"dataCollectionPeriod",
		"uncertaintyAdjustments",
		"useAdviceForDataSet",
		"other"
})
public class Representativeness implements Serializable {

	private final static long serialVersionUID = 1L;

	@FreeText
	public final List<LangString> dataCutOffAndCompletenessPrinciples = new ArrayList<>();

	@FreeText
	public final List<LangString> deviationsFromCutOffAndCompletenessPrinciples = new ArrayList<>();

	@FreeText
	public final List<LangString> dataSelectionAndCombinationPrinciples = new ArrayList<>();

	@FreeText
	public final List<LangString> deviationsFromSelectionAndCombinationPrinciples = new ArrayList<>();

	@FreeText
	public final List<LangString> dataTreatmentAndExtrapolationsPrinciples = new ArrayList<>();

	@FreeText
	public final List<LangString> deviationsFromTreatmentAndExtrapolationPrinciples = new ArrayList<>();

	public final List<DataSetReference> referenceToDataHandlingPrinciples = new ArrayList<>();

	public final List<DataSetReference> referenceToDataSource = new ArrayList<>();

	public BigDecimal percentageSupplyOrProductionCovered;

	@Label
	public final List<LangString> annualSupplyOrProductionVolume = new ArrayList<>();

	@FreeText
	public final List<LangString> samplingProcedure = new ArrayList<>();

	@Label
	public final List<LangString> dataCollectionPeriod = new ArrayList<>();

	@FreeText
	public final List<LangString> uncertaintyAdjustments = new ArrayList<>();

	@FreeText
	public final List<LangString> useAdviceForDataSet = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
