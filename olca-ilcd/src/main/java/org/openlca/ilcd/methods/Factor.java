
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.DataDerivation;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.RecommendationLevel;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CharacterisationFactorType", propOrder = {
		"flow",
		"location",
		"direction",
		"meanValue",
		"minimumValue",
		"maximumValue",
		"uncertaintyDistributionType",
		"relativeStandardDeviation95In",
		"dataDerivationTypeStatus",
		"deviatingRecommendation",
		"dataSources",
		"comment"
})
public class Factor implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToFlowDataSet", required = true)
	public Ref flow;

	public String location;

	@XmlElement(name = "exchangeDirection", required = true)
	public ExchangeDirection direction;

	public double meanValue;

	public Double minimumValue;

	public Double maximumValue;

	public UncertaintyDistribution uncertaintyDistributionType;

	public Double relativeStandardDeviation95In;

	public DataDerivation dataDerivationTypeStatus;

	public RecommendationLevel deviatingRecommendation;

	@XmlElementWrapper(name = "referencesToDataSource")
	@XmlElement(name = "referenceToDataSource")
	public Ref[] dataSources;

	@Label
	@XmlElement(name = "generalComment")
	public final List<LangString> comment = new ArrayList<>();

}
