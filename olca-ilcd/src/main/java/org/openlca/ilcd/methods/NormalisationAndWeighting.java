package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.LCIAMethodPrinciple;
import org.openlca.ilcd.commons.ShortText;
import org.openlca.ilcd.commons.TypeOfLCIAMethod;

/**
 * <p>
 * Java class for LCIAMethodNormalisationAndWeightingType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="LCIAMethodNormalisationAndWeightingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="typeOfDataSet" type="{http://lca.jrc.it/ILCD/Common}TypeOfLCIAMethodValues" minOccurs="0"/>
 *         &lt;element name="LCIAMethodPrinciple" type="{http://lca.jrc.it/ILCD/Common}LCIAMethodPrincipleValues" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="deviationsFromLCIAMethodPrinciple" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="normalisation" type="{http://lca.jrc.it/ILCD/Common}boolean" minOccurs="0"/>
 *         &lt;element name="referenceToUsableNormalisationDataSets" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="normalisationDescription" type="{http://lca.jrc.it/ILCD/Common}STMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToIncludedNormalisationDataSets" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="weighting" type="{http://lca.jrc.it/ILCD/Common}boolean" minOccurs="0"/>
 *         &lt;element name="referenceToUsableWeightingDataSets" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="weightingDescription" type="{http://lca.jrc.it/ILCD/Common}STMultiLang" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="referenceToIncludedWeightingDataSets" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LCIAMethodNormalisationAndWeightingType", propOrder = {
		"typeOfDataSet", "lciaMethodPrinciple",
		"deviationsFromLCIAMethodPrinciple", "normalisation",
		"referenceToUsableNormalisationDataSets", "normalisationDescription",
		"referenceToIncludedNormalisationDataSets", "weighting",
		"referenceToUsableWeightingDataSets", "weightingDescription",
		"referenceToIncludedWeightingDataSets" })
public class NormalisationAndWeighting implements Serializable {

	private final static long serialVersionUID = 1L;
	protected TypeOfLCIAMethod typeOfDataSet;
	@XmlElement(name = "LCIAMethodPrinciple")
	protected List<LCIAMethodPrinciple> lciaMethodPrinciple;
	protected List<FreeText> deviationsFromLCIAMethodPrinciple;
	protected Boolean normalisation;
	protected List<DataSetReference> referenceToUsableNormalisationDataSets;
	protected List<ShortText> normalisationDescription;
	protected List<DataSetReference> referenceToIncludedNormalisationDataSets;
	protected Boolean weighting;
	protected List<DataSetReference> referenceToUsableWeightingDataSets;
	protected List<ShortText> weightingDescription;
	protected List<DataSetReference> referenceToIncludedWeightingDataSets;

	/**
	 * Gets the value of the typeOfDataSet property.
	 * 
	 * @return possible object is {@link TypeOfLCIAMethod }
	 * 
	 */
	public TypeOfLCIAMethod getTypeOfDataSet() {
		return typeOfDataSet;
	}

	/**
	 * Sets the value of the typeOfDataSet property.
	 * 
	 * @param value
	 *            allowed object is {@link TypeOfLCIAMethod }
	 * 
	 */
	public void setTypeOfDataSet(TypeOfLCIAMethod value) {
		this.typeOfDataSet = value;
	}

	/**
	 * Gets the value of the lciaMethodPrinciple property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the lciaMethodPrinciple property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getLCIAMethodPrinciple().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link LCIAMethodPrinciple }
	 * 
	 * 
	 */
	public List<LCIAMethodPrinciple> getLCIAMethodPrinciple() {
		if (lciaMethodPrinciple == null) {
			lciaMethodPrinciple = new ArrayList<>();
		}
		return this.lciaMethodPrinciple;
	}

	/**
	 * Gets the value of the deviationsFromLCIAMethodPrinciple property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the deviationsFromLCIAMethodPrinciple
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDeviationsFromLCIAMethodPrinciple().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link FreeText }
	 * 
	 * 
	 */
	public List<FreeText> getDeviationsFromLCIAMethodPrinciple() {
		if (deviationsFromLCIAMethodPrinciple == null) {
			deviationsFromLCIAMethodPrinciple = new ArrayList<>();
		}
		return this.deviationsFromLCIAMethodPrinciple;
	}

	/**
	 * Gets the value of the normalisation property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isNormalisation() {
		return normalisation;
	}

	/**
	 * Sets the value of the normalisation property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setNormalisation(Boolean value) {
		this.normalisation = value;
	}

	/**
	 * Gets the value of the referenceToUsableNormalisationDataSets property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToUsableNormalisationDataSets
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToUsableNormalisationDataSets().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToUsableNormalisationDataSets() {
		if (referenceToUsableNormalisationDataSets == null) {
			referenceToUsableNormalisationDataSets = new ArrayList<>();
		}
		return this.referenceToUsableNormalisationDataSets;
	}

	/**
	 * Gets the value of the normalisationDescription property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the normalisationDescription property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNormalisationDescription().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ShortText }
	 * 
	 * 
	 */
	public List<ShortText> getNormalisationDescription() {
		if (normalisationDescription == null) {
			normalisationDescription = new ArrayList<>();
		}
		return this.normalisationDescription;
	}

	/**
	 * Gets the value of the referenceToIncludedNormalisationDataSets property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToIncludedNormalisationDataSets
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToIncludedNormalisationDataSets().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToIncludedNormalisationDataSets() {
		if (referenceToIncludedNormalisationDataSets == null) {
			referenceToIncludedNormalisationDataSets = new ArrayList<>();
		}
		return this.referenceToIncludedNormalisationDataSets;
	}

	/**
	 * Gets the value of the weighting property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isWeighting() {
		return weighting;
	}

	/**
	 * Sets the value of the weighting property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setWeighting(Boolean value) {
		this.weighting = value;
	}

	/**
	 * Gets the value of the referenceToUsableWeightingDataSets property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToUsableWeightingDataSets
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToUsableWeightingDataSets().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToUsableWeightingDataSets() {
		if (referenceToUsableWeightingDataSets == null) {
			referenceToUsableWeightingDataSets = new ArrayList<>();
		}
		return this.referenceToUsableWeightingDataSets;
	}

	/**
	 * Gets the value of the weightingDescription property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the weightingDescription property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getWeightingDescription().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ShortText }
	 * 
	 * 
	 */
	public List<ShortText> getWeightingDescription() {
		if (weightingDescription == null) {
			weightingDescription = new ArrayList<>();
		}
		return this.weightingDescription;
	}

	/**
	 * Gets the value of the referenceToIncludedWeightingDataSets property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referenceToIncludedWeightingDataSets
	 * property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getReferenceToIncludedWeightingDataSets().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataSetReference }
	 * 
	 * 
	 */
	public List<DataSetReference> getReferenceToIncludedWeightingDataSets() {
		if (referenceToIncludedWeightingDataSets == null) {
			referenceToIncludedWeightingDataSets = new ArrayList<>();
		}
		return this.referenceToIncludedWeightingDataSets;
	}

}
