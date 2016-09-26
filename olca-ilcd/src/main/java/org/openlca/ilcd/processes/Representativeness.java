
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
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.Other;

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

	public final List<FreeText> dataCutOffAndCompletenessPrinciples = new ArrayList<>();

	public final List<FreeText> deviationsFromCutOffAndCompletenessPrinciples = new ArrayList<>();

	public final List<FreeText> dataSelectionAndCombinationPrinciples = new ArrayList<>();

	public final List<FreeText> deviationsFromSelectionAndCombinationPrinciples = new ArrayList<>();

	public final List<FreeText> dataTreatmentAndExtrapolationsPrinciples = new ArrayList<>();

	public final List<FreeText> deviationsFromTreatmentAndExtrapolationPrinciples = new ArrayList<>();

	public final List<DataSetReference> referenceToDataHandlingPrinciples = new ArrayList<>();

	public final List<DataSetReference> referenceToDataSource = new ArrayList<>();

	public BigDecimal percentageSupplyOrProductionCovered;

	public final List<Label> annualSupplyOrProductionVolume = new ArrayList<>();

	public final List<FreeText> samplingProcedure = new ArrayList<>();

	public final List<Label> dataCollectionPeriod = new ArrayList<>();

	public final List<FreeText> uncertaintyAdjustments = new ArrayList<>();

	public final List<FreeText> useAdviceForDataSet = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

}
